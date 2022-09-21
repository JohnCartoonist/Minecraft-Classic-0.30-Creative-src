package com.mojang.minecraft;

import com.mojang.minecraft.ChatLine;
import com.mojang.minecraft.Entity;
import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.Minecraft$OS;
import com.mojang.minecraft.MinecraftApplet;
import com.mojang.minecraft.MovingObjectPosition;
import com.mojang.minecraft.OperatingSystemLookup;
import com.mojang.minecraft.ProgressBarDisplay;
import com.mojang.minecraft.ResourceDownloadThread;
import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.SkinDownloadThread;
import com.mojang.minecraft.SleepForeverThread;
import com.mojang.minecraft.StopGameException;
import com.mojang.minecraft.Timer;
import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.gui.ChatInputScreen;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.gui.FontRenderer;
import com.mojang.minecraft.gui.GameOverScreen;
import com.mojang.minecraft.gui.GuiScreen;
import com.mojang.minecraft.gui.HUDScreen;
import com.mojang.minecraft.gui.PauseScreen;
import com.mojang.minecraft.item.Arrow;
import com.mojang.minecraft.item.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.generator.LevelGenerator;
import com.mojang.minecraft.level.liquid.LiquidType;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.minecraft.model.ModelManager;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.Vec3D;
import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.net.NetworkPlayer;
import com.mojang.minecraft.net.PacketType;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.particle.WaterDropParticle;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.InputHandlerImpl;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.Chunk;
import com.mojang.minecraft.render.ChunkDirtyDistanceComparator;
import com.mojang.minecraft.render.Frustrum;
import com.mojang.minecraft.render.FrustrumImpl;
import com.mojang.minecraft.render.HeldBlock;
import com.mojang.minecraft.render.LevelRenderer;
import com.mojang.minecraft.render.Renderer;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.minecraft.render.texture.TextureFX;
import com.mojang.minecraft.render.texture.TextureLavaFX;
import com.mojang.minecraft.render.texture.TextureWaterFX;
import com.mojang.minecraft.sound.SoundManager;
import com.mojang.minecraft.sound.SoundPlayer;
import com.mojang.net.NetworkHandler;
import com.mojang.util.MathHelper;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public final class Minecraft implements Runnable {

   public GameMode gamemode = new CreativeGameMode(this);
   private boolean fullscreen = false;
   public int width;
   public int height;
   private Timer timer = new Timer(20.0F);
   public Level level;
   public LevelRenderer levelRenderer;
   public Player player;
   public ParticleManager particleManager;
   public SessionData session = null;
   public String host;
   public Canvas canvas;
   public boolean levelLoaded = false;
   public volatile boolean waiting = false;
   private Cursor cursor;
   public TextureManager textureManager;
   public FontRenderer fontRenderer;
   public GuiScreen currentScreen = null;
   public ProgressBarDisplay progressBar = new ProgressBarDisplay(this);
   public Renderer renderer = new Renderer(this);
   public LevelIO levelIo;
   public SoundManager sound;
   private ResourceDownloadThread resourceThread;
   private int ticks;
   private int blockHitTime;
   public String levelName;
   public int levelId;
   public Robot robot;
   public HUDScreen hud;
   public boolean online;
   public NetworkManager networkManager;
   public SoundPlayer soundPlayer;
   public MovingObjectPosition selected;
   public GameSettings settings;
   private MinecraftApplet applet;
   String server;
   int port;
   volatile boolean running;
   public String debug;
   public boolean hasMouse;
   private int lastClick;
   public boolean raining;


   public Minecraft(Canvas var1, MinecraftApplet var2, int var3, int var4, boolean var5) {
      this.levelIo = new LevelIO(this.progressBar);
      this.sound = new SoundManager();
      this.ticks = 0;
      this.blockHitTime = 0;
      this.levelName = null;
      this.levelId = 0;
      this.online = false;
      new HumanoidModel(0.0F);
      this.selected = null;
      this.server = null;
      this.port = 0;
      this.running = false;
      this.debug = "";
      this.hasMouse = false;
      this.lastClick = 0;
      this.raining = false;

      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      this.applet = var2;
      new SleepForeverThread(this);
      this.canvas = var1;
      this.width = var3;
      this.height = var4;
      this.fullscreen = var5;
      if(var1 != null) {
         try {
            this.robot = new Robot();
            return;
         } catch (AWTException var8) {
            var8.printStackTrace();
         }
      }

   }

   public final void setCurrentScreen(GuiScreen var1) {
      if(!(this.currentScreen instanceof ErrorScreen)) {
         if(this.currentScreen != null) {
            this.currentScreen.onClose();
         }

         if(var1 == null && this.player.health <= 0) {
            var1 = new GameOverScreen();
         }

         this.currentScreen = (GuiScreen)var1;
         if(var1 != null) {
            if(this.hasMouse) {
               this.player.releaseAllKeys();
               this.hasMouse = false;
               if(this.levelLoaded) {
                  try {
                     Mouse.setNativeCursor((Cursor)null);
                  } catch (LWJGLException var4) {
                     var4.printStackTrace();
                  }
               } else {
                  Mouse.setGrabbed(false);
               }
            }

            int var2 = this.width * 240 / this.height;
            int var3 = this.height * 240 / this.height;
            ((GuiScreen)var1).open(this, var2, var3);
            this.online = false;
         } else {
            this.grabMouse();
         }
      }
   }

   private static void checkGLError(String var0) {
      int var1;
      if((var1 = GL11.glGetError()) != 0) {
         String var2 = GLU.gluErrorString(var1);
         System.out.println("########## GL ERROR ##########");
         System.out.println("@ " + var0);
         System.out.println(var1 + ": " + var2);
         System.exit(0);
      }

   }

   public final void shutdown() {
      try {
         if(this.soundPlayer != null) {
            SoundPlayer var1 = this.soundPlayer;
            this.soundPlayer.running = false;
         }

         if(this.resourceThread != null) {
            ResourceDownloadThread var4 = this.resourceThread;
            this.resourceThread.running = true;
         }
      } catch (Exception var3) {
         ;
      }

      Minecraft var5 = this;
      if(!this.levelLoaded) {
         try {
            LevelIO.save(var5.level, (OutputStream)(new FileOutputStream(new File("level.dat"))));
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }

      Mouse.destroy();
      Keyboard.destroy();
      Display.destroy();
   }

   public final void run() {
      this.running = true;

      try {
         Minecraft var1 = this;
         if(this.canvas != null) {
            Display.setParent(this.canvas);
         } else if(this.fullscreen) {
            Display.setFullscreen(true);
            this.width = Display.getDisplayMode().getWidth();
            this.height = Display.getDisplayMode().getHeight();
         } else {
            Display.setDisplayMode(new DisplayMode(this.width, this.height));
         }

         Display.setTitle("Minecraft 0.30");

         try {
            Display.create();
         } catch (LWJGLException var57) {
            var57.printStackTrace();

            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var56) {
               ;
            }

            Display.create();
         }

         Keyboard.create();
         Mouse.create();

         try {
            Controllers.create();
         } catch (Exception var55) {
            var55.printStackTrace();
         }

         checkGLError("Pre startup");
         GL11.glEnable(3553);
         GL11.glShadeModel(7425);
         GL11.glClearDepth(1.0D);
         GL11.glEnable(2929);
         GL11.glDepthFunc(515);
         GL11.glEnable(3008);
         GL11.glAlphaFunc(516, 0.0F);
         GL11.glCullFace(1029);
         GL11.glMatrixMode(5889);
         GL11.glLoadIdentity();
         GL11.glMatrixMode(5888);
         checkGLError("Startup");
         String var3 = "minecraft";
         String var5 = System.getProperty("user.home", ".");
         String var6;
         File var7;
         switch(OperatingSystemLookup.lookup[((var6 = System.getProperty("os.name").toLowerCase()).contains("win")?Minecraft$OS.windows:(var6.contains("mac")?Minecraft$OS.macos:(var6.contains("solaris")?Minecraft$OS.solaris:(var6.contains("sunos")?Minecraft$OS.solaris:(var6.contains("linux")?Minecraft$OS.linux:(var6.contains("unix")?Minecraft$OS.linux:Minecraft$OS.unknown)))))).ordinal()]) {
         case 1:
         case 2:
            var7 = new File(var5, '.' + var3 + '/');
            break;
         case 3:
            String var8;
            if((var8 = System.getenv("APPDATA")) != null) {
               var7 = new File(var8, "." + var3 + '/');
            } else {
               var7 = new File(var5, '.' + var3 + '/');
            }
            break;
         case 4:
            var7 = new File(var5, "Library/Application Support/" + var3);
            break;
         default:
            var7 = new File(var5, var3 + '/');
         }

         if(!var7.exists() && !var7.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + var7);
         }

         File var2 = var7;
         this.settings = new GameSettings(this, var7);
         this.textureManager = new TextureManager(this.settings);
         this.textureManager.registerAnimation(new TextureLavaFX());
         this.textureManager.registerAnimation(new TextureWaterFX());
         this.fontRenderer = new FontRenderer(this.settings, "/default.png", this.textureManager);
         IntBuffer var9;
         (var9 = BufferUtils.createIntBuffer(256)).clear().limit(256);
         this.levelRenderer = new LevelRenderer(this, this.textureManager);
         Item.initModels();
         Mob.modelCache = new ModelManager();
         GL11.glViewport(0, 0, this.width, this.height);
         if(this.server != null && this.session != null) {
            Level var85;
            (var85 = new Level()).setData(8, 8, 8, new byte[512]);
            this.setLevel(var85);
         } else {
            boolean var10 = false;

            try {
               if(var1.levelName != null) {
                  var1.loadOnlineLevel(var1.levelName, var1.levelId);
               } else if(!var1.levelLoaded) {
                  Level var11 = null;
                  if((var11 = var1.levelIo.load((InputStream)(new FileInputStream(new File("level.dat"))))) != null) {
                     var1.setLevel(var11);
                  }
               }
            } catch (Exception var54) {
               var54.printStackTrace();
            }

            if(this.level == null) {
               this.generateLevel(1);
            }
         }

         this.particleManager = new ParticleManager(this.level, this.textureManager);
         if(this.levelLoaded) {
            try {
               var1.cursor = new Cursor(16, 16, 0, 0, 1, var9, (IntBuffer)null);
            } catch (LWJGLException var53) {
               var53.printStackTrace();
            }
         }

         try {
            var1.soundPlayer = new SoundPlayer(var1.settings);
            SoundPlayer var4 = var1.soundPlayer;

            try {
               AudioFormat var67 = new AudioFormat(44100.0F, 16, 2, true, true);
               var4.dataLine = AudioSystem.getSourceDataLine(var67);
               var4.dataLine.open(var67, 4410);
               var4.dataLine.start();
               var4.running = true;
               Thread var73;
               (var73 = new Thread(var4)).setDaemon(true);
               var73.setPriority(10);
               var73.start();
            } catch (Exception var51) {
               var51.printStackTrace();
               var4.running = false;
            }

            var1.resourceThread = new ResourceDownloadThread(var2, var1);
            var1.resourceThread.start();
         } catch (Exception var52) {
            ;
         }

         checkGLError("Post startup");
         this.hud = new HUDScreen(this, this.width, this.height);
         (new SkinDownloadThread(this)).start();
         if(this.server != null && this.session != null) {
            this.networkManager = new NetworkManager(this, this.server, this.port, this.session.username, this.session.mppass);
         }
      } catch (Exception var62) {
         var62.printStackTrace();
         JOptionPane.showMessageDialog((Component)null, var62.toString(), "Failed to start Minecraft", 0);
         return;
      }

      long var13 = System.currentTimeMillis();
      int var15 = 0;

      try {
         while(this.running) {
            if(this.waiting) {
               Thread.sleep(100L);
            } else {
               if(this.canvas == null && Display.isCloseRequested()) {
                  this.running = false;
               }

               try {
                  Timer var63 = this.timer;
                  long var16;
                  long var18 = (var16 = System.currentTimeMillis()) - var63.lastSysClock;
                  long var20 = System.nanoTime() / 1000000L;
                  double var24;
                  if(var18 > 1000L) {
                     long var22 = var20 - var63.lastHRClock;
                     var24 = (double)var18 / (double)var22;
                     var63.adjustment += (var24 - var63.adjustment) * 0.20000000298023224D;
                     var63.lastSysClock = var16;
                     var63.lastHRClock = var20;
                  }

                  if(var18 < 0L) {
                     var63.lastSysClock = var16;
                     var63.lastHRClock = var20;
                  }

                  double var95;
                  var24 = ((var95 = (double)var20 / 1000.0D) - var63.lastHR) * var63.adjustment;
                  var63.lastHR = var95;
                  if(var24 < 0.0D) {
                     var24 = 0.0D;
                  }

                  if(var24 > 1.0D) {
                     var24 = 1.0D;
                  }

                  var63.elapsedDelta = (float)((double)var63.elapsedDelta + var24 * (double)var63.speed * (double)var63.tps);
                  var63.elapsedTicks = (int)var63.elapsedDelta;
                  if(var63.elapsedTicks > 100) {
                     var63.elapsedTicks = 100;
                  }

                  var63.elapsedDelta -= (float)var63.elapsedTicks;
                  var63.delta = var63.elapsedDelta;

                  for(int var64 = 0; var64 < this.timer.elapsedTicks; ++var64) {
                     ++this.ticks;
                     this.tick();
                  }

                  checkGLError("Pre render");
                  GL11.glEnable(3553);
                  if(!this.online) {
                     this.gamemode.applyCracks(this.timer.delta);
                     float var65 = this.timer.delta;
                     Renderer var66 = this.renderer;
                     if(this.renderer.displayActive && !Display.isActive()) {
                        var66.minecraft.pause();
                     }

                     var66.displayActive = Display.isActive();
                     int var68;
                     int var70;
                     int var82;
                     int var86;
                     if(var66.minecraft.hasMouse) {
                        var82 = 0;
                        var86 = 0;
                        if(var66.minecraft.levelLoaded) {
                           if(var66.minecraft.canvas != null) {
                              Point var87;
                              var70 = (var87 = var66.minecraft.canvas.getLocationOnScreen()).x + var66.minecraft.width / 2;
                              var68 = var87.y + var66.minecraft.height / 2;
                              Point var74;
                              var82 = (var74 = MouseInfo.getPointerInfo().getLocation()).x - var70;
                              var86 = -(var74.y - var68);
                              var66.minecraft.robot.mouseMove(var70, var68);
                           } else {
                              Mouse.setCursorPosition(var66.minecraft.width / 2, var66.minecraft.height / 2);
                           }
                        } else {
                           var82 = Mouse.getDX();
                           var86 = Mouse.getDY();
                        }

                        byte var89 = 1;
                        if(var66.minecraft.settings.invertMouse) {
                           var89 = -1;
                        }

                        var66.minecraft.player.turn((float)var82, (float)(var86 * var89));
                     }

                     if(!var66.minecraft.online) {
                        var82 = var66.minecraft.width * 240 / var66.minecraft.height;
                        var86 = var66.minecraft.height * 240 / var66.minecraft.height;
                        int var93 = Mouse.getX() * var82 / var66.minecraft.width;
                        var70 = var86 - Mouse.getY() * var86 / var66.minecraft.height - 1;
                        if(var66.minecraft.level != null) {
                           float var79 = var65;
                           Renderer var81 = var66;
                           Renderer var27 = var66;
                           Player var28;
                           float var29 = (var28 = var66.minecraft.player).xRotO + (var28.xRot - var28.xRotO) * var65;
                           float var30 = var28.yRotO + (var28.yRot - var28.yRotO) * var65;
                           Vec3D var31 = var66.getPlayerVector(var65);
                           float var32 = MathHelper.cos(-var30 * 0.017453292F - 3.1415927F);
                           float var69 = MathHelper.sin(-var30 * 0.017453292F - 3.1415927F);
                           float var75 = MathHelper.cos(-var29 * 0.017453292F);
                           float var33 = MathHelper.sin(-var29 * 0.017453292F);
                           float var34 = var69 * var75;
                           float var91 = var32 * var75;
                           float var36 = var66.minecraft.gamemode.getReachDistance();
                           Vec3D var71 = var31.add(var34 * var36, var33 * var36, var91 * var36);
                           var66.minecraft.selected = var66.minecraft.level.clip(var31, var71);
                           var75 = var36;
                           if(var66.minecraft.selected != null) {
                              var75 = var66.minecraft.selected.vec.distance(var66.getPlayerVector(var65));
                           }

                           var31 = var66.getPlayerVector(var65);
                           if(var66.minecraft.gamemode instanceof CreativeGameMode) {
                              var36 = 32.0F;
                           } else {
                              var36 = var75;
                           }

                           var71 = var31.add(var34 * var36, var33 * var36, var91 * var36);
                           var66.entity = null;
                           List var37 = var66.minecraft.level.blockMap.getEntities(var28, var28.bb.expand(var34 * var36, var33 * var36, var91 * var36));
                           float var35 = 0.0F;

                           for(var82 = 0; var82 < var37.size(); ++var82) {
                              Entity var92;
                              if((var92 = (Entity)var37.get(var82)).isPickable()) {
                                 var75 = 0.1F;
                                 MovingObjectPosition var78;
                                 if((var78 = var92.bb.grow(var75, var75, var75).clip(var31, var71)) != null && ((var75 = var31.distance(var78.vec)) < var35 || var35 == 0.0F)) {
                                    var27.entity = var92;
                                    var35 = var75;
                                 }
                              }
                           }

                           if(var27.entity != null && !(var27.minecraft.gamemode instanceof CreativeGameMode)) {
                              var27.minecraft.selected = new MovingObjectPosition(var27.entity);
                           }

                           int var76 = 0;

                           while(true) {
                              if(var76 >= 2) {
                                 GL11.glColorMask(true, true, true, false);
                                 break;
                              }

                              if(var81.minecraft.settings.anaglyph) {
                                 if(var76 == 0) {
                                    GL11.glColorMask(false, true, true, false);
                                 } else {
                                    GL11.glColorMask(true, false, false, false);
                                 }
                              }

                              Player var123 = var81.minecraft.player;
                              Level var121 = var81.minecraft.level;
                              LevelRenderer var88 = var81.minecraft.levelRenderer;
                              ParticleManager var94 = var81.minecraft.particleManager;
                              GL11.glViewport(0, 0, var81.minecraft.width, var81.minecraft.height);
                              Level var26 = var81.minecraft.level;
                              var28 = var81.minecraft.player;
                              var29 = 1.0F / (float)(4 - var81.minecraft.settings.viewDistance);
                              var29 = 1.0F - (float)Math.pow((double)var29, 0.25D);
                              var30 = (float)(var26.skyColor >> 16 & 255) / 255.0F;
                              float var108 = (float)(var26.skyColor >> 8 & 255) / 255.0F;
                              var32 = (float)(var26.skyColor & 255) / 255.0F;
                              var81.fogRed = (float)(var26.fogColor >> 16 & 255) / 255.0F;
                              var81.fogBlue = (float)(var26.fogColor >> 8 & 255) / 255.0F;
                              var81.fogGreen = (float)(var26.fogColor & 255) / 255.0F;
                              var81.fogRed += (var30 - var81.fogRed) * var29;
                              var81.fogBlue += (var108 - var81.fogBlue) * var29;
                              var81.fogGreen += (var32 - var81.fogGreen) * var29;
                              var81.fogRed *= var81.fogColorMultiplier;
                              var81.fogBlue *= var81.fogColorMultiplier;
                              var81.fogGreen *= var81.fogColorMultiplier;
                              Block var72;
                              if((var72 = Block.blocks[var26.getTile((int)var28.x, (int)(var28.y + 0.12F), (int)var28.z)]) != null && var72.getLiquidType() != LiquidType.NOT_LIQUID) {
                                 LiquidType var80;
                                 if((var80 = var72.getLiquidType()) == LiquidType.WATER) {
                                    var81.fogRed = 0.02F;
                                    var81.fogBlue = 0.02F;
                                    var81.fogGreen = 0.2F;
                                 } else if(var80 == LiquidType.LAVA) {
                                    var81.fogRed = 0.6F;
                                    var81.fogBlue = 0.1F;
                                    var81.fogGreen = 0.0F;
                                 }
                              }

                              if(var81.minecraft.settings.anaglyph) {
                                 var75 = (var81.fogRed * 30.0F + var81.fogBlue * 59.0F + var81.fogGreen * 11.0F) / 100.0F;
                                 var33 = (var81.fogRed * 30.0F + var81.fogBlue * 70.0F) / 100.0F;
                                 var34 = (var81.fogRed * 30.0F + var81.fogGreen * 70.0F) / 100.0F;
                                 var81.fogRed = var75;
                                 var81.fogBlue = var33;
                                 var81.fogGreen = var34;
                              }

                              GL11.glClearColor(var81.fogRed, var81.fogBlue, var81.fogGreen, 0.0F);
                              GL11.glClear(16640);
                              var81.fogColorMultiplier = 1.0F;
                              GL11.glEnable(2884);
                              var81.fogEnd = (float)(512 >> (var81.minecraft.settings.viewDistance << 1));
                              GL11.glMatrixMode(5889);
                              GL11.glLoadIdentity();
                              var29 = 0.07F;
                              if(var81.minecraft.settings.anaglyph) {
                                 GL11.glTranslatef((float)(-((var76 << 1) - 1)) * var29, 0.0F, 0.0F);
                              }

                              Player var111 = var81.minecraft.player;
                              var69 = 70.0F;
                              if(var111.health <= 0) {
                                 var75 = (float)var111.deathTime + var79;
                                 var69 /= (1.0F - 500.0F / (var75 + 500.0F)) * 2.0F + 1.0F;
                              }

                              GLU.gluPerspective(var69, (float)var81.minecraft.width / (float)var81.minecraft.height, 0.05F, var81.fogEnd);
                              GL11.glMatrixMode(5888);
                              GL11.glLoadIdentity();
                              if(var81.minecraft.settings.anaglyph) {
                                 GL11.glTranslatef((float)((var76 << 1) - 1) * 0.1F, 0.0F, 0.0F);
                              }

                              var81.hurtEffect(var79);
                              if(var81.minecraft.settings.viewBobbing) {
                                 var81.applyBobbing(var79);
                              }

                              var111 = var81.minecraft.player;
                              GL11.glTranslatef(0.0F, 0.0F, -0.1F);
                              GL11.glRotatef(var111.xRotO + (var111.xRot - var111.xRotO) * var79, 1.0F, 0.0F, 0.0F);
                              GL11.glRotatef(var111.yRotO + (var111.yRot - var111.yRotO) * var79, 0.0F, 1.0F, 0.0F);
                              var69 = var111.xo + (var111.x - var111.xo) * var79;
                              var75 = var111.yo + (var111.y - var111.yo) * var79;
                              var33 = var111.zo + (var111.z - var111.zo) * var79;
                              GL11.glTranslatef(-var69, -var75, -var33);
                              Frustrum var77 = FrustrumImpl.update();
                              Frustrum var96 = var77;
                              LevelRenderer var99 = var81.minecraft.levelRenderer;

                              int var103;
                              for(var103 = 0; var103 < var99.chunkCache.length; ++var103) {
                                 var99.chunkCache[var103].clip(var96);
                              }

                              var99 = var81.minecraft.levelRenderer;
                              Collections.sort(var81.minecraft.levelRenderer.chunks, new ChunkDirtyDistanceComparator(var123));
                              var103 = var99.chunks.size() - 1;
                              int var105;
                              if((var105 = var99.chunks.size()) > 3) {
                                 var105 = 3;
                              }

                              int var106;
                              for(var106 = 0; var106 < var105; ++var106) {
                                 Chunk var109;
                                 (var109 = (Chunk)var99.chunks.remove(var103 - var106)).update();
                                 var109.loaded = false;
                              }

                              var81.updateFog();
                              GL11.glEnable(2912);
                              var88.sortChunks(var123, 0);
                              int var83;
                              ShapeRenderer var110;
                              int var113;
                              int var116;
                              int var119;
                              int var122;
                              int var124;
                              if(var121.isSolid(var123.x, var123.y, var123.z, 0.1F)) {
                                 var122 = (int)var123.x;
                                 var83 = (int)var123.y;
                                 var116 = (int)var123.z;

                                 for(var119 = var122 - 1; var119 <= var122 + 1; ++var119) {
                                    for(var124 = var83 - 1; var124 <= var83 + 1; ++var124) {
                                       for(int var38 = var116 - 1; var38 <= var116 + 1; ++var38) {
                                          var105 = var38;
                                          var103 = var124;
                                          int var97 = var119;
                                          if((var106 = var88.level.getTile(var119, var124, var38)) != 0 && Block.blocks[var106].isSolid()) {
                                             GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
                                             GL11.glDepthFunc(513);
                                             var110 = ShapeRenderer.instance;
                                             ShapeRenderer.instance.begin();

                                             for(var113 = 0; var113 < 6; ++var113) {
                                                Block.blocks[var106].renderInside(var110, var97, var103, var105, var113);
                                             }

                                             var110.end();
                                             GL11.glCullFace(1028);
                                             var110.begin();

                                             for(var113 = 0; var113 < 6; ++var113) {
                                                Block.blocks[var106].renderInside(var110, var97, var103, var105, var113);
                                             }

                                             var110.end();
                                             GL11.glCullFace(1029);
                                             GL11.glDepthFunc(515);
                                          }
                                       }
                                    }
                                 }
                              }

                              var81.setLighting(true);
                              Vec3D var98 = var81.getPlayerVector(var79);
                              var88.level.blockMap.render(var98, var77, var88.textureManager, var79);
                              var81.setLighting(false);
                              var81.updateFog();
                              float var104 = var79;
                              ParticleManager var101 = var94;
                              var29 = -MathHelper.cos(var123.yRot * 3.1415927F / 180.0F);
                              var108 = -(var30 = -MathHelper.sin(var123.yRot * 3.1415927F / 180.0F)) * MathHelper.sin(var123.xRot * 3.1415927F / 180.0F);
                              var32 = var29 * MathHelper.sin(var123.xRot * 3.1415927F / 180.0F);
                              var69 = MathHelper.cos(var123.xRot * 3.1415927F / 180.0F);

                              for(var83 = 0; var83 < 2; ++var83) {
                                 if(var101.particles[var83].size() != 0) {
                                    var116 = 0;
                                    if(var83 == 0) {
                                       var116 = var101.textureManager.load("/particles.png");
                                    }

                                    if(var83 == 1) {
                                       var116 = var101.textureManager.load("/terrain.png");
                                    }

                                    GL11.glBindTexture(3553, var116);
                                    ShapeRenderer var120 = ShapeRenderer.instance;
                                    ShapeRenderer.instance.begin();

                                    for(var122 = 0; var122 < var101.particles[var83].size(); ++var122) {
                                       ((Particle)var101.particles[var83].get(var122)).render(var120, var104, var29, var69, var30, var108, var32);
                                    }

                                    var120.end();
                                 }
                              }

                              GL11.glBindTexture(3553, var88.textureManager.load("/rock.png"));
                              GL11.glEnable(3553);
                              GL11.glCallList(var88.listId);
                              var81.updateFog();
                              var99 = var88;
                              GL11.glBindTexture(3553, var88.textureManager.load("/clouds.png"));
                              GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                              var104 = (float)(var88.level.cloudColor >> 16 & 255) / 255.0F;
                              var29 = (float)(var88.level.cloudColor >> 8 & 255) / 255.0F;
                              var30 = (float)(var88.level.cloudColor & 255) / 255.0F;
                              if(var88.minecraft.settings.anaglyph) {
                                 var108 = (var104 * 30.0F + var29 * 59.0F + var30 * 11.0F) / 100.0F;
                                 var32 = (var104 * 30.0F + var29 * 70.0F) / 100.0F;
                                 var69 = (var104 * 30.0F + var30 * 70.0F) / 100.0F;
                                 var104 = var108;
                                 var29 = var32;
                                 var30 = var69;
                              }

                              var110 = ShapeRenderer.instance;
                              var75 = 0.0F;
                              var33 = 4.8828125E-4F;
                              var75 = (float)(var88.level.depth + 2);
                              var34 = ((float)var88.ticks + var79) * var33 * 0.03F;
                              var35 = 0.0F;
                              var110.begin();
                              var110.color(var104, var29, var30);

                              for(var86 = -2048; var86 < var99.level.width + 2048; var86 += 512) {
                                 for(var124 = -2048; var124 < var99.level.height + 2048; var124 += 512) {
                                    var110.vertexUV((float)var86, var75, (float)(var124 + 512), (float)var86 * var33 + var34, (float)(var124 + 512) * var33);
                                    var110.vertexUV((float)(var86 + 512), var75, (float)(var124 + 512), (float)(var86 + 512) * var33 + var34, (float)(var124 + 512) * var33);
                                    var110.vertexUV((float)(var86 + 512), var75, (float)var124, (float)(var86 + 512) * var33 + var34, (float)var124 * var33);
                                    var110.vertexUV((float)var86, var75, (float)var124, (float)var86 * var33 + var34, (float)var124 * var33);
                                    var110.vertexUV((float)var86, var75, (float)var124, (float)var86 * var33 + var34, (float)var124 * var33);
                                    var110.vertexUV((float)(var86 + 512), var75, (float)var124, (float)(var86 + 512) * var33 + var34, (float)var124 * var33);
                                    var110.vertexUV((float)(var86 + 512), var75, (float)(var124 + 512), (float)(var86 + 512) * var33 + var34, (float)(var124 + 512) * var33);
                                    var110.vertexUV((float)var86, var75, (float)(var124 + 512), (float)var86 * var33 + var34, (float)(var124 + 512) * var33);
                                 }
                              }

                              var110.end();
                              GL11.glDisable(3553);
                              var110.begin();
                              var34 = (float)(var99.level.skyColor >> 16 & 255) / 255.0F;
                              var35 = (float)(var99.level.skyColor >> 8 & 255) / 255.0F;
                              var91 = (float)(var99.level.skyColor & 255) / 255.0F;
                              if(var99.minecraft.settings.anaglyph) {
                                 var36 = (var34 * 30.0F + var35 * 59.0F + var91 * 11.0F) / 100.0F;
                                 var69 = (var34 * 30.0F + var35 * 70.0F) / 100.0F;
                                 var75 = (var34 * 30.0F + var91 * 70.0F) / 100.0F;
                                 var34 = var36;
                                 var35 = var69;
                                 var91 = var75;
                              }

                              var110.color(var34, var35, var91);
                              var75 = (float)(var99.level.depth + 10);

                              for(var124 = -2048; var124 < var99.level.width + 2048; var124 += 512) {
                                 for(var68 = -2048; var68 < var99.level.height + 2048; var68 += 512) {
                                    var110.vertex((float)var124, var75, (float)var68);
                                    var110.vertex((float)(var124 + 512), var75, (float)var68);
                                    var110.vertex((float)(var124 + 512), var75, (float)(var68 + 512));
                                    var110.vertex((float)var124, var75, (float)(var68 + 512));
                                 }
                              }

                              var110.end();
                              GL11.glEnable(3553);
                              var81.updateFog();
                              int var114;
                              if(var81.minecraft.selected != null) {
                                 GL11.glDisable(3008);
                                 MovingObjectPosition var10001 = var81.minecraft.selected;
                                 var105 = var123.inventory.getSelected();
                                 boolean var107 = false;
                                 MovingObjectPosition var100 = var10001;
                                 var99 = var88;
                                 ShapeRenderer var112 = ShapeRenderer.instance;
                                 GL11.glEnable(3042);
                                 GL11.glEnable(3008);
                                 GL11.glBlendFunc(770, 1);
                                 GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin((float)System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
                                 if(var88.cracks > 0.0F) {
                                    GL11.glBlendFunc(774, 768);
                                    var114 = var88.textureManager.load("/terrain.png");
                                    GL11.glBindTexture(3553, var114);
                                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
                                    GL11.glPushMatrix();
                                    Block var10000 = (var113 = var88.level.getTile(var100.x, var100.y, var100.z)) > 0?Block.blocks[var113]:null;
                                    var72 = var10000;
                                    var75 = (var10000.x1 + var72.x2) / 2.0F;
                                    var33 = (var72.y1 + var72.y2) / 2.0F;
                                    var34 = (var72.z1 + var72.z2) / 2.0F;
                                    GL11.glTranslatef((float)var100.x + var75, (float)var100.y + var33, (float)var100.z + var34);
                                    var35 = 1.01F;
                                    GL11.glScalef(1.01F, var35, var35);
                                    GL11.glTranslatef(-((float)var100.x + var75), -((float)var100.y + var33), -((float)var100.z + var34));
                                    var112.begin();
                                    var112.noColor();
                                    GL11.glDepthMask(false);
                                    if(var72 == null) {
                                       var72 = Block.STONE;
                                    }

                                    for(var86 = 0; var86 < 6; ++var86) {
                                       var72.renderSide(var112, var100.x, var100.y, var100.z, var86, 240 + (int)(var99.cracks * 10.0F));
                                    }

                                    var112.end();
                                    GL11.glDepthMask(true);
                                    GL11.glPopMatrix();
                                 }

                                 GL11.glDisable(3042);
                                 GL11.glDisable(3008);
                                 var10001 = var81.minecraft.selected;
                                 var123.inventory.getSelected();
                                 var107 = false;
                                 var100 = var10001;
                                 GL11.glEnable(3042);
                                 GL11.glBlendFunc(770, 771);
                                 GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
                                 GL11.glLineWidth(2.0F);
                                 GL11.glDisable(3553);
                                 GL11.glDepthMask(false);
                                 var29 = 0.002F;
                                 if((var106 = var88.level.getTile(var100.x, var100.y, var100.z)) > 0) {
                                    AABB var117 = Block.blocks[var106].getSelectionBox(var100.x, var100.y, var100.z).grow(var29, var29, var29);
                                    GL11.glBegin(3);
                                    GL11.glVertex3f(var117.x0, var117.y0, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y0, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y0, var117.z1);
                                    GL11.glVertex3f(var117.x0, var117.y0, var117.z1);
                                    GL11.glVertex3f(var117.x0, var117.y0, var117.z0);
                                    GL11.glEnd();
                                    GL11.glBegin(3);
                                    GL11.glVertex3f(var117.x0, var117.y1, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y1, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y1, var117.z1);
                                    GL11.glVertex3f(var117.x0, var117.y1, var117.z1);
                                    GL11.glVertex3f(var117.x0, var117.y1, var117.z0);
                                    GL11.glEnd();
                                    GL11.glBegin(1);
                                    GL11.glVertex3f(var117.x0, var117.y0, var117.z0);
                                    GL11.glVertex3f(var117.x0, var117.y1, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y0, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y1, var117.z0);
                                    GL11.glVertex3f(var117.x1, var117.y0, var117.z1);
                                    GL11.glVertex3f(var117.x1, var117.y1, var117.z1);
                                    GL11.glVertex3f(var117.x0, var117.y0, var117.z1);
                                    GL11.glVertex3f(var117.x0, var117.y1, var117.z1);
                                    GL11.glEnd();
                                 }

                                 GL11.glDepthMask(true);
                                 GL11.glEnable(3553);
                                 GL11.glDisable(3042);
                                 GL11.glEnable(3008);
                              }

                              GL11.glBlendFunc(770, 771);
                              var81.updateFog();
                              GL11.glEnable(3553);
                              GL11.glEnable(3042);
                              GL11.glBindTexture(3553, var88.textureManager.load("/water.png"));
                              GL11.glCallList(var88.listId + 1);
                              GL11.glDisable(3042);
                              GL11.glEnable(3042);
                              GL11.glColorMask(false, false, false, false);
                              var122 = var88.sortChunks(var123, 1);
                              GL11.glColorMask(true, true, true, true);
                              if(var81.minecraft.settings.anaglyph) {
                                 if(var76 == 0) {
                                    GL11.glColorMask(false, true, true, false);
                                 } else {
                                    GL11.glColorMask(true, false, false, false);
                                 }
                              }

                              if(var122 > 0) {
                                 GL11.glBindTexture(3553, var88.textureManager.load("/terrain.png"));
                                 GL11.glCallLists(var88.buffer);
                              }

                              GL11.glDepthMask(true);
                              GL11.glDisable(3042);
                              GL11.glDisable(2912);
                              if(var81.minecraft.raining) {
                                 float var102 = var79;
                                 var27 = var81;
                                 var28 = var81.minecraft.player;
                                 Level var115 = var81.minecraft.level;
                                 var106 = (int)var28.x;
                                 var114 = (int)var28.y;
                                 var113 = (int)var28.z;
                                 ShapeRenderer var84 = ShapeRenderer.instance;
                                 GL11.glDisable(2884);
                                 GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                                 GL11.glEnable(3042);
                                 GL11.glBlendFunc(770, 771);
                                 GL11.glBindTexture(3553, var81.minecraft.textureManager.load("/rain.png"));

                                 for(var116 = var106 - 5; var116 <= var106 + 5; ++var116) {
                                    for(var119 = var113 - 5; var119 <= var113 + 5; ++var119) {
                                       var122 = var115.getHighestTile(var116, var119);
                                       var86 = var114 - 5;
                                       var124 = var114 + 5;
                                       if(var86 < var122) {
                                          var86 = var122;
                                       }

                                       if(var124 < var122) {
                                          var124 = var122;
                                       }

                                       if(var86 != var124) {
                                          var75 = ((float)((var27.levelTicks + var116 * 3121 + var119 * 418711) % 32) + var102) / 32.0F;
                                          float var126 = (float)var116 + 0.5F - var28.x;
                                          var35 = (float)var119 + 0.5F - var28.z;
                                          float var90 = MathHelper.sqrt(var126 * var126 + var35 * var35) / (float)5;
                                          GL11.glColor4f(1.0F, 1.0F, 1.0F, (1.0F - var90 * var90) * 0.7F);
                                          var84.begin();
                                          var84.vertexUV((float)var116, (float)var86, (float)var119, 0.0F, (float)var86 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)(var116 + 1), (float)var86, (float)(var119 + 1), 2.0F, (float)var86 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)(var116 + 1), (float)var124, (float)(var119 + 1), 2.0F, (float)var124 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)var116, (float)var124, (float)var119, 0.0F, (float)var124 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)var116, (float)var86, (float)(var119 + 1), 0.0F, (float)var86 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)(var116 + 1), (float)var86, (float)var119, 2.0F, (float)var86 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)(var116 + 1), (float)var124, (float)var119, 2.0F, (float)var124 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.vertexUV((float)var116, (float)var124, (float)(var119 + 1), 0.0F, (float)var124 * 2.0F / 8.0F + var75 * 2.0F);
                                          var84.end();
                                       }
                                    }
                                 }

                                 GL11.glEnable(2884);
                                 GL11.glDisable(3042);
                              }

                              if(var81.entity != null) {
                                 var81.entity.renderHover(var81.minecraft.textureManager, var79);
                              }

                              GL11.glClear(256);
                              GL11.glLoadIdentity();
                              if(var81.minecraft.settings.anaglyph) {
                                 GL11.glTranslatef((float)((var76 << 1) - 1) * 0.1F, 0.0F, 0.0F);
                              }

                              var81.hurtEffect(var79);
                              if(var81.minecraft.settings.viewBobbing) {
                                 var81.applyBobbing(var79);
                              }

                              HeldBlock var118 = var81.heldBlock;
                              var108 = var81.heldBlock.lastPos + (var118.pos - var118.lastPos) * var79;
                              var111 = var118.minecraft.player;
                              GL11.glPushMatrix();
                              GL11.glRotatef(var111.xRotO + (var111.xRot - var111.xRotO) * var79, 1.0F, 0.0F, 0.0F);
                              GL11.glRotatef(var111.yRotO + (var111.yRot - var111.yRotO) * var79, 0.0F, 1.0F, 0.0F);
                              var118.minecraft.renderer.setLighting(true);
                              GL11.glPopMatrix();
                              GL11.glPushMatrix();
                              var69 = 0.8F;
                              if(var118.moving) {
                                 var33 = MathHelper.sin((var75 = ((float)var118.offset + var79) / 7.0F) * 3.1415927F);
                                 GL11.glTranslatef(-MathHelper.sin(MathHelper.sqrt(var75) * 3.1415927F) * 0.4F, MathHelper.sin(MathHelper.sqrt(var75) * 3.1415927F * 2.0F) * 0.2F, -var33 * 0.2F);
                              }

                              GL11.glTranslatef(0.7F * var69, -0.65F * var69 - (1.0F - var108) * 0.6F, -0.9F * var69);
                              GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                              GL11.glEnable(2977);
                              if(var118.moving) {
                                 var33 = MathHelper.sin((var75 = ((float)var118.offset + var79) / 7.0F) * var75 * 3.1415927F);
                                 GL11.glRotatef(MathHelper.sin(MathHelper.sqrt(var75) * 3.1415927F) * 80.0F, 0.0F, 1.0F, 0.0F);
                                 GL11.glRotatef(-var33 * 20.0F, 1.0F, 0.0F, 0.0F);
                              }

                              GL11.glColor4f(var75 = var118.minecraft.level.getBrightness((int)var111.x, (int)var111.y, (int)var111.z), var75, var75, 1.0F);
                              ShapeRenderer var125 = ShapeRenderer.instance;
                              if(var118.block != null) {
                                 var34 = 0.4F;
                                 GL11.glScalef(0.4F, var34, var34);
                                 GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                                 GL11.glBindTexture(3553, var118.minecraft.textureManager.load("/terrain.png"));
                                 var118.block.renderPreview(var125);
                              } else {
                                 var111.bindTexture(var118.minecraft.textureManager);
                                 GL11.glScalef(1.0F, -1.0F, -1.0F);
                                 GL11.glTranslatef(0.0F, 0.2F, 0.0F);
                                 GL11.glRotatef(-120.0F, 0.0F, 0.0F, 1.0F);
                                 GL11.glScalef(1.0F, 1.0F, 1.0F);
                                 var34 = 0.0625F;
                                 ModelPart var127;
                                 if(!(var127 = var118.minecraft.player.getModel().leftArm).hasList) {
                                    var127.generateList(var34);
                                 }

                                 GL11.glCallList(var127.list);
                              }

                              GL11.glDisable(2977);
                              GL11.glPopMatrix();
                              var118.minecraft.renderer.setLighting(false);
                              if(!var81.minecraft.settings.anaglyph) {
                                 break;
                              }

                              ++var76;
                           }

                           var66.minecraft.hud.render(var65, var66.minecraft.currentScreen != null, var93, var70);
                        } else {
                           GL11.glViewport(0, 0, var66.minecraft.width, var66.minecraft.height);
                           GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                           GL11.glClear(16640);
                           GL11.glMatrixMode(5889);
                           GL11.glLoadIdentity();
                           GL11.glMatrixMode(5888);
                           GL11.glLoadIdentity();
                           var66.enableGuiMode();
                        }

                        if(var66.minecraft.currentScreen != null) {
                           var66.minecraft.currentScreen.render(var93, var70);
                        }

                        Thread.yield();
                        Display.update();
                     }
                  }

                  if(this.settings.limitFramerate) {
                     Thread.sleep(5L);
                  }

                  checkGLError("Post render");
                  ++var15;
               } catch (Exception var58) {
                  this.setCurrentScreen(new ErrorScreen("Client error", "The game broke! [" + var58 + "]"));
                  var58.printStackTrace();
               }

               while(System.currentTimeMillis() >= var13 + 1000L) {
                  this.debug = var15 + " fps, " + Chunk.chunkUpdates + " chunk updates";
                  Chunk.chunkUpdates = 0;
                  var13 += 1000L;
                  var15 = 0;
               }
            }
         }

         return;
      } catch (StopGameException var59) {
         return;
      } catch (Exception var60) {
         var60.printStackTrace();
      } finally {
         this.shutdown();
      }

   }

   public final void grabMouse() {
      if(!this.hasMouse) {
         this.hasMouse = true;
         if(this.levelLoaded) {
            try {
               Mouse.setNativeCursor(this.cursor);
               Mouse.setCursorPosition(this.width / 2, this.height / 2);
            } catch (LWJGLException var2) {
               var2.printStackTrace();
            }

            if(this.canvas == null) {
               this.canvas.requestFocus();
            }
         } else {
            Mouse.setGrabbed(true);
         }

         this.setCurrentScreen((GuiScreen)null);
         this.lastClick = this.ticks + 10000;
      }
   }

   public final void pause() {
      if(this.currentScreen == null) {
         this.setCurrentScreen(new PauseScreen());
      }
   }

   private void onMouseClick(int var1) {
      if(var1 != 0 || this.blockHitTime <= 0) {
         HeldBlock var2;
         if(var1 == 0) {
            var2 = this.renderer.heldBlock;
            this.renderer.heldBlock.offset = -1;
            var2.moving = true;
         }

         int var3;
         if(var1 == 1 && (var3 = this.player.inventory.getSelected()) > 0 && this.gamemode.useItem(this.player, var3)) {
            var2 = this.renderer.heldBlock;
            this.renderer.heldBlock.pos = 0.0F;
         } else if(this.selected == null) {
            if(var1 == 0 && !(this.gamemode instanceof CreativeGameMode)) {
               this.blockHitTime = 10;
            }

         } else {
            if(this.selected.entityPos == 1) {
               if(var1 == 0) {
                  this.selected.entity.hurt(this.player, 4);
                  return;
               }
            } else if(this.selected.entityPos == 0) {
               var3 = this.selected.x;
               int var4 = this.selected.y;
               int var5 = this.selected.z;
               if(var1 != 0) {
                  if(this.selected.face == 0) {
                     --var4;
                  }

                  if(this.selected.face == 1) {
                     ++var4;
                  }

                  if(this.selected.face == 2) {
                     --var5;
                  }

                  if(this.selected.face == 3) {
                     ++var5;
                  }

                  if(this.selected.face == 4) {
                     --var3;
                  }

                  if(this.selected.face == 5) {
                     ++var3;
                  }
               }

               Block var6 = Block.blocks[this.level.getTile(var3, var4, var5)];
               if(var1 == 0) {
                  if(var6 != Block.BEDROCK || this.player.userType >= 100) {
                     this.gamemode.hitBlock(var3, var4, var5);
                     return;
                  }
               } else {
                  int var10;
                  if((var10 = this.player.inventory.getSelected()) <= 0) {
                     return;
                  }

                  Block var8;
                  AABB var9;
                  if(((var8 = Block.blocks[this.level.getTile(var3, var4, var5)]) == null || var8 == Block.WATER || var8 == Block.STATIONARY_WATER || var8 == Block.LAVA || var8 == Block.STATIONARY_LAVA) && ((var9 = Block.blocks[var10].getCollisionBox(var3, var4, var5)) == null || (this.player.bb.intersects(var9)?false:this.level.isFree(var9)))) {
                     if(!this.gamemode.canPlace(var10)) {
                        return;
                     }

                     if(this.isOnline()) {
                        this.networkManager.sendBlockChange(var3, var4, var5, var1, var10);
                     }

                     this.level.netSetTile(var3, var4, var5, var10);
                     var2 = this.renderer.heldBlock;
                     this.renderer.heldBlock.pos = 0.0F;
                     Block.blocks[var10].onPlace(this.level, var3, var4, var5);
                  }
               }
            }

         }
      }
   }

   private void tick() {
      if(this.soundPlayer != null) {
         SoundPlayer var1 = this.soundPlayer;
         SoundManager var2 = this.sound;
         if(System.currentTimeMillis() > var2.lastMusic && var2.playMusic(var1, "calm")) {
            var2.lastMusic = System.currentTimeMillis() + (long)var2.random.nextInt(900000) + 300000L;
         }
      }

      this.gamemode.spawnMob();
      HUDScreen var17 = this.hud;
      ++this.hud.ticks;

      int var16;
      for(var16 = 0; var16 < var17.chat.size(); ++var16) {
         ++((ChatLine)var17.chat.get(var16)).time;
      }

      GL11.glBindTexture(3553, this.textureManager.load("/terrain.png"));
      TextureManager var18 = this.textureManager;

      for(var16 = 0; var16 < var18.animations.size(); ++var16) {
         TextureFX var3;
         (var3 = (TextureFX)var18.animations.get(var16)).anaglyph = var18.settings.anaglyph;
         var3.animate();
         var18.textureBuffer.clear();
         var18.textureBuffer.put(var3.textureData);
         var18.textureBuffer.position(0).limit(var3.textureData.length);
         GL11.glTexSubImage2D(3553, 0, var3.textureId % 16 << 4, var3.textureId / 16 << 4, 16, 16, 6408, 5121, var18.textureBuffer);
      }

      int var4;
      int var8;
      int var40;
      int var46;
      int var47;
      if(this.networkManager != null && !(this.currentScreen instanceof ErrorScreen)) {
         if(!this.networkManager.isConnected()) {
            this.progressBar.setTitle("Connecting..");
            this.progressBar.setProgress(0);
         } else {
            NetworkManager var20 = this.networkManager;
            if(this.networkManager.successful) {
               NetworkHandler var19 = var20.netHandler;
               if(var20.netHandler.connected) {
                  try {
                     NetworkHandler var22 = var20.netHandler;
                     var20.netHandler.channel.read(var22.in);
                     var4 = 0;

                     while(var22.in.position() > 0 && var4++ != 100) {
                        var22.in.flip();
                        byte var5 = var22.in.get(0);
                        PacketType var6;
                        if((var6 = PacketType.packets[var5]) == null) {
                           throw new IOException("Bad command: " + var5);
                        }

                        if(var22.in.remaining() < var6.length + 1) {
                           var22.in.compact();
                           break;
                        }

                        var22.in.get();
                        Object[] var7 = new Object[var6.params.length];

                        for(var8 = 0; var8 < var7.length; ++var8) {
                           var7[var8] = var22.readObject(var6.params[var8]);
                        }

                        NetworkManager var44 = var22.netManager;
                        if(var22.netManager.successful) {
                           if(var6 == PacketType.IDENTIFICATION) {
                              var44.minecraft.progressBar.setTitle(var7[1].toString());
                              var44.minecraft.progressBar.setText(var7[2].toString());
                              var44.minecraft.player.userType = ((Byte)var7[3]).byteValue();
                           } else if(var6 == PacketType.LEVEL_INIT) {
                              var44.minecraft.setLevel((Level)null);
                              var44.levelData = new ByteArrayOutputStream();
                           } else if(var6 == PacketType.LEVEL_DATA) {
                              short var11 = ((Short)var7[0]).shortValue();
                              byte[] var12 = (byte[])((byte[])var7[1]);
                              byte var13 = ((Byte)var7[2]).byteValue();
                              var44.minecraft.progressBar.setProgress(var13);
                              var44.levelData.write(var12, 0, var11);
                           } else if(var6 == PacketType.LEVEL_FINALIZE) {
                              try {
                                 var44.levelData.close();
                              } catch (IOException var14) {
                                 var14.printStackTrace();
                              }

                              byte[] var54 = LevelIO.decompress(new ByteArrayInputStream(var44.levelData.toByteArray()));
                              var44.levelData = null;
                              short var59 = ((Short)var7[0]).shortValue();
                              short var62 = ((Short)var7[1]).shortValue();
                              short var21 = ((Short)var7[2]).shortValue();
                              Level var31;
                              (var31 = new Level()).setNetworkMode(true);
                              var31.setData(var59, var62, var21, var54);
                              var44.minecraft.setLevel(var31);
                              var44.minecraft.online = false;
                              var44.levelLoaded = true;
                           } else if(var6 == PacketType.BLOCK_CHANGE) {
                              if(var44.minecraft.level != null) {
                                 var44.minecraft.level.netSetTile(((Short)var7[0]).shortValue(), ((Short)var7[1]).shortValue(), ((Short)var7[2]).shortValue(), ((Byte)var7[3]).byteValue());
                              }
                           } else {
                              byte var9;
                              byte var10001;
                              short var10003;
                              short var10004;
                              String var33;
                              NetworkPlayer var34;
                              short var37;
                              short var45;
                              if(var6 == PacketType.SPAWN_PLAYER) {
                                 var10001 = ((Byte)var7[0]).byteValue();
                                 String var10002 = (String)var7[1];
                                 var10003 = ((Short)var7[2]).shortValue();
                                 var10004 = ((Short)var7[3]).shortValue();
                                 short var10005 = ((Short)var7[4]).shortValue();
                                 byte var10006 = ((Byte)var7[5]).byteValue();
                                 byte var55 = ((Byte)var7[6]).byteValue();
                                 var9 = var10006;
                                 short var10 = var10005;
                                 var45 = var10004;
                                 var37 = var10003;
                                 var33 = var10002;
                                 var5 = var10001;
                                 if(var5 >= 0) {
                                    var9 = (byte)(var9 + 128);
                                    var45 = (short)(var45 - 22);
                                    var34 = new NetworkPlayer(var44.minecraft, var5, var33, var37, var45, var10, (float)(var9 * 360) / 256.0F, (float)(var55 * 360) / 256.0F);
                                    var44.players.put(Byte.valueOf(var5), var34);
                                    var44.minecraft.level.addEntity(var34);
                                 } else {
                                    var44.minecraft.level.setSpawnPos(var37 / 32, var45 / 32, var10 / 32, (float)(var9 * 320 / 256));
                                    var44.minecraft.player.moveTo((float)var37 / 32.0F, (float)var45 / 32.0F, (float)var10 / 32.0F, (float)(var9 * 360) / 256.0F, (float)(var55 * 360) / 256.0F);
                                 }
                              } else {
                                 byte var51;
                                 NetworkPlayer var58;
                                 byte var69;
                                 if(var6 == PacketType.POSITION_ROTATION) {
                                    var10001 = ((Byte)var7[0]).byteValue();
                                    short var65 = ((Short)var7[1]).shortValue();
                                    var10003 = ((Short)var7[2]).shortValue();
                                    var10004 = ((Short)var7[3]).shortValue();
                                    var69 = ((Byte)var7[4]).byteValue();
                                    var9 = ((Byte)var7[5]).byteValue();
                                    var51 = var69;
                                    var45 = var10004;
                                    var37 = var10003;
                                    short var35 = var65;
                                    var5 = var10001;
                                    if(var5 < 0) {
                                       var44.minecraft.player.moveTo((float)var35 / 32.0F, (float)var37 / 32.0F, (float)var45 / 32.0F, (float)(var51 * 360) / 256.0F, (float)(var9 * 360) / 256.0F);
                                    } else {
                                       var51 = (byte)(var51 + 128);
                                       var37 = (short)(var37 - 22);
                                       if((var58 = (NetworkPlayer)var44.players.get(Byte.valueOf(var5))) != null) {
                                          var58.teleport(var35, var37, var45, (float)(var51 * 360) / 256.0F, (float)(var9 * 360) / 256.0F);
                                       }
                                    }
                                 } else {
                                    byte var38;
                                    byte var42;
                                    byte var49;
                                    byte var66;
                                    byte var67;
                                    if(var6 == PacketType.POSITION_ROTATION_UPDATE) {
                                       var10001 = ((Byte)var7[0]).byteValue();
                                       var66 = ((Byte)var7[1]).byteValue();
                                       var67 = ((Byte)var7[2]).byteValue();
                                       byte var68 = ((Byte)var7[3]).byteValue();
                                       var69 = ((Byte)var7[4]).byteValue();
                                       var9 = ((Byte)var7[5]).byteValue();
                                       var51 = var69;
                                       var49 = var68;
                                       var42 = var67;
                                       var38 = var66;
                                       var5 = var10001;
                                       if(var5 >= 0) {
                                          var51 = (byte)(var51 + 128);
                                          if((var58 = (NetworkPlayer)var44.players.get(Byte.valueOf(var5))) != null) {
                                             var58.queue(var38, var42, var49, (float)(var51 * 360) / 256.0F, (float)(var9 * 360) / 256.0F);
                                          }
                                       }
                                    } else if(var6 == PacketType.ROTATION_UPDATE) {
                                       var10001 = ((Byte)var7[0]).byteValue();
                                       var66 = ((Byte)var7[1]).byteValue();
                                       var42 = ((Byte)var7[2]).byteValue();
                                       var38 = var66;
                                       var5 = var10001;
                                       if(var5 >= 0) {
                                          var38 = (byte)(var38 + 128);
                                          NetworkPlayer var52;
                                          if((var52 = (NetworkPlayer)var44.players.get(Byte.valueOf(var5))) != null) {
                                             var52.queue((float)(var38 * 360) / 256.0F, (float)(var42 * 360) / 256.0F);
                                          }
                                       }
                                    } else if(var6 == PacketType.POSITION_UPDATE) {
                                       var10001 = ((Byte)var7[0]).byteValue();
                                       var66 = ((Byte)var7[1]).byteValue();
                                       var67 = ((Byte)var7[2]).byteValue();
                                       var49 = ((Byte)var7[3]).byteValue();
                                       var42 = var67;
                                       var38 = var66;
                                       var5 = var10001;
                                       NetworkPlayer var56;
                                       if(var5 >= 0 && (var56 = (NetworkPlayer)var44.players.get(Byte.valueOf(var5))) != null) {
                                          var56.queue(var38, var42, var49);
                                       }
                                    } else if(var6 == PacketType.DESPAWN_PLAYER) {
                                       var5 = ((Byte)var7[0]).byteValue();
                                       if(var5 >= 0 && (var34 = (NetworkPlayer)var44.players.remove(Byte.valueOf(var5))) != null) {
                                          var34.clear();
                                          var44.minecraft.level.removeEntity(var34);
                                       }
                                    } else if(var6 == PacketType.CHAT_MESSAGE) {
                                       var10001 = ((Byte)var7[0]).byteValue();
                                       var33 = (String)var7[1];
                                       var5 = var10001;
                                       if(var5 < 0) {
                                          var44.minecraft.hud.addChat("&e" + var33);
                                       } else {
                                          var44.players.get(Byte.valueOf(var5));
                                          var44.minecraft.hud.addChat(var33);
                                       }
                                    } else if(var6 == PacketType.DISCONNECT) {
                                       var44.netHandler.close();
                                       var44.minecraft.setCurrentScreen(new ErrorScreen("Connection lost", (String)var7[0]));
                                    } else if(var6 == PacketType.UPDATE_PLAYER_TYPE) {
                                       var44.minecraft.player.userType = ((Byte)var7[0]).byteValue();
                                    }
                                 }
                              }
                           }
                        }

                        if(!var22.connected) {
                           break;
                        }

                        var22.in.compact();
                     }

                     if(var22.out.position() > 0) {
                        var22.out.flip();
                        var22.channel.write(var22.out);
                        var22.out.compact();
                     }
                  } catch (Exception var15) {
                     var20.minecraft.setCurrentScreen(new ErrorScreen("Disconnected!", "You\'ve lost connection to the server"));
                     var20.minecraft.online = false;
                     var15.printStackTrace();
                     var20.netHandler.close();
                     var20.minecraft.networkManager = null;
                  }
               }
            }

            Player var28 = this.player;
            var20 = this.networkManager;
            if(this.networkManager.levelLoaded) {
               int var24 = (int)(var28.x * 32.0F);
               var4 = (int)(var28.y * 32.0F);
               var40 = (int)(var28.z * 32.0F);
               var46 = (int)(var28.yRot * 256.0F / 360.0F) & 255;
               var47 = (int)(var28.xRot * 256.0F / 360.0F) & 255;
               var20.netHandler.send(PacketType.POSITION_ROTATION, new Object[]{Integer.valueOf(-1), Integer.valueOf(var24), Integer.valueOf(var4), Integer.valueOf(var40), Integer.valueOf(var46), Integer.valueOf(var47)});
            }
         }
      }

      if(this.currentScreen == null && this.player != null && this.player.health <= 0) {
         this.setCurrentScreen((GuiScreen)null);
      }

      if(this.currentScreen == null || this.currentScreen.grabsMouse) {
         int var25;
         while(Mouse.next()) {
            if((var25 = Mouse.getEventDWheel()) != 0) {
               this.player.inventory.swapPaint(var25);
            }

            if(this.currentScreen == null) {
               if(!this.hasMouse && Mouse.getEventButtonState()) {
                  this.grabMouse();
               } else {
                  if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                     this.onMouseClick(0);
                     this.lastClick = this.ticks;
                  }

                  if(Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
                     this.onMouseClick(1);
                     this.lastClick = this.ticks;
                  }

                  if(Mouse.getEventButton() == 2 && Mouse.getEventButtonState() && this.selected != null) {
                     if((var16 = this.level.getTile(this.selected.x, this.selected.y, this.selected.z)) == Block.GRASS.id) {
                        var16 = Block.DIRT.id;
                     }

                     if(var16 == Block.DOUBLE_SLAB.id) {
                        var16 = Block.SLAB.id;
                     }

                     if(var16 == Block.BEDROCK.id) {
                        var16 = Block.STONE.id;
                     }

                     this.player.inventory.grabTexture(var16, this.gamemode instanceof CreativeGameMode);
                  }
               }
            }

            if(this.currentScreen != null) {
               this.currentScreen.mouseEvent();
            }
         }

         if(this.blockHitTime > 0) {
            --this.blockHitTime;
         }

         while(Keyboard.next()) {
            this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
            if(Keyboard.getEventKeyState()) {
               if(this.currentScreen != null) {
                  this.currentScreen.keyboardEvent();
               }

               if(this.currentScreen == null) {
                  if(Keyboard.getEventKey() == 1) {
                     this.pause();
                  }

                  if(this.gamemode instanceof CreativeGameMode) {
                     if(Keyboard.getEventKey() == this.settings.loadLocationKey.key) {
                        this.player.resetPos();
                     }

                     if(Keyboard.getEventKey() == this.settings.saveLocationKey.key) {
                        this.level.setSpawnPos((int)this.player.x, (int)this.player.y, (int)this.player.z, this.player.yRot);
                        this.player.resetPos();
                     }
                  }

                  Keyboard.getEventKey();
                  if(Keyboard.getEventKey() == 63) {
                     this.raining = !this.raining;
                  }

                  if(Keyboard.getEventKey() == 15 && this.gamemode instanceof SurvivalGameMode && this.player.arrows > 0) {
                     this.level.addEntity(new Arrow(this.level, this.player, this.player.x, this.player.y, this.player.z, this.player.yRot, this.player.xRot, 1.2F));
                     --this.player.arrows;
                  }

                  if(Keyboard.getEventKey() == this.settings.buildKey.key) {
                     this.gamemode.openInventory();
                  }

                  if(Keyboard.getEventKey() == this.settings.chatKey.key && this.networkManager != null && this.networkManager.isConnected()) {
                     this.player.releaseAllKeys();
                     this.setCurrentScreen(new ChatInputScreen());
                  }
               }

               for(var25 = 0; var25 < 9; ++var25) {
                  if(Keyboard.getEventKey() == var25 + 2) {
                     this.player.inventory.selected = var25;
                  }
               }

               if(Keyboard.getEventKey() == this.settings.toggleFogKey.key) {
                  this.settings.toggleSetting(4, !Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54)?1:-1);
               }
            }
         }

         if(this.currentScreen == null) {
            if(Mouse.isButtonDown(0) && (float)(this.ticks - this.lastClick) >= this.timer.tps / 4.0F && this.hasMouse) {
               this.onMouseClick(0);
               this.lastClick = this.ticks;
            }

            if(Mouse.isButtonDown(1) && (float)(this.ticks - this.lastClick) >= this.timer.tps / 4.0F && this.hasMouse) {
               this.onMouseClick(1);
               this.lastClick = this.ticks;
            }
         }

         boolean var26 = this.currentScreen == null && Mouse.isButtonDown(0) && this.hasMouse;
         boolean var36 = false;
         if(!this.gamemode.instantBreak && this.blockHitTime <= 0) {
            if(var26 && this.selected != null && this.selected.entityPos == 0) {
               var4 = this.selected.x;
               var40 = this.selected.y;
               var46 = this.selected.z;
               this.gamemode.hitBlock(var4, var40, var46, this.selected.face);
            } else {
               this.gamemode.resetHits();
            }
         }
      }

      if(this.currentScreen != null) {
         this.lastClick = this.ticks + 10000;
      }

      if(this.currentScreen != null) {
         this.currentScreen.doInput();
         if(this.currentScreen != null) {
            this.currentScreen.tick();
         }
      }

      if(this.level != null) {
         Renderer var29 = this.renderer;
         ++this.renderer.levelTicks;
         HeldBlock var39 = var29.heldBlock;
         var29.heldBlock.lastPos = var39.pos;
         if(var39.moving) {
            ++var39.offset;
            if(var39.offset == 7) {
               var39.offset = 0;
               var39.moving = false;
            }
         }

         Player var27 = var39.minecraft.player;
         var4 = var39.minecraft.player.inventory.getSelected();
         Block var43 = null;
         if(var4 > 0) {
            var43 = Block.blocks[var4];
         }

         float var48 = 0.4F;
         float var53;
         if((var53 = (var43 == var39.block?1.0F:0.0F) - var39.pos) < -var48) {
            var53 = -var48;
         }

         if(var53 > var48) {
            var53 = var48;
         }

         var39.pos += var53;
         if(var39.pos < 0.1F) {
            var39.block = var43;
         }

         if(var29.minecraft.raining) {
            Renderer var41 = var29;
            var27 = var29.minecraft.player;
            Level var32 = var29.minecraft.level;
            var40 = (int)var27.x;
            var46 = (int)var27.y;
            var47 = (int)var27.z;

            for(var8 = 0; var8 < 50; ++var8) {
               int var57 = var40 + var41.random.nextInt(9) - 4;
               int var50 = var47 + var41.random.nextInt(9) - 4;
               int var60;
               if((var60 = var32.getHighestTile(var57, var50)) <= var46 + 4 && var60 >= var46 - 4) {
                  float var61 = var41.random.nextFloat();
                  float var63 = var41.random.nextFloat();
                  var41.minecraft.particleManager.spawnParticle(new WaterDropParticle(var32, (float)var57 + var61, (float)var60 + 0.1F, (float)var50 + var63));
               }
            }
         }

         LevelRenderer var30 = this.levelRenderer;
         ++this.levelRenderer.ticks;
         this.level.tickEntities();
         if(!this.isOnline()) {
            this.level.tick();
         }

         this.particleManager.tick();
      }

   }

   public final boolean isOnline() {
      return this.networkManager != null;
   }

   public final void generateLevel(int var1) {
      String var2 = this.session != null?this.session.username:"anonymous";
      Level var4 = (new LevelGenerator(this.progressBar)).generate(var2, 128 << var1, 128 << var1, 64);
      this.gamemode.prepareLevel(var4);
      this.setLevel(var4);
   }

   public final boolean loadOnlineLevel(String var1, int var2) {
      Level var3;
      if((var3 = this.levelIo.loadOnline(this.host, var1, var2)) == null) {
         return false;
      } else {
         this.setLevel(var3);
         return true;
      }
   }

   public final void setLevel(Level var1) {
      if(this.applet == null || !this.applet.getDocumentBase().getHost().equalsIgnoreCase("minecraft.net") && !this.applet.getDocumentBase().getHost().equalsIgnoreCase("www.minecraft.net") || !this.applet.getCodeBase().getHost().equalsIgnoreCase("minecraft.net") && !this.applet.getCodeBase().getHost().equalsIgnoreCase("www.minecraft.net")) {
         var1 = null;
      }

      this.level = var1;
      if(var1 != null) {
         var1.initTransient();
         this.gamemode.apply(var1);
         var1.font = this.fontRenderer;
         var1.rendererContext$5cd64a7f = this;
         if(!this.isOnline()) {
            this.player = (Player)var1.findSubclassOf(Player.class);
         } else if(this.player != null) {
            this.player.resetPos();
            this.gamemode.preparePlayer(this.player);
            if(var1 != null) {
               var1.player = this.player;
               var1.addEntity(this.player);
            }
         }
      }

      if(this.player == null) {
         this.player = new Player(var1);
         this.player.resetPos();
         this.gamemode.preparePlayer(this.player);
         if(var1 != null) {
            var1.player = this.player;
         }
      }

      if(this.player != null) {
         this.player.input = new InputHandlerImpl(this.settings);
         this.gamemode.apply(this.player);
      }

      if(this.levelRenderer != null) {
         LevelRenderer var3 = this.levelRenderer;
         if(this.levelRenderer.level != null) {
            var3.level.removeListener(var3);
         }

         var3.level = var1;
         if(var1 != null) {
            var1.addListener(var3);
            var3.refresh();
         }
      }

      if(this.particleManager != null) {
         ParticleManager var5 = this.particleManager;
         if(var1 != null) {
            var1.particleEngine = var5;
         }

         for(int var4 = 0; var4 < 2; ++var4) {
            var5.particles[var4].clear();
         }
      }

      System.gc();
   }
}
