package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class TakeEntityAnim extends Entity {

   private static final long serialVersionUID = 1L;
   private int time = 0;
   private Entity item;
   private Entity player;
   private float xorg;
   private float yorg;
   private float zorg;


   public TakeEntityAnim(Level var1, Entity var2, Entity var3) {
      super(var1);
      this.item = var2;
      this.player = var3;
      this.setSize(1.0F, 1.0F);
      this.xorg = var2.x;
      this.yorg = var2.y;
      this.zorg = var2.z;
   }

   public void tick() {
      ++this.time;
      if(this.time >= 3) {
         this.remove();
      }

      float var1 = (var1 = (float)this.time / 3.0F) * var1;
      this.xo = this.item.xo = this.item.x;
      this.yo = this.item.yo = this.item.y;
      this.zo = this.item.zo = this.item.z;
      this.x = this.item.x = this.xorg + (this.player.x - this.xorg) * var1;
      this.y = this.item.y = this.yorg + (this.player.y - 1.0F - this.yorg) * var1;
      this.z = this.item.z = this.zorg + (this.player.z - this.zorg) * var1;
      this.setPos(this.x, this.y, this.z);
   }

   public void render(TextureManager var1, float var2) {
      this.item.render(var1, var2);
   }
}
