package com.drugs.farm;

import com.drugs.items.DrugItemMetadata;

import com.drugs.growing.CannabisPlantListener;
import com.drugs.config.MechanicsConfig;
import com.drugs.growing.DrugGrowing;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FarmWorkerService implements Runnable {
    private static final double MAX_WORK_DISTANCE_SQUARED = 24 * 24;
    private static final double WORK_REACH_DISTANCE_SQUARED = 2.5 * 2.5;

    private final FarmManager manager;
    private final DrugGrowing growing;
    private final java.util.Set<UUID> navigating = new java.util.HashSet<>();
    private final Random random = new Random();

    public FarmWorkerService(FarmManager manager, DrugGrowing growing) {
        this.manager = manager;
        this.growing = growing;
    }

    @Override
    public void run() {
        for (Farm farm : manager.getFarms()) {
            manager.pruneInvalidVillagers(farm);
            if (!farm.isEnabled() || farm.getAssignedVillagers().isEmpty() || !farm.hasRegion()) {
                continue;
            }

            World world = Bukkit.getWorld(farm.getWorldId());
            if (world == null || world.getPlayers().stream().noneMatch(Player::isOnline)) {
                continue;
            }

            processFarm(farm, world);
        }
    }

    private void processFarm(Farm farm, World world) {
        navigating.clear();
        List<Villager> workers = new ArrayList<>();
        for (UUID workerId : farm.getAssignedVillagers()) {
            Entity entity = Bukkit.getEntity(workerId);
            if (entity instanceof Villager villager && villager.getWorld().equals(world) && villager.isValid() && !villager.isDead()) {
                villager.setPersistent(true);
                villager.setRemoveWhenFarAway(false);
                workers.add(villager);
            }
        }
        if (workers.isEmpty()) {
            return;
        }

        int speedMultiplier = Math.min(Farm.MAX_WORKERS, workers.size());
        int maxHarvest = Math.max(1, MechanicsConfig.getMaxHarvestPerCycle() * speedMultiplier);
        int maxPlant = Math.max(1, MechanicsConfig.getMaxPlantPerCycle() * speedMultiplier);
        int perWorkerHarvestLimit = Math.max(1, MechanicsConfig.getMaxHarvestPerCycle());
        int perWorkerPlantLimit = Math.max(1, MechanicsConfig.getMaxPlantPerCycle());

        Map<UUID, Integer> workerHarvestQuota = new HashMap<>();
        Map<UUID, Integer> workerPlantQuota = new HashMap<>();
        for (Villager worker : workers) {
            workerHarvestQuota.put(worker.getUniqueId(), perWorkerHarvestLimit);
            workerPlantQuota.put(worker.getUniqueId(), perWorkerPlantLimit);
        }

        List<ItemStack> workerSeeds = new ArrayList<>();
        int harvests = 0;
        int plants = 0;
        int inspected = 0;
        int total = farmVolume(farm);
        int cursor = farm.getScanCursor();

        while (inspected < total && (harvests < maxHarvest || plants < maxPlant)) {
            int index = (cursor + inspected) % total;
            Block block = blockAtIndex(world, farm, index);
            // A wand selection made on farmland should work on the crops above it.
            if (block.getType() == Material.FARMLAND) block = block.getRelative(0, 1, 0);

            if (harvests < maxHarvest && growing != null && growing.isMature(block)) {
                Villager worker = selectReachableWorker(workers, block, workerHarvestQuota);
                if (worker != null) {
                    List<ItemStack> drops = growing.harvestMature(block);
                    if (!drops.isEmpty()) {
                        workerSeeds.add(drops.get(0));
                        for (int d = 1; d < drops.size(); d++) storeOutput(farm, drops.get(d));
                        consumeQuota(workerHarvestQuota, worker.getUniqueId());
                        harvests++;
                        moveWorker(worker, block.getLocation().add(0.5, 0, 0.5));
                    }
                }
            }

            if (harvests < maxHarvest && block.getType() == Material.LARGE_FERN) {
                Villager worker = selectReachableWorker(workers, block, workerHarvestQuota);
                if (worker != null) {
                    CannabisPlantListener.HarvestResult result = CannabisPlantListener.harvestCannabis(block, true);
                    if (result != null) {
                        for (ItemStack drop : result.getDrops()) {
                            if (drop.getType() == Material.FERN && drop.hasItemMeta()
                                && "cannabis_plant".equals(com.drugs.items.DrugItemMetadata.getItemType(drop.getItemMeta()))) workerSeeds.add(drop);
                            else storeOutput(farm, drop);
                        }
                        consumeQuota(workerHarvestQuota, worker.getUniqueId());
                        harvests++;
                        moveWorker(worker, result.getRootBlock().getLocation().add(0.5, 0, 0.5));
                        result.getRootBlock().getWorld().spawnParticle(Particle.HAPPY_VILLAGER, result.getRootBlock().getLocation().add(0.5, 0.8, 0.5), 6, 0.2, 0.3, 0.2, 0.0);
                    }
                }
            }

            if (plants < maxPlant && !workerSeeds.isEmpty() && block.getType().isAir()) {
                Villager planter = selectReachableWorker(workers, block, workerPlantQuota);
                if (planter != null) {
                    for (int seedIndex = 0; seedIndex < workerSeeds.size(); seedIndex++) {
                        ItemStack seed = workerSeeds.get(seedIndex);
                        boolean planted = false;
                        if (seed.getType() == Material.FERN && isPlantable(block)) {
                            block.setType(Material.FERN, false);
                            planted = CannabisPlantListener.applyPlacedCannabis(block, seed);
                            if (!planted) block.setType(Material.AIR, false);
                        } else if (growing != null) {
                            planted = growing.plantSeed(block, seed);
                        }
                        if (!planted) continue;
                        workerSeeds.remove(seedIndex);
                        plants++;
                        consumeQuota(workerPlantQuota, planter.getUniqueId());
                        moveWorker(planter, block.getLocation().add(0.5, 0, 0.5));
                        world.spawnParticle(Particle.COMPOSTER, block.getLocation().add(0.5, 0.8, 0.5), 4, 0.2, 0.1, 0.2, 0.0);
                        break;
                    }
                }
            }

            inspected++;
        }

        farm.setScanCursor((cursor + inspected) % Math.max(1, total));

        for (ItemStack seed : workerSeeds) {
            storeOutput(farm, seed);
        }
    }

    static void storeOutput(Farm farm, ItemStack item) {
        Location output = farm.getOutputChest();
        if (output != null && output.getWorld() != null
                && output.getWorld().isChunkLoaded(output.getBlockX() >> 4, output.getBlockZ() >> 4)
                && output.getBlock().getState() instanceof org.bukkit.block.Chest chest) {
            for (ItemStack leftover : chest.getInventory().addItem(item).values()) {
                farm.getControllerLocation().getWorld().dropItemNaturally(farm.getControllerLocation(), leftover);
            }
            return;
        }
        farm.getControllerLocation().getWorld().dropItemNaturally(farm.getControllerLocation(), item);
    }

    private Villager selectReachableWorker(List<Villager> workers, Block targetPlant, Map<UUID, Integer> quota) {
        List<Villager> shuffled = new ArrayList<>(workers);
        java.util.Collections.shuffle(shuffled, random);
        for (Villager worker : shuffled) {
            if (quota.getOrDefault(worker.getUniqueId(), 0) > 0 && canReach(worker, targetPlant)) {
                return worker;
            }
        }
        return null;
    }

    private void consumeQuota(Map<UUID, Integer> quota, UUID workerId) {
        int current = quota.getOrDefault(workerId, 0);
        if (current > 0) {
            quota.put(workerId, current - 1);
        }
    }

    private boolean canReach(Villager villager, Block targetPlant) {
        if (villager.isDead() || !villager.isValid() || villager.getWorld() != targetPlant.getWorld()) {
            return false;
        }

        Location stand = findStandLocation(targetPlant, villager.getLocation());
        if (stand == null) {
            return false;
        }

        double distSq = villager.getLocation().distanceSquared(stand);
        if (distSq > MAX_WORK_DISTANCE_SQUARED) {
            return false;
        }

        if (distSq <= WORK_REACH_DISTANCE_SQUARED) return true;
        // Keep one target per worker for this cycle instead of overwriting the path
        // for every crop scanned. Work happens only after the worker arrives.
        if (navigating.add(villager.getUniqueId())) villager.getPathfinder().moveTo(stand);
        return false;
    }

    private Location findStandLocation(Block plantBlock, Location workerLocation) {
        Block root = CannabisPlantListener.getRootFernBlock(plantBlock);
        Block[] candidates = new Block[] {
                root,
                root.getRelative(1, 0, 0),
                root.getRelative(-1, 0, 0),
                root.getRelative(0, 0, 1),
                root.getRelative(0, 0, -1)
        };
        Location nearest = null;
        for (Block candidate : candidates) {
            if (!isOccupiable(candidate)) continue;
            Location stand = candidate.getLocation().add(0.5, 0, 0.5);
            if (nearest == null || workerLocation.distanceSquared(stand) < workerLocation.distanceSquared(nearest)) nearest = stand;
        }
        return nearest;
    }

    private boolean isOccupiable(Block feet) {
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);
        return feet.isPassable() && head.isPassable() && (ground.getType() == Material.FARMLAND || ground.getType().isSolid());
    }

    private void moveWorker(Villager villager, Location target) {
        villager.swingMainHand();

    }

    private boolean isPlantable(Block block) {
        return block.getType().isAir() && CannabisPlantListener.hasHydratedFarmland(block);
    }

    private int farmVolume(Farm farm) {
        return (farm.getMaxX() - farm.getMinX() + 1) * (farm.getMaxY() - farm.getMinY() + 1) * (farm.getMaxZ() - farm.getMinZ() + 1);
    }

    private Block blockAtIndex(World world, Farm farm, int index) {
        int sizeX = farm.getMaxX() - farm.getMinX() + 1;
        int sizeY = farm.getMaxY() - farm.getMinY() + 1;
        int sizeZ = farm.getMaxZ() - farm.getMinZ() + 1;

        int x = index % sizeX;
        int rem = index / sizeX;
        int y = rem % sizeY;
        int z = rem / sizeY;

        return world.getBlockAt(farm.getMinX() + x, farm.getMinY() + y, farm.getMinZ() + z);
    }
}
