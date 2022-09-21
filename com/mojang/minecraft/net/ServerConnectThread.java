package com.mojang.minecraft.net;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.net.PacketType;
import com.mojang.net.NetworkHandler;
import java.io.IOException;

final class ServerConnectThread extends Thread {

   // $FF: synthetic field
   private String server;
   // $FF: synthetic field
   private int port;
   // $FF: synthetic field
   private String username;
   // $FF: synthetic field
   private String key;
   // $FF: synthetic field
   private Minecraft minecraft;
   // $FF: synthetic field
   private NetworkManager netManager;


   ServerConnectThread(NetworkManager var1, String var2, int var3, String var4, String var5, Minecraft var6) {
      this.netManager = var1;
      this.server = var2;
      this.port = var3;
      this.username = var4;
      this.key = var5;
      this.minecraft = var6;
      super();
   }

   public final void run() {
      boolean var1;
      NetworkManager var2;
      try {
         NetworkManager var10000 = this.netManager;
         NetworkHandler var4 = new NetworkHandler(this.server, this.port);
         var10000.netHandler = var4;
         var2 = this.netManager;
         NetworkManager var5 = this.netManager;
         NetworkHandler var10001 = this.netManager.netHandler;
         this.netManager.netHandler.netManager = var5;
         var2 = this.netManager;
         this.netManager.netHandler.send(PacketType.IDENTIFICATION, new Object[]{Byte.valueOf((byte)7), this.username, this.key, Integer.valueOf(0)});
         var1 = true;
         var2 = this.netManager;
         this.netManager.successful = var1;
      } catch (IOException var3) {
         this.minecraft.online = false;
         this.minecraft.networkManager = null;
         this.minecraft.setCurrentScreen(new ErrorScreen("Failed to connect", "You failed to connect to the server. It\'s probably down!"));
         var1 = false;
         var2 = this.netManager;
         this.netManager.successful = var1;
      }
   }
}
