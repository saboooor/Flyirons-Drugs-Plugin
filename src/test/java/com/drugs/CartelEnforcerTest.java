package com.drugs;

import com.destroystokyo.paper.entity.Pathfinder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartelEnforcerTest {
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<DrugRegistry> registry;
    private Player player;
    private Villager npc;
    private CartelDealer dealer;
    private CartelEnforcer enforcer;
    private YamlConfiguration config;
    private AtomicLong time;
    private World world;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        bukkit = mockStatic(Bukkit.class);
        registry = mockStatic(DrugRegistry.class);
        registry.when(() -> DrugRegistry.isDrugRegistered("joint")).thenReturn(true);
        DrugsV2 plugin = mock(DrugsV2.class);
        when(plugin.getName()).thenReturn("DrugsV2");
        dealer = mock(CartelDealer.class);
        config = new YamlConfiguration();
        config.set("enforcer.enabled", false);
        config.set("enforcer.deadline-seconds", 10);
        config.set("enforcer.warning-interval-seconds", 5);
        config.set("prices.joint", 15.0);
        when(dealer.settings()).thenReturn(config);
        when(dealer.payments()).thenReturn(mock(CartelDealer.Payments.class));
        time = new AtomicLong(1000);
        world = mock(World.class);
        when(world.getMinHeight()).thenReturn(-64);
        when(world.getMaxHeight()).thenReturn(320);
        when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);
        WorldBorder border = mock(WorldBorder.class);
        when(world.getWorldBorder()).thenReturn(border);
        when(border.isInside(any())).thenReturn(true);
        Block floor = mock(Block.class);
        when(floor.getType()).thenReturn(Material.STONE);
        when(floor.isSolid()).thenReturn(true);
        Block air = mock(Block.class);
        when(air.getType()).thenReturn(Material.AIR);
        when(air.isPassable()).thenReturn(true);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenAnswer(call ->
                (int) call.getArgument(1) == 63 ? floor : air);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.isOnline()).thenReturn(true);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.hasPermission("drugs.cartel.sell")).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(new Location(world, 0, 64, 0));
        bukkit.when(() -> Bukkit.getPlayer(player.getUniqueId())).thenReturn(player);
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(player));
        npc = mock(Villager.class);
        when(npc.getUniqueId()).thenReturn(UUID.randomUUID());
        when(npc.isValid()).thenReturn(true);
        when(npc.getWorld()).thenReturn(world);
        when(npc.getLocation()).thenReturn(new Location(world, 1, 64, 0));
        when(npc.hasLineOfSight(player)).thenReturn(true);
        PersistentDataContainer data = mock(PersistentDataContainer.class);
        when(npc.getPersistentDataContainer()).thenReturn(data);
        when(data.has(any(NamespacedKey.class), eq(PersistentDataType.BYTE))).thenReturn(true);
        Pathfinder pathfinder = mock(Pathfinder.class);
        Pathfinder.PathResult path = mock(Pathfinder.PathResult.class);
        when(npc.getPathfinder()).thenReturn(pathfinder);
        when(pathfinder.findPath(player)).thenReturn(path);
        when(path.getFinalPoint()).thenReturn(new Location(world, 0, 64, 0));
        when(pathfinder.moveTo(path, 1.15)).thenReturn(true);
        when(world.spawn(any(Location.class), eq(Villager.class), any(Consumer.class))).thenAnswer(call -> {
            Consumer<Villager> configure = call.getArgument(2);
            configure.accept(npc);
            return npc;
        });
        enforcer = new CartelEnforcer(plugin, dealer, time::get);
    }

    @AfterEach void cleanup() { registry.close(); bukkit.close(); }

    private void spawnAndApproach() {
        assertTrue(enforcer.spawn(player, "joint", 16, true));
        enforcer.tick();
    }

    private PlayerInteractEntityEvent interaction(Player who) {
        PlayerInteractEntityEvent event = mock(PlayerInteractEntityEvent.class);
        when(event.getPlayer()).thenReturn(who);
        when(event.getRightClicked()).thenReturn(npc);
        when(event.getHand()).thenReturn(EquipmentSlot.HAND);
        return event;
    }

    @Test void approachesWarnsAndThreatensAtDeadlineWithNoDefaultDamage() {
        spawnAndApproach();
        verify(player).sendMessage(contains("16 joint within 10s"));
        time.addAndGet(5000);
        enforcer.tick();
        verify(player).sendMessage(contains("5s left"));
        time.addAndGet(5000);
        enforcer.tick();
        verify(player).sendMessage(contains("Watch your back"));
        verify(npc).remove();
        verify(player, never()).damage(anyDouble(), any(org.bukkit.entity.Entity.class));
    }

    @Test void deadlineDoesNotStartUntilTheNpcReachesThePlayer() {
        when(npc.getLocation()).thenReturn(new Location(world, 10, 64, 0));
        assertTrue(enforcer.spawn(player, "joint", 16, true));
        time.addAndGet(15000);
        enforcer.tick();
        verify(player, never()).sendMessage(contains("within 10s"));
        verify(player, never()).sendMessage(contains("Watch your back"));
        when(npc.getLocation()).thenReturn(new Location(world, 1, 64, 0));
        enforcer.tick();
        verify(player).sendMessage(contains("16 joint within 10s"));
        time.addAndGet(9000);
        enforcer.tick();
        verify(npc, never()).remove();
    }

    @Test void fulfillmentPaysOnlyOnceAndRemovesEnforcer() {
        spawnAndApproach();
        when(dealer.sellDemand(eq(player), eq("joint"), eq(16), any())).thenReturn(true);
        PlayerInteractEntityEvent event = interaction(player);
        enforcer.onInteract(event);
        enforcer.onInteract(event);
        verify(dealer, times(1)).sellDemand(eq(player), eq("joint"), eq(16), any());
        verify(npc, times(1)).remove();
        verify(player).sendMessage(contains("You're clear"));
        verify(event, times(2)).setCancelled(true);
    }

    @Test void otherPlayersCannotFulfillSomeonesDemand() {
        spawnAndApproach();
        Player other = mock(Player.class);
        when(other.getUniqueId()).thenReturn(UUID.randomUUID());
        enforcer.onInteract(interaction(other));
        verify(dealer, never()).sellDemand(any(), anyString(), anyInt(), any());
        verify(npc, never()).remove();
    }

    @Test void firstEarlyClickExplainsTheDealWithoutSelling() {
        assertTrue(enforcer.spawn(player, "joint", 16, true));
        enforcer.onInteract(interaction(player));
        verify(player).sendMessage(contains("16 joint within 10s"));
        verify(dealer, never()).sellDemand(any(), anyString(), anyInt(), any());
    }

    @Test void insufficientOrFailedSaleDoesNotCompleteDemand() {
        spawnAndApproach();
        enforcer.onInteract(interaction(player));
        verify(dealer).sellDemand(eq(player), eq("joint"), eq(16), any());
        verify(npc, never()).remove();
        verify(player, never()).sendMessage(contains("You're clear"));
    }

    @Test void quitRemovesNpcAndAppliesCooldown() {
        spawnAndApproach();
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);
        enforcer.onQuit(event);
        verify(npc).remove();
        assertFalse(enforcer.spawn(player, "joint", 16, false));
    }

    @Test void rejectsBypassedPlayersAndInvalidOrUnpricedDemands() {
        assertFalse(enforcer.spawn(player, "joint", 0, true));
        assertFalse(enforcer.spawn(player, "joint", 2305, true));
        assertFalse(enforcer.spawn(player, "unknown", 16, true));
        config.set("prices.joint", 0);
        assertFalse(enforcer.spawn(player, "joint", 16, true));
        config.set("prices.joint", 15);
        when(player.hasPermission("drugs.cartel.enforcer.bypass")).thenReturn(true);
        assertFalse(enforcer.spawn(player, "joint", 16, true));
    }

    @Test void unreachableNpcIsRemovedWithoutIssuingADemand() {
        when(npc.getPathfinder().findPath(player)).thenReturn(null);
        assertFalse(enforcer.spawn(player, "joint", 16, true));
        verify(npc).remove();
        verify(player, never()).sendMessage(anyString());
    }

    @Test void vanishedEconomyCallsOffDemandWithoutSelling() {
        spawnAndApproach();
        when(dealer.payments()).thenReturn(null);
        enforcer.onInteract(interaction(player));
        verify(npc).remove();
        verify(dealer, never()).sellDemand(any(), anyString(), anyInt(), any());
        verify(player).sendMessage(contains("called off"));
    }
    @Test void optionalDamageIsLimitedByCurrentHealth() {
        config.set("enforcer.failure-damage", 20);
        when(player.getHealth()).thenReturn(3.0);
        spawnAndApproach();
        time.addAndGet(10000);
        enforcer.tick();
        verify(player).damage(2.0, npc);
        verify(npc).remove();
    }

    @Test void unloadedGroundIsSkippedWithoutGeneratingChunks() {
        when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(false);
        assertFalse(enforcer.spawn(player, "joint", 16, true));
        verify(world, never()).getBlockAt(anyInt(), anyInt(), anyInt());
        verify(world, never()).spawn(any(Location.class), eq(Villager.class), any(Consumer.class));
    }

    @Test void reloadCallsOffActiveDemandsWithoutThreats() {
        spawnAndApproach();
        enforcer.reload();
        time.addAndGet(20000);
        enforcer.tick();
        verify(npc).remove();
        verify(player).sendMessage(contains("called off"));
        verify(player, never()).sendMessage(contains("Watch your back"));
    }

    @Test void unavailableEconomyCancelsAtDeadlineInsteadOfThreatening() {
        spawnAndApproach();
        when(dealer.payments()).thenReturn(null);
        time.addAndGet(10000);
        enforcer.tick();
        verify(npc).remove();
        verify(player).sendMessage(contains("called off"));
        verify(player, never()).sendMessage(contains("Watch your back"));
    }

    @Test void randomDemandsVaryDrugAndQuantityWithinConfiguredBounds() {
        registry.when(DrugRegistry::getRegisteredDrugNames).thenReturn(Set.of("joint", "meth", "glue", "lsd", "heroin", "molly"));
        config.set("prices.meth", 75);
        config.set("prices.glue", 0);
        config.set("prices.lsd", -1);
        config.set("prices.heroin", Double.NaN);
        config.set("prices.molly", Double.POSITIVE_INFINITY);
        config.set("enforcer.min-amount", 3);
        config.set("enforcer.max-amount", 5);
        Random random = new Random(42);
        Set<String> chosenDrugs = new HashSet<>();
        Set<Integer> chosenAmounts = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            CartelEnforcer.Demand demand = enforcer.randomDemand(random);
            assertNotNull(demand);
            assertTrue(Set.of("joint", "meth").contains(demand.drug()));
            assertTrue(demand.amount() >= 3 && demand.amount() <= 5);
            chosenDrugs.add(demand.drug());
            chosenAmounts.add(demand.amount());
        }
        assertEquals(Set.of("joint", "meth"), chosenDrugs);
        assertEquals(Set.of(3, 4, 5), chosenAmounts);
    }

    @Test void randomDemandRequiresAtLeastOnePricedRegisteredDrug() {
        registry.when(DrugRegistry::getRegisteredDrugNames).thenReturn(Set.of("unknown"));
        assertNull(enforcer.randomDemand(new Random(42)));
    }

    @Test void randomQuantityLimitsAreClampedAndLegacyFixedSettingsIgnored() {
        registry.when(DrugRegistry::getRegisteredDrugNames).thenReturn(Set.of("joint"));
        config.set("enforcer.drug", "unknown");
        config.set("enforcer.amount", 16);
        config.set("enforcer.min-amount", 9999);
        config.set("enforcer.max-amount", -10);
        CartelEnforcer.Demand demand = enforcer.randomDemand(new Random(42));
        assertEquals("joint", demand.drug());
        assertEquals(2304, demand.amount());
        config.set("enforcer.min-amount", -5);
        config.set("enforcer.max-amount", -1);
        assertEquals(1, enforcer.randomDemand(new Random(42)).amount());
    }

}
