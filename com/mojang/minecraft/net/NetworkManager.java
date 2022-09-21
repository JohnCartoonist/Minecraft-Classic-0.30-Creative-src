package com.mojang.minecraft.net;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.net.NetworkPlayer;
import com.mojang.minecraft.net.PacketType;
import com.mojang.minecraft.net.ServerConnectThread;
import com.mojang.net.NetworkHandler;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class NetworkManager {

   public ByteArrayOutputStream levelData;
   public NetworkHandler netHandler;
   public Minecraft minecraft;
   public boolean successful = false;
   public boolean levelLoaded = false;
   public HashMap players = new HashMap();


   public NetworkManager(Minecraft var1, String var2, int var3, String var4, String var5) {
      var1.online = true;
      this.minecraft = var1;
      (new ServerConnectThread(this, var2, var3, var4, var5, var1)).start();
   }

   public final void sendBlockChange(int var1, int var2, int var3, int var4, int var5) {
      this.netHandler.send(PacketType.PLAYER_SET_BLOCK, new Object[]{Integer.valueOf(var1), Integer.valueOf(var2), Integer.valueOf(var3), Integer.valueOf(var4), Integer.valueOf(var5)});
   }

   public final void error(Exception var1) {
      this.netHandler.close();
      this.minecraft.setCurrentScreen(new ErrorScreen("Disconnected!", var1.getMessage()));
      var1.printStackTrace();
   }

   public final boolean isConnected() {
      NetworkHandler var1;
      return this.netHandler != null && (var1 = this.netHandler).connected;
   }

   public final List getPlayers() {
      ArrayList var1;
      (var1 = new ArrayList()).add(this.minecraft.session.username);
      Iterator var3 = this.players.values().iterator();

      while(var3.hasNext()) {
         NetworkPlayer var2 = (NetworkPlayer)var3.next();
         var1.add(var2.name);
      }

      return var1;
   }
}
