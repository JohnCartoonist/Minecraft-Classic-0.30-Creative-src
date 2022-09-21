package com.mojang.minecraft.render;

import com.mojang.minecraft.player.Player;
import java.util.Comparator;

public final class ChunkDirtyDistanceComparator implements Comparator {

   private Player player;


   public ChunkDirtyDistanceComparator(Player var1) {
      this.player = var1;
   }
}
