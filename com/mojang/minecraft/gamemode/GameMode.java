package com.mojang.minecraft.gamemode;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.level.tile.Tile$SoundType;
import com.mojang.minecraft.player.Player;

public class GameMode {

   protected final Minecraft minecraft;
   public boolean instantBreak = false;


   public GameMode(Minecraft var1) {
      this.minecraft = var1;
   }

   public void apply(Level var1) {
      var1.creativeMode = false;
      var1.growTrees = true;
   }

   public void openInventory() {}

   public void hitBlock(int var1, int var2, int var3) {
      this.breakBlock(var1, var2, var3);
   }

   public boolean canPlace(int var1) {
      return true;
   }

   public void breakBlock(int var1, int var2, int var3) {
      Level var4 = this.minecraft.level;
      Block var5 = Block.blocks[var4.getTile(var1, var2, var3)];
      boolean var6 = var4.netSetTile(var1, var2, var3, 0);
      if(var5 != null && var6) {
         if(this.minecraft.isOnline()) {
            this.minecraft.networkManager.sendBlockChange(var1, var2, var3, 0, this.minecraft.player.inventory.getSelected());
         }

         if(var5.stepsound != Tile$SoundType.none) {
            var4.playSound("step." + var5.stepsound.name, (float)var1, (float)var2, (float)var3, (var5.stepsound.getVolume() + 1.0F) / 2.0F, var5.stepsound.getPitch() * 0.8F);
         }

         var5.spawnBreakParticles(var4, var1, var2, var3, this.minecraft.particleManager);
      }

   }

   public void hitBlock(int var1, int var2, int var3, int var4) {}

   public void resetHits() {}

   public void applyCracks(float var1) {}

   public float getReachDistance() {
      return 5.0F;
   }

   public boolean useItem(Player var1, int var2) {
      return false;
   }

   public void preparePlayer(Player var1) {}

   public void spawnMob() {}

   public void prepareLevel(Level var1) {}

   public boolean isSurvival() {
      return true;
   }

   public void apply(Player var1) {}
}
