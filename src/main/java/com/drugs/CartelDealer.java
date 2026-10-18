package com.drugs;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

/** Persistent villager buyers; all inventory and economy operations run on the server thread. */
public final class CartelDealer implements Listener, CommandExecutor, TabCompleter {
    private final DrugsV2 plugin;
    private final NamespacedKey dealerKey;
    private FileConfiguration config;
    private CartelEnforcer enforcer;

    public CartelDealer(DrugsV2 plugin) {
        this.plugin = plugin;
        dealerKey = new NamespacedKey(plugin, "cartel_dealer");
        File file = new File(plugin.getDataFolder(), "cartel.yml");
        if (!file.exists()) plugin.saveResource("cartel.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    void setEnforcer(CartelEnforcer enforcer) { this.enforcer = enforcer; }

    FileConfiguration settings() { return config; }

    private boolean isDealer(Entity entity) {
        return entity instanceof Villager && entity.getPersistentDataContainer().has(dealerKey, PersistentDataType.BYTE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("drugs.cartel.admin")) {
            sender.sendMessage(ChatColor.RED + "You cannot manage cartel dealers.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("enforcer")) {
            return enforcer != null && enforcer.onCommand(sender, args);
        }
        if (args.length != 1) return false;
        if (args[0].equalsIgnoreCase("reload")) {
            config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "cartel.yml"));
            if (enforcer != null) enforcer.reload();
            sender.sendMessage(ChatColor.GREEN + "Cartel prices and encounters reloaded.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Run this command in game.");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> {
                player.getWorld().spawn(player.getLocation(), Villager.class, villager -> {
                    villager.getPersistentDataContainer().set(dealerKey, PersistentDataType.BYTE, (byte) 1);
                    villager.setCustomName(ChatColor.DARK_GREEN + "Cartel Buyer");
                    villager.setCustomNameVisible(true);
                    villager.setAI(false);
                    villager.setInvulnerable(true);
                    villager.setSilent(true);
                    villager.setCollidable(false);
                    villager.setPersistent(true);
                    villager.setRemoveWhenFarAway(false);
                });
                player.sendMessage(ChatColor.GREEN + "Cartel buyer created. Right-click to sell.");
            }
            case "remove" -> {
                Entity target = player.getTargetEntity(5);
                if (target != null && isDealer(target)) {
                    target.remove();
                    player.sendMessage(ChatColor.GREEN + "Cartel buyer removed.");
                } else player.sendMessage(ChatColor.RED + "Look directly at a cartel buyer within five blocks.");
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("drugs.cartel.admin")) return List.of();
        if (args.length > 1 && args[0].equalsIgnoreCase("enforcer")) {
            List<String> options = switch (args.length) {
                case 2 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                case 3 -> new ArrayList<>(DrugRegistry.getRegisteredDrugNames());
                case 4 -> List.of("16", "32", "64");
                default -> List.of();
            };
            return options.stream().filter(s -> s.toLowerCase(Locale.ROOT)
                    .startsWith(args[args.length - 1].toLowerCase(Locale.ROOT))).toList();
        }
        if (args.length != 1) return List.of();
        return List.of("create", "remove", "reload", "enforcer").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!isDealer(event.getRightClicked())) return;
        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("drugs.cartel.sell")) {
            player.sendMessage(ChatColor.RED + "You cannot sell to cartel buyers.");
            return;
        }
        DealerMenu menu = new DealerMenu(event.getRightClicked().getUniqueId());
        menu.inventory = Bukkit.createInventory(menu, 9, "Cartel Buyer");
        menu.inventory.setItem(2, button(Material.EMERALD, "Sell held stack", "Sells the stack in your main hand."));
        menu.inventory.setItem(6, button(Material.CHEST, "Sell all drugs", "Sells all priced drugs in your inventory."));
        player.openInventory(menu.inventory);
    }

    private ItemStack button(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(List.of(ChatColor.GRAY + lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (isDealer(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof DealerMenu) event.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof DealerMenu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() != 2 && event.getRawSlot() != 6) return;
        Entity dealer = Bukkit.getEntity(menu.dealer);
        if (!player.hasPermission("drugs.cartel.sell") || dealer == null || !dealer.isValid()
                || !isDealer(dealer) || dealer.getWorld() != player.getWorld()
                || dealer.getLocation().distanceSquared(player.getLocation()) > 36) {
            player.closeInventory();
            return;
        }
        Payments payments = payments();
        if (payments == null) {
            player.sendMessage(ChatColor.RED + "Selling requires Vault and an enabled economy provider.");
            return;
        }
        sell(player, event.getRawSlot() == 6, payments);
    }

    Payments payments() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) return null;
        return VaultPayments.find();
    }

