package com.drugs;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

    @Test void successfulSaleRemovesOnlyAcceptedStorageItems() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack joint = drug("joint", "joint", null);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
        when(inventory.getItem(0)).thenReturn(joint);
        when(inventory.getItem(1)).thenReturn(mock(ItemStack.class));
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        when(payments.deposit(player, 45)).thenReturn(true);
        new CartelDealer(plugin).sell(player, true, payments);
        verify(payments).deposit(player, 45);
        verify(inventory).setItem(0, null);
        verify(inventory, never()).setItem(eq(1), any());
        verify(inventory, never()).setItem(0, joint);
    }

    @Test void failedAndThrowingPaymentsRestoreHeldStack() {
        for (boolean throwing : new boolean[]{false, true}) {
            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack joint = drug("joint", "joint", null);
            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
            when(inventory.getHeldItemSlot()).thenReturn(4);
            when(inventory.getItem(4)).thenReturn(joint);
            CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
            if (throwing) when(payments.deposit(player, 45)).thenThrow(new IllegalStateException("offline"));
            new CartelDealer(plugin).sell(player, false, payments);
            var order = inOrder(inventory, payments);
            order.verify(inventory).setItem(4, null);
            order.verify(payments).deposit(player, 45);
            order.verify(inventory).setItem(4, joint);
        }
    }
    @Test void demandTakesExactlyTheRequestedQuantityAcrossStacks() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
        ItemStack first = drug("joint", "joint", null);
        ItemStack second = drug("joint", "joint", "reggie");
        ItemStack original = mock(ItemStack.class);
        ItemStack leftover = mock(ItemStack.class);
        when(second.clone()).thenReturn(original, leftover);
        when(inventory.getItem(0)).thenReturn(first);
        when(inventory.getItem(1)).thenReturn(second);
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        when(payments.deposit(player, 60)).thenReturn(true);
        assertTrue(new CartelDealer(plugin).sellDemand(player, "joint", 4, payments));
        verify(inventory).setItem(0, null);
        verify(leftover).setAmount(2);
        verify(inventory).setItem(1, leftover);
        verify(payments).deposit(player, 60);
        verify(inventory, never()).setItem(1, original);
    }

    @Test void insufficientDemandTakesNothingAndPaysNothing() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
        ItemStack joint = drug("joint", "joint", null);
        when(inventory.getItem(0)).thenReturn(joint);
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        assertFalse(new CartelDealer(plugin).sellDemand(player, "joint", 4, payments));
        verify(inventory, never()).setItem(anyInt(), any());
        verifyNoInteractions(payments);
    }

    @Test void rejectedDemandPaymentRestoresEntireOriginalStack() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getStorageContents()).thenReturn(new ItemStack[36]);
        ItemStack joint = drug("joint", "joint", null);
        ItemStack original = mock(ItemStack.class);
        ItemStack leftover = mock(ItemStack.class);
        when(joint.clone()).thenReturn(original, leftover);
        when(inventory.getItem(0)).thenReturn(joint);
        CartelDealer.Payments payments = mock(CartelDealer.Payments.class);
        assertFalse(new CartelDealer(plugin).sellDemand(player, "joint", 2, payments));
        var order = inOrder(inventory, payments);
        order.verify(inventory).setItem(0, leftover);
        order.verify(payments).deposit(player, 30);
        order.verify(inventory).setItem(0, original);
    }

}
