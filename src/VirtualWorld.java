import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import processing.core.*;
import java.net.URL;

public final class VirtualWorld
   extends PApplet
{
   private static final int TIMER_ACTION_PERIOD = 10;

   private static final int VIEW_WIDTH = 1472;//960;//1280;
   private static final int VIEW_HEIGHT = 960;//704;//960;
   private static final int TILE_WIDTH = 64;
   private static final int TILE_HEIGHT = 64;
   //private static final int WORLD_WIDTH_SCALE = 2;
   //private static final int WORLD_HEIGHT_SCALE = 2;

   private static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
   private static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;
   private static final int WORLD_COLS = VIEW_COLS;
   private static final int WORLD_ROWS = VIEW_ROWS;

   private static final String IMAGE_LIST_FILE_NAME = "project 1 starter code/imagelist";
   private static final String DEFAULT_IMAGE_NAME = "background_default";
   private static final int DEFAULT_IMAGE_COLOR = 0x808080;

   private String LOAD_FILE_NAME = "project 1 starter code/gaia";
   private int levelCounter = 1;
   private static final int NUM_LEVELS = 5;

   private static final String FAST_FLAG = "-fast";
   private static final String FASTER_FLAG = "-faster";
   private static final String FASTEST_FLAG = "-fastest";
   private static final double FAST_SCALE = 0.5;
   private static final double FASTER_SCALE = 0.25;
   private static final double FASTEST_SCALE = 0.10;

   private static final char MOVE_UP = 'w';
   private static final char MOVE_LEFT = 'a';
   private static final char MOVE_DOWN= 's';
   private static final char MOVE_RIGHT = 'd';
   private static final char RESTART = 'r';

   private static double timeScale = 1.0;

   private ImageStore imageStore;
   private WorldModel world;
   private WorldView view;
   private EventScheduler scheduler;




   private long next_time;

   public void settings()
   {
      size(VIEW_WIDTH, VIEW_HEIGHT);
   }

   /*
      Processing entry point for "sketch" setup.
   */

   public void setup()
   {



      this.imageStore = new ImageStore(
         createImageColored(TILE_WIDTH, TILE_HEIGHT, DEFAULT_IMAGE_COLOR));
      this.world = new WorldModel(WORLD_ROWS, WORLD_COLS,
         createDefaultBackground(imageStore), this);
      this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world,
         TILE_WIDTH, TILE_HEIGHT);
      this.scheduler = new EventScheduler(timeScale);

      loadImages(IMAGE_LIST_FILE_NAME, imageStore, this);
      loadWorld(world, LOAD_FILE_NAME + levelCounter + ".sav", imageStore);

      scheduleActions(world, scheduler, imageStore);

      next_time = System.currentTimeMillis() + TIMER_ACTION_PERIOD;



   }

   private void testAddImage() {

      createFont("hello", 2);
      text("HELLLO LOSERS", 0, 0);

   }

   public void draw()
   {
      long time = System.currentTimeMillis();

      if (time >= next_time)
      {
         this.scheduler.updateOnTime(time);
         next_time = time + TIMER_ACTION_PERIOD;
      }
      view.drawViewport();

   }

   public void keyPressed()
   {

      if (key == MOVE_UP || key == CODED && keyCode == UP) {
         for (Moveable p : world.getPlayers()) {
            p.playerMove(world, new Point(0, -1), scheduler);
         }
      }
      else if (key == MOVE_DOWN || key == CODED && keyCode == DOWN) {
         for (Moveable p : world.getPlayers()) {
            p.playerMove(world, new Point(0, 1), scheduler);
         }

      }
      else if (key == MOVE_LEFT || key == CODED && keyCode == LEFT) {
         for (Moveable p : world.getPlayers()) {
            p.playerMove(world, new Point(-1, 0), scheduler);
         }

      }
      else if (key == MOVE_RIGHT || key == CODED && keyCode == RIGHT) {
         for (Moveable p : world.getPlayers()) {
            p.playerMove(world, new Point(1, 0), scheduler);
         }

      }
      else if (key == RESTART) {
         setup();
      }
   }

   public void mouseClicked() {
      if (view.winScreen != null) {
         levelCounter += 1;
         if (levelCounter > NUM_LEVELS) {
            levelCounter = 1;
         }
         setup();
      }
   }

   public static Background createDefaultBackground(ImageStore imageStore)
   {
      return new Background(DEFAULT_IMAGE_NAME,
         imageStore.getImageList(DEFAULT_IMAGE_NAME));
   }

   public static PImage createImageColored(int width, int height, int color)
   {
      PImage img = new PImage(width, height, RGB);
      img.loadPixels();
      for (int i = 0; i < img.pixels.length; i++)
      {
         img.pixels[i] = color;
      }
      img.updatePixels();
      return img;
   }

   private static void loadImages(String filename, ImageStore imageStore,
      PApplet screen)
   {
      try
      {
         Scanner in = new Scanner(new File(filename));
         imageStore.loadImages(in, screen);
      }
      catch (FileNotFoundException e)
      {
         System.err.println(e.getMessage());
      }
   }

   public static void loadWorld(WorldModel world, String filename,
      ImageStore imageStore)
   {
      try
      {
         Scanner in = new Scanner(new File(filename));
         world.load(in, imageStore);
      }
      catch (FileNotFoundException e)
      {
         System.err.println(e.getMessage());
      }
   }

   public static void scheduleActions(WorldModel world,
      EventScheduler scheduler, ImageStore imageStore)
   {
      for (Entity entity : world.getEntities())
      {

         entity.scheduleActions(scheduler, world, imageStore);
         if (entity instanceof Noun) {
            ((Noun) entity).checkIfPhrase(world, scheduler);
         }
      }
   }

   public static void parseCommandLine(String [] args)
   {
      for (String arg : args)
      {
         switch (arg)
         {
            case FAST_FLAG:
               timeScale = Math.min(FAST_SCALE, timeScale);
               break;
            case FASTER_FLAG:
               timeScale = Math.min(FASTER_SCALE, timeScale);
               break;
            case FASTEST_FLAG:
               timeScale = Math.min(FASTEST_SCALE, timeScale);
               break;
         }
      }
   }

   public static void main(String [] args)
   {
      parseCommandLine(args);
      PApplet.main(VirtualWorld.class);
   }
}
