package com.drugs;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import java.util.UUID;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import org.mockito.MockedStatic;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartelDealerTest {
    @TempDir Path directory;
    private DrugsV2 plugin;
    private YamlConfiguration prices;
    private MockedStatic<DrugRegistry> registry;

    @BeforeEach
    void setup() throws Exception {
        plugin = mock(DrugsV2.class);
        when(plugin.getName()).thenReturn("DrugsV2");
        when(plugin.namespace()).thenReturn("drugsv2");
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        var instance = DrugsV2.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, plugin);
        registry = mockStatic(DrugRegistry.class);
        registry.when(() -> DrugRegistry.isDrugRegistered("joint")).thenReturn(true);
        prices = new YamlConfiguration();
        prices.set("prices.joint", 15.0);
        prices.save(directory.resolve("cartel.yml").toFile());
    }

    @AfterEach void cleanup() { registry.close(); }

    private ItemStack drug(String id, String type, String strain) {
        ItemStack item = mock(ItemStack.class);
        when(item.hasItemMeta()).thenReturn(true);
        when(item.getAmount()).thenReturn(3);
        when(item.clone()).thenReturn(item);
        ItemMeta meta = mock(ItemMeta.class);
        PersistentDataContainer data = mock(PersistentDataContainer.class);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(data);
        when(data.get(new NamespacedKey(plugin, "drug_id"), PersistentDataType.STRING)).thenReturn(id);
        when(data.get(new NamespacedKey(plugin, "item_type"), PersistentDataType.STRING)).thenReturn(type);
        when(data.get(new NamespacedKey(plugin, "strain_id"), PersistentDataType.STRING)).thenReturn(strain);
        return item;
    }

    @Test void rejectsVanillaUnknownAndPlantingItems() {
        assertEquals(0, CartelDealer.price(mock(ItemStack.class), prices));
        assertEquals(0, CartelDealer.price(drug("unknown", "unknown", null), prices));
        assertEquals(0, CartelDealer.price(drug("joint", "cannabis_plant", null), prices));
    }

    @Test void strainOverrideAndInvalidPrices() {
        ItemStack joint = drug("joint", "joint", "reggie");
        assertEquals(15, CartelDealer.price(joint, prices));
        prices.set("strain-prices.joint.reggie", 9.0);
        assertEquals(9, CartelDealer.price(joint, prices));
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            prices.set("strain-prices.joint.reggie", invalid);
            assertEquals(0, CartelDealer.price(joint, prices));
        }
    }


    private Item dropped(ItemStack stack) {
        Item item = mock(Item.class);
        when(item.isValid()).thenReturn(true);
        when(item.getUniqueId()).thenReturn(UUID.randomUUID());
        when(item.getItemStack()).thenReturn(stack);
        when(item.getPickupDelay()).thenReturn(10);
        return item;
    }

    @Test void successfulDropPaysAndRemovesFullStack() {
        Player player = mock(Player.class);
        Item item = dropped(drug("joint", "joint", null));
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        when(payments.deposit(player, 45)).thenReturn(true);
        assertEquals(3, new CartelDealer(plugin).sellDropped(player, item, 16, payments));
        verify(payments).deposit(player, 45);
        verify(item).remove();
        verify(player, never()).getInventory();
    }

    @Test void excessDroppedItemsRemainOnGround() {
        Player player = mock(Player.class);
        ItemStack joint = drug("joint", "joint", null);
        ItemStack leftover = mock(ItemStack.class);
        when(joint.clone()).thenReturn(leftover);
        Item item = dropped(joint);
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        when(payments.deposit(player, 30)).thenReturn(true);
        assertEquals(2, new CartelDealer(plugin).sellDropped(player, item, 2, payments));
        verify(leftover).setAmount(1);
        verify(item).setItemStack(leftover);
        verify(item, never()).remove();
        verify(item).setPickupDelay(10);
    }

    @Test void failedAndThrowingDepositsLeaveDropUntouched() {
        for (boolean throwing : new boolean[]{false, true}) {
            Player player = mock(Player.class);
            Item item = dropped(drug("joint", "joint", null));
            CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
            if (throwing) when(payments.deposit(player, 45)).thenThrow(new IllegalStateException("offline"));
            assertEquals(0, new CartelDealer(plugin).sellDropped(player, item, 16, payments));
            verify(item, never()).remove();
            verify(item, never()).setItemStack(any());
            verify(item).setPickupDelay(10);
        }
    }

    @Test void nestedPaymentCallbackCannotSellSameDropTwice() {
        Player player = mock(Player.class);
        Item item = dropped(drug("joint", "joint", null));
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        CartelDealer dealer = new CartelDealer(plugin);
        when(payments.deposit(player, 45)).thenAnswer(call -> {
            assertEquals(0, dealer.sellDropped(player, item, 16, payments));
            return true;
        });
        assertEquals(3, dealer.sellDropped(player, item, 16, payments));
        verify(payments, times(1)).deposit(player, 45);
    }

    @Test void invalidOrUnpricedDropsNeverDepositMoney() {
        Player player = mock(Player.class);
        Item item = dropped(mock(ItemStack.class));
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        assertEquals(0, new CartelDealer(plugin).sellDropped(player, item, 16, payments));
        verifyNoInteractions(payments);
    }
}
