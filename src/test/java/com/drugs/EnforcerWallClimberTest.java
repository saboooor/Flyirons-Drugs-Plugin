package com.drugs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnforcerWallClimberTest {
    private final World world = mock(World.class);
    private final LivingEntity entity = mock(LivingEntity.class);
    private final Player target = mock(Player.class);
    private final EnforcerWallClimber climber = new EnforcerWallClimber();
    private boolean ceiling;

    private void terrain(int wallHeight) {
        when(world.getMaxHeight()).thenReturn(320);
        when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
            int x = call.getArgument(0), y = call.getArgument(1), z = call.getArgument(2);
            boolean solid = y < 64 || (x == 1 && y < 64 + wallHeight)
                    || (ceiling && y == 66 && x == 0);
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(solid ? Material.STONE : Material.AIR);
            when(block.isPassable()).thenReturn(!solid);
            when(block.getLocation()).thenReturn(new Location(world, x, y, z));
            return block;
        });
        when(world.getBlockAt(any(Location.class))).thenAnswer(call -> {
            Location sample = call.getArgument(0);
            return world.getBlockAt(sample.getBlockX(), sample.getBlockY(), sample.getBlockZ());
        });
        when(entity.getLocation()).thenReturn(new Location(world, 0.5, 64, 0.5));
        when(target.getWorld()).thenReturn(world);
        when(target.getLocation()).thenReturn(new Location(world, 4, 67, 0.5));
    }

    @Test void climbsWallAndResumesAfterReachingTheLedge() {
        terrain(3);
        assertTrue(climber.tick(entity, target, 6, 100));
        verify(entity).setVelocity(new Vector(0, 0.25, 0));
        when(entity.getLocation()).thenReturn(new Location(world, 0.5, 67.2, 0.5));
        assertTrue(climber.tick(entity, target, 6, 100));
        when(entity.getLocation()).thenReturn(new Location(world, 1.5, 67.1, 0.5));
        assertFalse(climber.tick(entity, target, 6, 100));
    }

    @Test void cancelsNavigationBeforeApplyingClimbingVelocity() {
        terrain(3);
        Runnable cancel = mock(Runnable.class);
        assertTrue(climber.tick(entity, target, 6, 100, cancel));
        var order = inOrder(cancel, entity);
        order.verify(cancel).run();
        order.verify(entity).setVelocity(new Vector(0, 0.25, 0));
    }

    @Test void failedPathStillApproachesAWallAcrossClearGround() {
        terrain(3);
        when(entity.getLocation()).thenReturn(new Location(world, -1.5, 64, 0.5));
        when(entity.isOnGround()).thenReturn(true);
        when(entity.getVelocity()).thenReturn(new Vector());
        climber.approachWhenNoPath(entity, target);
        verify(entity).setVelocity(new Vector(0.18, 0, 0));
    }

    @Test void refusesWallsAboveTheConfiguredHeight() {
        terrain(7);
        assertFalse(climber.tick(entity, target, 6, 100));
        verify(entity, never()).setVelocity(any());
    }

    @Test void refusesToClimbThroughACeiling() {
        terrain(3);
        ceiling = true;
        assertFalse(climber.tick(entity, target, 6, 100));
        verify(entity, never()).setVelocity(any());
    }

    @Test void abandonsClimbingWhenTimeRunsOut() {
        terrain(3);
        assertTrue(climber.tick(entity, target, 6, 1));
        assertFalse(climber.tick(entity, target, 6, 1));
        assertFalse(climber.tick(entity, target, 6, 1));
    }
}
