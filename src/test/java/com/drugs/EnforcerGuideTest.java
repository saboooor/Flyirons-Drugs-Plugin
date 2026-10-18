package com.drugs;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnforcerGuideTest {
    private MockedStatic<Bukkit> bukkit;
    private MockedConstruction<ItemStack> items;
    private final List<Inventory> inventories = new ArrayList<>();
    private DrugsV2 plugin;
    private Player player;
    private EnforcerGuide guide;

    @BeforeEach void setup() {
        bukkit = mockStatic(Bukkit.class);
        items = mockConstruction(ItemStack.class, (item, context) -> {
            when(item.getItemMeta()).thenReturn(mock(ItemMeta.class));
        });
        plugin = mock(DrugsV2.class);
        when(plugin.getName()).thenReturn("FlyironsDrugs");
        var server = mock(Server.class);
        var scheduler = mock(BukkitScheduler.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        doAnswer(call -> { ((Runnable) call.getArgument(1)).run(); return null; })
                .when(scheduler).runTask(eq(plugin), any(Runnable.class));
        bukkit.when(() -> Bukkit.createInventory(any(InventoryHolder.class), eq(54), anyString()))
                .thenAnswer(call -> {
                    Inventory inventory = mock(Inventory.class);
                    when(inventory.getHolder()).thenReturn(call.getArgument(0));
                    when(inventory.getSize()).thenReturn(54);
                    inventories.add(inventory);
                    return inventory;
                });
        player = mock(Player.class);
        guide = new EnforcerGuide(plugin);
    }

    @AfterEach void cleanup() { items.close(); bukkit.close(); }

    private InventoryClickEvent click(Inventory inventory, int slot) {
        InventoryView view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(inventory);
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getView()).thenReturn(view);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getRawSlot()).thenReturn(slot);
        guide.click(event);
        verify(event).setCancelled(true);
        return event;
    }

    @Test void dialogueOpensRecipeGridAndGrowingPage() {
        ShapedRecipe recipe = mock(ShapedRecipe.class);
        ItemStack paper = mock(ItemStack.class);
        when(paper.getType()).thenReturn(Material.PAPER);
        ItemStack result = mock(ItemStack.class);
        when(result.clone()).thenReturn(result);
        when(recipe.getResult()).thenReturn(result);
        when(recipe.getShape()).thenReturn(new String[]{" P ", "   ", "   "});
        when(recipe.getIngredientMap()).thenReturn(Map.of('P', paper));
        bukkit.when(() -> Bukkit.getRecipe(new NamespacedKey("flyironsdrugs", "joint"))).thenReturn(recipe);
        EnforcerGuide.open(plugin, player, "joint");
        click(inventories.getFirst(), 29);
        Inventory preview = inventories.get(1);
        verify(preview).setItem(11, paper);
        verify(preview).setItem(24, result);
        click(preview, 49);
        assertEquals(3, inventories.size());
        verify(inventories.get(2)).setItem(eq(10), any(ItemStack.class));
        click(inventories.get(2), 45);
        assertEquals(4, inventories.size());
    }

    @Test void bottomInventoryClicksAndDragsCannotTakePreviewItems() {
        EnforcerGuide.open(plugin, player, "joint");
        Inventory inventory = inventories.getFirst();
        click(inventory, 60);
        assertEquals(1, inventories.size());
        InventoryView view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(inventory);
        InventoryDragEvent drag = mock(InventoryDragEvent.class);
        when(drag.getView()).thenReturn(view);
        guide.drag(drag);
        verify(drag).setCancelled(true);
    }

    @Test void growingInstructionsUseConfiguredCropAndCannabisMechanics() {
        try (MockedStatic<DrugGrowing> growing = mockStatic(DrugGrowing.class)) {
            growing.when(() -> DrugGrowing.crop("coca")).thenReturn(
                    new DrugGrowing.Crop("coca", "Coca Leaves", Material.VINE, Material.VINE, 123));
            String instructions = String.join(" ", EnforcerGuide.growingSteps("coca"));
            assertTrue(instructions.contains("wheat seeds + vine"));
            assertTrue(instructions.contains("123 seconds"));
            assertTrue(instructions.contains("light at least 9"));
        }
        assertTrue(String.join(" ", EnforcerGuide.growingSteps("cannabis")).contains("using shears"));
    }
}
