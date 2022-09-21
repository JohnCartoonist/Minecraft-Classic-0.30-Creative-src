package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.liquid.LiquidType;
import com.mojang.minecraft.level.tile.Block;

public final class SandBlock extends Block {

   public SandBlock(int var1, int var2) {
      super(var1, var2);
   }

   public final void onPlace(Level var1, int var2, int var3, int var4) {
      this.fall(var1, var2, var3, var4);
   }

   public final void onNeighborChange(Level var1, int var2, int var3, int var4, int var5) {
      this.fall(var1, var2, var3, var4);
   }

   private void fall(Level var1, int var2, int var3, int var4) {
      int var12 = var2;
      int var5 = var3;
      int var6 = var4;

      while(true) {
         int var8 = var5 - 1;
         int var10;
         LiquidType var11;
         if(!((var10 = var1.getTile(var12, var8, var6)) == 0?true:((var11 = Block.blocks[var10].getLiquidType()) == LiquidType.WATER?true:var11 == LiquidType.LAVA)) || var5 <= 0) {
            if(var5 != var3) {
               if((var10 = var1.getTile(var12, var5, var6)) > 0 && Block.blocks[var10].getLiquidType() != LiquidType.NOT_LIQUID) {
                  var1.setTileNoUpdate(var12, var5, var6, 0);
               }

               var1.swap(var2, var3, var4, var12, var5, var6);
            }

            return;
         }

         --var5;
      }
   }
}
