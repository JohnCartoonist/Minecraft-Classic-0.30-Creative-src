package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.item.ItemModel;
import com.mojang.minecraft.item.TakeEntityAnim;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class Item extends Entity {

   public static final long serialVersionUID = 0L;
   private static ItemModel[] models = new ItemModel[256];
   private float xd;
   private float yd;
   private float zd;
   private float rot;
   private int resource;
   private int tickCount;
   private int age = 0;


   public static void initModels() {
      for(int var0 = 0; var0 < 256; ++var0) {
         Block var1;
         if((var1 = Block.blocks[var0]) != null) {
            models[var0] = new ItemModel(var1.textureId);
         }
      }

   }

   public Item(Level var1, float var2, float var3, float var4, int var5) {
      super(var1);
      this.setSize(0.25F, 0.25F);
      this.heightOffset = this.bbHeight / 2.0F;
      this.setPos(var2, var3, var4);
      this.resource = var5;
      this.rot = (float)(Math.random() * 360.0D);
      this.xd = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D);
      this.yd = 0.2F;
      this.zd = (float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D);
      this.makeStepSound = false;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.yd -= 0.04F;
      this.move(this.xd, this.yd, this.zd);
      this.xd *= 0.98F;
      this.yd *= 0.98F;
      this.zd *= 0.98F;
      if(this.onGround) {
         this.xd *= 0.7F;
         this.zd *= 0.7F;
         this.yd *= -0.5F;
      }

      ++this.tickCount;
      ++this.age;
      if(this.age >= 6000) {
         this.remove();
      }

   }

   public void render(TextureManager var1, float var2) {
      this.textureId = var1.load("/terrain.png");
      GL11.glBindTexture(3553, this.textureId);
      float var5 = this.level.getBrightness((int)this.x, (int)this.y, (int)this.z);
      float var3 = this.rot + ((float)this.tickCount + var2) * 3.0F;
      GL11.glPushMatrix();
      GL11.glColor4f(var5, var5, var5, 1.0F);
      float var4 = (var5 = MathHelper.sin(var3 / 10.0F)) * 0.1F + 0.1F;
      GL11.glTranslatef(this.xo + (this.x - this.xo) * var2, this.yo + (this.y - this.yo) * var2 + var4, this.zo + (this.z - this.zo) * var2);
      GL11.glRotatef(var3, 0.0F, 1.0F, 0.0F);
      models[this.resource].generateList();
      var5 = (var5 = (var5 = var5 * 0.5F + 0.5F) * var5) * var5;
      GL11.glColor4f(1.0F, 1.0F, 1.0F, var5 * 0.4F);
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 1);
      GL11.glDisable(3008);
      models[this.resource].generateList();
      GL11.glEnable(3008);
      GL11.glDisable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPopMatrix();
      GL11.glEnable(3553);
   }

   public void playerTouch(Entity var1) {
      Player var2;
      if((var2 = (Player)var1).addResource(this.resource)) {
         this.level.addEntity(new TakeEntityAnim(this.level, this, var2));
         this.remove();
      }

   }

}
