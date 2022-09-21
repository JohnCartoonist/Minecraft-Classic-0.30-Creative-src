package com.mojang.minecraft.net;


public final class PositionUpdate {

   public float x;
   public float y;
   public float z;
   public float yaw;
   public float pitch;
   public boolean rotation = false;
   public boolean position = false;


   public PositionUpdate(float var1, float var2, float var3, float var4, float var5) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.yaw = var4;
      this.pitch = var5;
      this.rotation = true;
      this.position = true;
   }

   public PositionUpdate(float var1, float var2, float var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
      this.position = true;
      this.rotation = false;
   }

   public PositionUpdate(float var1, float var2) {
      this.yaw = var1;
      this.pitch = var2;
      this.rotation = true;
      this.position = false;
   }
}
