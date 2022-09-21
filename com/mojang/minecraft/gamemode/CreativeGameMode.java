package com.mojang.minecraft.gamemode;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gui.BlockSelectScreen;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.player.Player;

public final class CreativeGameMode extends GameMode {

   public CreativeGameMode(Minecraft var1) {
      super(var1);
      this.instantBreak = true;
   }

   public final void openInventory() {
      this.minecraft.setCurrentScreen(new BlockSelectScreen());
   }

   public final void apply(Level var1) {
      super.apply(var1);
      var1.removeAllNonCreativeModeEntities();
      var1.creativeMode = true;
      var1.growTrees = false;
   }

   public final void apply(Player var1) {
      for(int var2 = 0; var2 < 9; ++var2) {
         var1.inventory.count[var2] = 1;
         if(var1.inventory.slots[var2] <= 0) {
            var1.inventory.slots[var2] = ((Block)SessionData.allowedBlocks.get(var2)).id;
         }
      }

   }

   public final boolean isSurvival() {
      return false;
   }
}
