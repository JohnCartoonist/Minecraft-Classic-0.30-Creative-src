package com.mojang.minecraft.player;

import com.mojang.minecraft.mob.ai.BasicAI;
import com.mojang.minecraft.player.Player;

final class Player$1 extends BasicAI {

   public static final long serialVersionUID = 0L;
   // $FF: synthetic field
   final Player player;


   Player$1(Player var1) {
      this.player = var1;
   }

   protected final void update() {
      this.jumping = this.player.input.yya;
      this.xxa = this.player.input.xxa;
      this.yya = this.player.input.jumping;
   }
}
