package com.mojang.minecraft.net;


public final class PacketType {

   public static final PacketType[] packets = new PacketType[256];
   public static final PacketType IDENTIFICATION = new PacketType(new Class[]{Byte.TYPE, String.class, String.class, Byte.TYPE});
   public static final PacketType LEVEL_INIT;
   public static final PacketType LEVEL_DATA;
   public static final PacketType LEVEL_FINALIZE;
   public static final PacketType PLAYER_SET_BLOCK;
   public static final PacketType BLOCK_CHANGE;
   public static final PacketType SPAWN_PLAYER;
   public static final PacketType POSITION_ROTATION;
   public static final PacketType POSITION_ROTATION_UPDATE;
   public static final PacketType POSITION_UPDATE;
   public static final PacketType ROTATION_UPDATE;
   public static final PacketType DESPAWN_PLAYER;
   public static final PacketType CHAT_MESSAGE;
   public static final PacketType DISCONNECT;
   public static final PacketType UPDATE_PLAYER_TYPE;
   public final int length;
   private static int nextOpcode;
   public final byte opcode;
   public Class[] params;


   private PacketType(Class ... var1) {
      this.opcode = (byte)(nextOpcode++);
      packets[this.opcode] = this;
      this.params = new Class[var1.length];
      int var2 = 0;

      for(int var3 = 0; var3 < var1.length; ++var3) {
         Class var4 = var1[var3];
         this.params[var3] = var4;
         if(var4 == Long.TYPE) {
            var2 += 8;
         } else if(var4 == Integer.TYPE) {
            var2 += 4;
         } else if(var4 == Short.TYPE) {
            var2 += 2;
         } else if(var4 == Byte.TYPE) {
            ++var2;
         } else if(var4 == Float.TYPE) {
            var2 += 4;
         } else if(var4 == Double.TYPE) {
            var2 += 8;
         } else if(var4 == byte[].class) {
            var2 += 1024;
         } else if(var4 == String.class) {
            var2 += 64;
         }
      }

      this.length = var2;
   }

   static {
      new PacketType(new Class[0]);
      LEVEL_INIT = new PacketType(new Class[0]);
      LEVEL_DATA = new PacketType(new Class[]{Short.TYPE, byte[].class, Byte.TYPE});
      LEVEL_FINALIZE = new PacketType(new Class[]{Short.TYPE, Short.TYPE, Short.TYPE});
      PLAYER_SET_BLOCK = new PacketType(new Class[]{Short.TYPE, Short.TYPE, Short.TYPE, Byte.TYPE, Byte.TYPE});
      BLOCK_CHANGE = new PacketType(new Class[]{Short.TYPE, Short.TYPE, Short.TYPE, Byte.TYPE});
      SPAWN_PLAYER = new PacketType(new Class[]{Byte.TYPE, String.class, Short.TYPE, Short.TYPE, Short.TYPE, Byte.TYPE, Byte.TYPE});
      POSITION_ROTATION = new PacketType(new Class[]{Byte.TYPE, Short.TYPE, Short.TYPE, Short.TYPE, Byte.TYPE, Byte.TYPE});
      POSITION_ROTATION_UPDATE = new PacketType(new Class[]{Byte.TYPE, Byte.TYPE, Byte.TYPE, Byte.TYPE, Byte.TYPE, Byte.TYPE});
      POSITION_UPDATE = new PacketType(new Class[]{Byte.TYPE, Byte.TYPE, Byte.TYPE, Byte.TYPE});
      ROTATION_UPDATE = new PacketType(new Class[]{Byte.TYPE, Byte.TYPE, Byte.TYPE});
      DESPAWN_PLAYER = new PacketType(new Class[]{Byte.TYPE});
      CHAT_MESSAGE = new PacketType(new Class[]{Byte.TYPE, String.class});
      DISCONNECT = new PacketType(new Class[]{String.class});
      UPDATE_PLAYER_TYPE = new PacketType(new Class[]{Byte.TYPE});
      nextOpcode = 0;
   }
}
