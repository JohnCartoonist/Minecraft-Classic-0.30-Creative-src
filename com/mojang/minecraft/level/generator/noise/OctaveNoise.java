package com.mojang.minecraft.level.generator.noise;

import com.mojang.minecraft.level.generator.noise.Noise;
import com.mojang.minecraft.level.generator.noise.PerlinNoise;
import java.util.Random;

public final class OctaveNoise extends Noise {

   private PerlinNoise[] perlin;
   private int octaves;


   public OctaveNoise(Random var1, int var2) {
      this.octaves = var2;
      this.perlin = new PerlinNoise[var2];

      for(int var3 = 0; var3 < var2; ++var3) {
         this.perlin[var3] = new PerlinNoise(var1);
      }

   }

   public final double compute(double var1, double var3) {
      double var5 = 0.0D;
      double var7 = 1.0D;

      for(int var9 = 0; var9 < this.octaves; ++var9) {
         var5 += this.perlin[var9].compute(var1 / var7, var3 / var7) * var7;
         var7 *= 2.0D;
      }

      return var5;
   }
}
