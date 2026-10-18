package com.drugs;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** A bounded velocity assist; Citizens remains responsible for ordinary navigation. */
final class EnforcerWallClimber {
    private Location landing;
    private int ticks;
    private boolean exhausted;

    void reset() {
        landing = null;
        ticks = 0;
        exhausted = false;
    }

    boolean tick(LivingEntity entity, Player target, int maxHeight, int timeoutTicks) {
        return tick(entity, target, maxHeight, timeoutTicks, () -> {});
    }

    boolean tick(LivingEntity entity, Player target, int maxHeight, int timeoutTicks, Runnable beforeMove) {
        Location here = entity.getLocation();
        if (!here.getWorld().equals(target.getWorld())) return false;
        if (landing == null) {
            if (exhausted) {
                if (entity.isOnGround()) exhausted = false;
                return false;
            }
            Vector direction = target.getLocation().toVector().subtract(here.toVector()).setY(0);
            if (direction.lengthSquared() < 0.01) return false;
            direction.normalize();
            Location wall = here.clone().add(direction.clone().multiply(0.8));
            if (wall.getBlockX() == here.getBlockX() && wall.getBlockZ() == here.getBlockZ()) return false;
            if (!wall.getWorld().isChunkLoaded(wall.getBlockX() >> 4, wall.getBlockZ() >> 4)
                    || (wall.getBlock().isPassable() || wall.getBlock().isLiquid())) return false;
            for (int height = 1; height <= maxHeight; height++) {
                Location candidate = new Location(here.getWorld(), wall.getBlockX() + 0.5,
                        here.getBlockY(), wall.getBlockZ() + 0.5);
                candidate.setY(here.getBlockY() + height);
                if (candidate.getY() + 2 >= here.getWorld().getMaxHeight()) break;
                if (clear(candidate) && !candidate.clone().subtract(0, 1, 0).getBlock().isPassable()
                        && !candidate.clone().subtract(0, 1, 0).getBlock().isLiquid()) {
                    landing = candidate.getBlock().getLocation().add(0.5, 0, 0.5);
                    ticks = 0;
                    break;
                }
            }
            if (landing == null) return false;
        }
        if (++ticks > timeoutTicks || !clear(landing)) {
            landing = null;
            exhausted = true;
            return false;
        }
        Vector across = landing.toVector().subtract(here.toVector()).setY(0);
        if (here.getY() >= landing.getY() && across.lengthSquared() < 0.16) {
            landing = null;
            exhausted = true;
            return false;
        }
        // Ascend beside the wall, then move over its edge. Never move through a ceiling.
        if (here.getY() < landing.getY() + 0.1) {
            if (!clear(here.clone().add(0, 0.35, 0))) {
                landing = null;
                exhausted = true;
                return false;
            }
            beforeMove.run();
            entity.setVelocity(new Vector(0, 0.25, 0));
        } else {
            if (across.lengthSquared() > 0.01) across.normalize().multiply(0.18);
            beforeMove.run();
            entity.setVelocity(across.setY(0.05));
        }
        entity.setFallDistance(0);
        return true;
    }

    void approachWhenNoPath(LivingEntity entity, Player target) {
        approachWhenNoPath(entity, target, 0.18);
    }

    void approachWhenNoPath(LivingEntity entity, Player target, double speed) {
        Location here = entity.getLocation();
        if (!here.getWorld().equals(target.getWorld())) return;
        Vector step = target.getLocation().toVector().subtract(here.toVector()).setY(0);
        if (step.lengthSquared() <= 6.25 || !entity.isOnGround()) return;
        step.normalize().multiply(speed);
        Location next = here.clone().add(step);
        Location floor = next.clone().subtract(0, 0.1, 0);
        if (clear(next) && !floor.getBlock().isPassable() && !floor.getBlock().isLiquid()) {
            entity.setVelocity(step.setY(entity.getVelocity().getY()));
        }
    }

    private boolean clear(Location feet) {
        // Check the player's full width and height, including edges between blocks.
        for (double x : new double[]{-0.3, 0.3}) {
            for (double z : new double[]{-0.3, 0.3}) {
                for (double y : new double[]{0, 0.9, 1.8}) {
                    Location sample = feet.clone().add(x, y, z);
                    if (!sample.getWorld().isChunkLoaded(sample.getBlockX() >> 4, sample.getBlockZ() >> 4)
                            || !sample.getBlock().isPassable() || sample.getBlock().isLiquid()) return false;
                }
            }
        }
        return true;
    }
}
