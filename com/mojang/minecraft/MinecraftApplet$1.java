package com.mojang.minecraft;

import com.mojang.minecraft.MinecraftApplet;
import java.awt.Canvas;

final class MinecraftApplet$1 extends Canvas {

   private static final long serialVersionUID = 1L;
   // $FF: synthetic field
   final MinecraftApplet applet;


   MinecraftApplet$1(MinecraftApplet var1) {
      this.applet = var1;
   }

   public final synchronized void addNotify() {
      super.addNotify();
      this.applet.startGameThread();
   }

   public final synchronized void removeNotify() {
      this.applet.stopGameThread();
      super.removeNotify();
   }
}
