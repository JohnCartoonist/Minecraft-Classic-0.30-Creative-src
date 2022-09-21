package com.mojang.minecraft.render;

import com.mojang.minecraft.player.Player;
import java.util.Comparator;

public final class ChunkDistanceComparator implements Comparator {

   private Player player;


   public ChunkDistanceComparator(Player var1) {
      this.player = var1;
   }
}