    static double price(ItemStack item, FileConfiguration prices) {
        if (item == null || !item.hasItemMeta() || item.getAmount() <= 0) return 0;
        ItemMeta meta = item.getItemMeta();
        String drug = DrugItemMetadata.getDrugId(meta);
        if (drug == null || !DrugRegistry.isDrugRegistered(drug)) return 0;
        // Reject equipment, planting stock, and partially used cartridges.
        if (!drug.equals(DrugItemMetadata.getItemType(meta))) return 0;
        Integer durability = DrugItemMetadata.getCartDurability(meta);
        if (drug.equals("cart") && (durability == null || durability != MechanicsConfig.getCartDurabilityUses())) return 0;
        double value = prices.getDouble("prices." + drug, 0);
        String strain = DrugItemMetadata.getStrainId(meta);
        if (strain != null) value = prices.getDouble("strain-prices." + drug + "." + strain, value);
        return Double.isFinite(value) && value > 0 ? value : 0;
    }

    void sell(Player player, boolean all, Payments payments) {
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> sold = new LinkedHashMap<>();
        double total = 0;
        int count = 0;
        for (int slot = 0; slot < inventory.getStorageContents().length; slot++) {
            if (!all && slot != inventory.getHeldItemSlot()) continue;
            ItemStack item = inventory.getItem(slot);
            double unit = price(item, config);
            if (unit == 0) continue;
            sold.put(slot, item.clone());
            total += unit * item.getAmount();
            count += item.getAmount();
        }
        if (sold.isEmpty() || !Double.isFinite(total)) {
            player.sendMessage(ChatColor.RED + "You have no drugs this buyer accepts.");
            return;
        }
        Map<Integer, ItemStack> remaining = new LinkedHashMap<>();
        sold.keySet().forEach(slot -> remaining.put(slot, null));
        completeSale(player, sold, remaining, total, count, payments);
    }

    boolean sellDemand(Player player, String drug, int amount, Payments payments) {
        if (amount <= 0 || amount > 2304) return false;
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> original = new LinkedHashMap<>();
        Map<Integer, ItemStack> remaining = new LinkedHashMap<>();
        int needed = amount;
        double total = 0;
        for (int slot = 0; slot < inventory.getStorageContents().length && needed > 0; slot++) {
            ItemStack item = inventory.getItem(slot);
            double unit = price(item, config);
            if (unit == 0 || !drug.equals(DrugItemMetadata.getDrugId(item.getItemMeta()))) continue;
            int taken = Math.min(needed, item.getAmount());
            original.put(slot, item.clone());
            ItemStack leftover = null;
            if (taken < item.getAmount()) {
                leftover = item.clone();
                leftover.setAmount(item.getAmount() - taken);
            }
            remaining.put(slot, leftover);
            needed -= taken;
            total += taken * unit;
        }
        if (needed != 0 || !Double.isFinite(total)) {
            player.sendMessage(ChatColor.RED + "Bring " + amount + " " + drug + " to complete this sale. Nothing was taken.");
            return false;
        }
        return completeSale(player, original, remaining, total, amount, payments);
    }

    private boolean completeSale(Player player, Map<Integer, ItemStack> original,
                                 Map<Integer, ItemStack> remaining, double total, int count, Payments payments) {
        PlayerInventory inventory = player.getInventory();
        // Remove before calling the provider; restore on a rejected or throwing deposit.
        remaining.forEach(inventory::setItem);
        boolean paid;
        try {
            paid = payments.deposit(player, total);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Cartel payment failed: " + exception.getMessage());
            paid = false;
        }
        if (!paid) {
            original.forEach(inventory::setItem);
            player.sendMessage(ChatColor.RED + "Payment failed. Your items have been returned.");
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "Sold " + count + " items for " + payments.format(total) + ".");
        return true;
    }

    interface Payments {
        boolean deposit(Player player, double amount);
        String format(double amount);
    }

    // Isolated so loading the dealer without Vault installed does not resolve Vault classes.
    private static final class VaultPayments implements Payments {
        private final net.milkbowl.vault.economy.Economy economy;
        private VaultPayments(net.milkbowl.vault.economy.Economy economy) { this.economy = economy; }
        static Payments find() {
            var registration = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (registration == null || !registration.getProvider().isEnabled()) return null;
            return new VaultPayments(registration.getProvider());
        }
        public boolean deposit(Player player, double amount) {
            return economy.depositPlayer(player, amount).transactionSuccess();
        }
        public String format(double amount) { return economy.format(amount); }
    }

    private static final class DealerMenu implements InventoryHolder {
        private final UUID dealer;
        private Inventory inventory;
        private DealerMenu(UUID dealer) { this.dealer = dealer; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
