package com.mojang.minecraft.net;

import com.mojang.minecraft.net.NetworkPlayer;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

final class SkinDownloadThread extends Thread {

   // $FF: synthetic field
   private NetworkPlayer player;


   SkinDownloadThread(NetworkPlayer var1) {
      this.player = var1;
      super();
   }

   public final void run() {
      HttpURLConnection var1 = null;

      try {
         (var1 = (HttpURLConnection)(new URL("http://www.minecraft.net/skin/" + this.player.name + ".png")).openConnection()).setDoInput(true);
         var1.setDoOutput(false);
         var1.connect();
         if(var1.getResponseCode() != 404) {
            this.player.newTexture = ImageIO.read(var1.getInputStream());
            return;
         }

         return;
      } catch (Exception var5) {
         var5.printStackTrace();
      } finally {
         var1.disconnect();
      }

   }
}
