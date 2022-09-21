package com.mojang.minecraft;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.model.Vec3D;

public final class MovingObjectPosition {

   public int entityPos;
   public int x;
   public int y;
   public int z;
   public int face;
   public Vec3D vec;
   public Entity entity;


   public MovingObjectPosition(int var1, int var2, int var3, int var4, Vec3D var5) {
      this.entityPos = 0;
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.face = var4;
      this.vec = new Vec3D(var5.x, var5.y, var5.z);
   }

   public MovingObjectPosition(Entity var1) {
      this.entityPos = 1;
      this.entity = var1;
   }
}
