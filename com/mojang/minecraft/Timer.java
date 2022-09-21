package com.mojang.minecraft;


public final class Timer {

   float tps;
   double lastHR;
   public int elapsedTicks;
   public float delta;
   public float speed = 1.0F;
   public float elapsedDelta = 0.0F;
   long lastSysClock;
   long lastHRClock;
   double adjustment = 1.0D;


   public Timer(float var1) {
      this.tps = var1;
      this.lastSysClock = System.currentTimeMillis();
      this.lastHRClock = System.nanoTime() / 1000000L;
   }
}
