package com.mojang.minecraft.level.generator.noise;

import com.mojang.minecraft.level.generator.noise.Noise;

public final class CombinedNoise extends Noise {

   private Noise noise1;
   private Noise noise2;


   public CombinedNoise(Noise var1, Noise var2) {
      this.noise1 = var1;
      this.noise2 = var2;
   }

   public final double compute(double var1, double var3) {
      return this.noise1.compute(var1 + this.noise2.compute(var1, var3), var3);
   }
}
