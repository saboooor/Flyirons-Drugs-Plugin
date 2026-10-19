package me.dexuby.UltimateDrugs.drugs.growing;

import me.dexuby.UltimateDrugs.block.BlockOffset;
import me.dexuby.UltimateDrugs.block.OffsetBlock;
import org.bukkit.block.data.BlockData;

public class GrowingBlock extends OffsetBlock {
   private String skullTexture;
   private boolean randomRotation;
   private boolean applyPhysics;
   private boolean autoConnect;
   private boolean ignoreSpace;

   public GrowingBlock(BlockData var1, BlockOffset var2, String var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      super(var1, var2);
      this.skullTexture = var3;
      this.randomRotation = var4;
      this.applyPhysics = var5;
      this.autoConnect = var6;
      this.ignoreSpace = var7;
   }

   public String getSkullTexture() {
      return this.skullTexture;
   }

   public boolean useRandomRotation() {
      return this.randomRotation;
   }

   public boolean doApplyPhysics() {
      return this.applyPhysics;
   }

   public boolean doAutoConnect() {
      return this.autoConnect;
   }

   public boolean doIgnoreSpace() {
      return this.ignoreSpace;
   }
}
