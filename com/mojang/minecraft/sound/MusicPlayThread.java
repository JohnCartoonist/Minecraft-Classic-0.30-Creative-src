package com.mojang.minecraft.sound;

import com.mojang.minecraft.sound.Music;
import de.jarnbjo.ogg.EndOfOggStreamException;
import de.jarnbjo.vorbis.VorbisStream;
import java.nio.ByteBuffer;

final class MusicPlayThread extends Thread {

   // $FF: synthetic field
   private Music music;


   public MusicPlayThread(Music var1) {
      this.music = var1;
      super();
      this.setPriority(10);
      this.setDaemon(true);
   }

   public final void run() {
      try {
         while(!this.music.stopped) {
            Music var1 = this.music;
            Music var10001;
            ByteBuffer var2;
            if(this.music.playing == null) {
               var1 = this.music;
               if(this.music.current != null) {
                  var1 = this.music;
                  var2 = this.music.current;
                  var10001 = this.music;
                  this.music.playing = var2;
                  var2 = null;
                  var1 = this.music;
                  this.music.current = null;
                  var1 = this.music;
                  this.music.playing.clear();
               }
            }

            var1 = this.music;
            if(this.music.playing != null) {
               var1 = this.music;
               if(this.music.playing.remaining() != 0) {
                  while(true) {
                     var1 = this.music;
                     if(this.music.playing.remaining() == 0) {
                        break;
                     }

                     var1 = this.music;
                     var1 = this.music;
                     var2 = this.music.playing;
                     VorbisStream var11 = this.music.stream;
                     int var9 = this.music.stream.readPcm(var2.array(), var2.position(), var2.remaining());
                     var2.position(var2.position() + var9);
                     boolean var10;
                     if(var10 = var9 <= 0) {
                        this.music.finished = true;
                        this.music.stopped = true;
                        break;
                     }
                  }
               }
            }

            var1 = this.music;
            if(this.music.playing != null) {
               var1 = this.music;
               if(this.music.previous == null) {
                  var1 = this.music;
                  this.music.playing.flip();
                  var1 = this.music;
                  var2 = this.music.playing;
                  var10001 = this.music;
                  this.music.previous = var2;
                  var2 = null;
                  var1 = this.music;
                  this.music.playing = var2;
               }
            }

            Thread.sleep(10L);
            var1 = this.music;
            if(!this.music.player.running) {
               return;
            }
         }
      } catch (EndOfOggStreamException var6) {
         return;
      } catch (Exception var7) {
         var7.printStackTrace();
         return;
      } finally {
         this.music.finished = true;
      }

   }
}
