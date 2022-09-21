package com.mojang.minecraft.gamemode;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.ProgressBarDisplay;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.MobSpawner;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.player.Player;

public final class SurvivalGameMode extends GameMode {

   private int hitX;
   private int hitY;
   private int hitZ;
   private int hits;
   private int hardness;
   private int hitDelay;
   private MobSpawner spawner;


   public final void preparePlayer(Player var1) {
      var1.inventory.slots[8] = Block.TNT.id;
      var1.inventory.count[8] = 10;
   }

   public final void breakBlock(int var1, int var2, int var3) {
      int var4 = this.minecraft.level.getTile(var1, var2, var3);
      Block.blocks[var4].onBreak(this.minecraft.level, var1, var2, var3);
      super.breakBlock(var1, var2, var3);
   }

   public final boolean canPlace(int var1) {
      return this.minecraft.player.inventory.removeResource(var1);
   }

   public final void hitBlock(int var1, int var2, int var3) {
      int var4;
      if((var4 = this.minecraft.level.getTile(var1, var2, var3)) > 0 && Block.blocks[var4].getHardness() == 0) {
         this.breakBlock(var1, var2, var3);
      }

   }

   public final void resetHits() {
      this.hits = 0;
      this.hitDelay = 0;
   }

   public final void hitBlock(int var1, int var2, int var3, int var4) {
      if(this.hitDelay > 0) {
         --this.hitDelay;
      } else if(var1 == this.hitX && var2 == this.hitY && var3 == this.hitZ) {
         int var5;
         if((var5 = this.minecraft.level.getTile(var1, var2, var3)) != 0) {
            Block var6 = Block.blocks[var5];
            this.hardness = var6.getHardness();
            var6.spawnBlockParticles(this.minecraft.level, var1, var2, var3, var4, this.minecraft.particleManager);
            ++this.hits;
            if(this.hits == this.hardness + 1) {
               this.breakBlock(var1, var2, var3);
               this.hits = 0;
               this.hitDelay = 5;
            }

         }
      } else {
         this.hits = 0;
         this.hitX = var1;
         this.hitY = var2;
         this.hitZ = var3;
      }
   }

   public final void applyCracks(float var1) {
      if(this.hits <= 0) {
         this.minecraft.levelRenderer.cracks = 0.0F;
      } else {
         this.minecraft.levelRenderer.cracks = ((float)this.hits + var1 - 1.0F) / (float)this.hardness;
      }
   }

   public final float getReachDistance() {
      return 4.0F;
   }

   public final boolean useItem(Player var1, int var2) {
      Block var3;
      if((var3 = Block.blocks[var2]) == Block.RED_MUSHROOM && this.minecraft.player.inventory.removeResource(var2)) {
         var1.hurt((Entity)null, 3);
         return true;
      } else if(var3 == Block.BROWN_MUSHROOM && this.minecraft.player.inventory.removeResource(var2)) {
         var1.heal(5);
         return true;
      } else {
         return false;
      }
   }

   public final void apply(Level var1) {
      super.apply(var1);
      this.spawner = new MobSpawner(var1);
   }

   public final void spawnMob() {
      MobSpawner var3;
      int var1 = (var3 = this.spawner).level.width * var3.level.height * var3.level.depth / 64 / 64 / 64;
      if(var3.level.random.nextInt(100) < var1 && var3.level.countInstanceOf(Mob.class) < var1 * 20) {
         var3.spawn(var1, var3.level.player, (ProgressBarDisplay)null);
      }

   }

   public final void prepareLevel(Level var1) {
      this.spawner = new MobSpawner(var1);
      (this = this).minecraft.progressBar.setText("Spawning..");
      int var2 = var1.width * var1.height * var1.depth / 800;
      this.spawner.spawn(var2, (Entity)null, this.minecraft.progressBar);
   }
}
