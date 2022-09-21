package com.mojang.minecraft;

import com.mojang.minecraft.Minecraft;

final class SleepForeverThread extends Thread {

   SleepForeverThread(Minecraft var1) {
      this.setDaemon(true);
      this.start();
   }

   public final void run() {
      while(true) {
         try {
            while(true) {
               Thread.sleep(2147483647L);
            }
         } catch (InterruptedException var1) {
            ;
         }
      }
   }
}
