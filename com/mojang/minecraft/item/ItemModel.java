package com.mojang.minecraft.item;

import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.TexturedQuad;
import com.mojang.minecraft.model.Vertex;

public final class ItemModel {

   private ModelPart model = new ModelPart(0, 0);


   public ItemModel(int var1) {
      ModelPart var10000 = this.model;
      int var2 = var1;
      boolean var14 = true;
      var14 = true;
      var14 = true;
      float var3 = -2.0F;
      float var4 = -2.0F;
      float var15 = -2.0F;
      ModelPart var16 = var10000;
      var10000.vertices = new Vertex[8];
      var16.quads = new TexturedQuad[6];
      Vertex var5 = new Vertex(var15, var4, var3, 0.0F, 0.0F);
      Vertex var6 = new Vertex(2.0F, var4, var3, 0.0F, 8.0F);
      Vertex var7 = new Vertex(2.0F, 2.0F, var3, 8.0F, 8.0F);
      Vertex var19 = new Vertex(var15, 2.0F, var3, 8.0F, 0.0F);
      Vertex var8 = new Vertex(var15, var4, 2.0F, 0.0F, 0.0F);
      Vertex var20 = new Vertex(2.0F, var4, 2.0F, 0.0F, 8.0F);
      Vertex var9 = new Vertex(2.0F, 2.0F, 2.0F, 8.0F, 8.0F);
      Vertex var17 = new Vertex(var15, 2.0F, 2.0F, 8.0F, 0.0F);
      var16.vertices[0] = var5;
      var16.vertices[1] = var6;
      var16.vertices[2] = var7;
      var16.vertices[3] = var19;
      var16.vertices[4] = var8;
      var16.vertices[5] = var20;
      var16.vertices[6] = var9;
      var16.vertices[7] = var17;
      float var10 = 0.25F;
      float var11 = 0.25F;
      float var12 = ((float)(var2 % 16) + (1.0F - var10)) / 16.0F;
      float var13 = ((float)(var2 / 16) + (1.0F - var11)) / 16.0F;
      var10 = ((float)(var2 % 16) + var10) / 16.0F;
      float var18 = ((float)(var2 / 16) + var11) / 16.0F;
      var16.quads[0] = new TexturedQuad(new Vertex[]{var20, var6, var7, var9}, var12, var13, var10, var18);
      var16.quads[1] = new TexturedQuad(new Vertex[]{var5, var8, var17, var19}, var12, var13, var10, var18);
      var16.quads[2] = new TexturedQuad(new Vertex[]{var20, var8, var5, var6}, var12, var13, var10, var18);
      var16.quads[3] = new TexturedQuad(new Vertex[]{var7, var19, var17, var9}, var12, var13, var10, var18);
      var16.quads[4] = new TexturedQuad(new Vertex[]{var6, var5, var19, var7}, var12, var13, var10, var18);
      var16.quads[5] = new TexturedQuad(new Vertex[]{var8, var20, var9, var17}, var12, var13, var10, var18);
   }

   public final void generateList() {
      this.model.render(0.0625F);
   }
}
