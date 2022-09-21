package com.mojang.minecraft.player;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.player.InputHandler;

public final class InputHandlerImpl extends InputHandler {

   private boolean[] keyStates = new boolean[10];
   private GameSettings settings;


   public InputHandlerImpl(GameSettings var1) {
      this.settings = var1;
   }

   public final void setKeyState(int var1, boolean var2) {
      byte var3 = -1;
      if(var1 == this.settings.forwardKey.key) {
         var3 = 0;
      }

      if(var1 == this.settings.backKey.key) {
         var3 = 1;
      }

      if(var1 == this.settings.leftKey.key) {
         var3 = 2;
      }

      if(var1 == this.settings.rightKey.key) {
         var3 = 3;
      }

      if(var1 == this.settings.jumpKey.key) {
         var3 = 4;
      }

      if(var3 >= 0) {
         this.keyStates[var3] = var2;
      }

   }

   public final void resetKeys() {
      for(int var1 = 0; var1 < 10; ++var1) {
         this.keyStates[var1] = false;
      }

   }

   public final void updateMovement() {
      this.xxa = 0.0F;
      this.jumping = 0.0F;
      if(this.keyStates[0]) {
         --this.jumping;
      }

      if(this.keyStates[1]) {
         ++this.jumping;
      }

      if(this.keyStates[2]) {
         --this.xxa;
      }

      if(this.keyStates[3]) {
         ++this.xxa;
      }

      this.yya = this.keyStates[4];
   }
}
