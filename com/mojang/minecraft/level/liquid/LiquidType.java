package com.mojang.minecraft.level.liquid;


public final class LiquidType {

   private static LiquidType[] values = new LiquidType[4];
   public static final LiquidType NOT_LIQUID = new LiquidType(0);
   public static final LiquidType WATER = new LiquidType(1);
   public static final LiquidType LAVA = new LiquidType(2);


   private LiquidType(int var1) {
      values[var1] = this;
   }

}
