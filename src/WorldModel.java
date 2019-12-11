import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.*;
import processing.core.PImage;

final class WorldModel
{
   private int numRows;
   private int numCols;
   private Background background[][];
   private OccupancyHolder occupancy[][];
   private Set<Entity> entities;
   private Set<Entity> higherEntities = new HashSet<>();
   private Set<Moveable> players = new HashSet<>();

   private List<Character> babas = new ArrayList<>();
   private List<Character> walls = new ArrayList<>();
   private List<Character> rocks = new ArrayList<>();
   private List<Character> flags = new ArrayList<>();
   private List<Character> skulls = new ArrayList<>();
   private List<Character> grass = new ArrayList<>();
   private List<Character> floors = new ArrayList<>();
   private List<Noun> nouns = new ArrayList<>();


   private ImageStore imageStr;

   private static final int PROPERTY_KEY = 0;

   private static final String BGND_KEY = "background";
   private static final int BGND_NUM_PROPERTIES = 4;
   private static final int BGND_ID = 1;
   private static final int BGND_COL = 2;
   private static final int BGND_ROW = 3;

   private static final String BABA_KEY = "baba";
   private static final String WALL_KEY = "wall";
   private static final String ROCK_KEY = "rock";
   private static final String FLAG_KEY = "flag";
   private static final String SKULL_KEY = "skull";
   private static final String GRASS_KEY = "grass";
   private static final String FLOOR_KEY = "floor";

   private static final String NOUN_BABA_KEY = "noun_baba";
   private static final String NOUN_WALL_KEY = "noun_wall";
   private static final String NOUN_ROCK_KEY = "noun_rock";
   private static final String NOUN_FLAG_KEY = "noun_flag";
   private static final String NOUN_SKULL_KEY = "noun_skull";
   private static final String NOUN_GRASS_KEY = "noun_grass";

   private static final String CONJ_IS_KEY = "is";

   private static final String VERB_YOU_KEY = "verb_you";
   private static final String VERB_PUSH_KEY = "verb_push";
   private static final String VERB_STOP_KEY = "verb_stop";
   private static final String VERB_WIN_KEY = "verb_win";
   private static final String VERB_DEATH_KEY = "verb_death";


   private static final int NOUN_NUM_PROPERTIES = 6;
   private static final int CONJ_NUM_PROPERTIES = 6;
   private static final int CHAR_NUM_PROPERTIES = 6;
   private static final int ID = 1;
   private static final int COL = 2;
   private static final int ROW = 3;
   private static final int ACTION_PERIOD = 4;
   private static final int ANIMATION_PERIOD = 5;

   private VirtualWorld screen;



   public WorldModel(int numRows, int numCols, Background defaultBackground, VirtualWorld screen)
   {
      this.numRows = numRows;
      this.numCols = numCols;
      this.background = new Background[numRows][numCols];
      this.occupancy = new OccupancyHolder[numRows][numCols];
      this.entities = new HashSet<>();
      this.screen = screen;

      for (int row = 0; row < numRows; row++)
      {
         Arrays.fill(this.background[row], defaultBackground);
      }
   }

   //Get Functions
   public ImageStore getImageStore() { return this.imageStr; }
   public int getNumRows() { return this.numRows; }
   public int getNumCols() { return this.numCols; }
   public VirtualWorld getScreen() { return this.screen; }
   public Set<Entity> getEntities() { return this.entities; }
   public Set<Entity> getHigherEntities() { return this.higherEntities; }
   public Set<Moveable> getPlayers() { return this.players; }
   public List<Noun> getNouns() { return this.nouns; }

   public List<Character> getWalls() { return this.walls; }
   public List<Character> getBabas() { return this.babas; }
   public List<Character> getRocks() { return this.rocks; }
   public List<Character> getFlags() { return this.flags; }
   public List<Character> getSkulls() { return this.skulls; }

   //Functions

   public Optional<Entity> findNearest(Point pos, Class kind)
   {
      List<Entity> ofType = new LinkedList<>();
      for (Entity entity : this.entities)
      {
         if (entity.getClass() == kind)
         {
            ofType.add(entity);
         }
      }

      return nearestEntity(ofType, pos);
   }

   public Optional<Entity> nearestEntity(List<Entity> entities,
                                         Point pos)
   {
      if (entities.isEmpty())
      {
         return Optional.empty();
      }
      else
      {
         Entity nearest = entities.get(0);
         int nearestDistance = nearest.getPosition().distanceSquared(pos);

         for (Entity other : entities)
         {
            int otherDistance = other.getPosition().distanceSquared(pos);

            if (otherDistance < nearestDistance)
            {
               nearest = other;
               nearestDistance = otherDistance;
            }
         }

         return Optional.of(nearest);
      }
   }

   public void load(Scanner in, ImageStore imageStore)
   {
      this.imageStr = imageStore;
      int lineNumber = 0;
      while (in.hasNextLine())
      {
         try
         {
            String line = in.nextLine();
            if (line.equals("")) {
               System.out.println("Line " + lineNumber + " empty line");
            }
            else if(!processLine(line, imageStore))
            {
               System.err.println(String.format("invalid entry on line %d",
                       lineNumber));
            }
         }
         catch (NumberFormatException e)
         {
            System.err.println(String.format("invalid entry on line %d",
                    lineNumber));
         }
         catch (IllegalArgumentException e)
         {
            System.err.println(String.format("issue on line %d: %s",
                    lineNumber, e.getMessage()));
         }
         lineNumber++;
      }
   }

   public boolean processLine(String line, ImageStore imageStore)
   {
      String[] properties = line.split("\\s");
      if (properties.length > 0)
      {
         switch (properties[PROPERTY_KEY])
         {
            case BABA_KEY:
               return parseBaba(properties, imageStore);
            case BGND_KEY:
               return parseBackground(properties, imageStore);
            case WALL_KEY:
               return parseWall(properties, imageStore);
            case ROCK_KEY:
               return parseRock(properties, imageStore);
            case FLAG_KEY:
               return parseFlag(properties, imageStore);
            case SKULL_KEY:
               return parseSkull(properties, imageStore);
            case GRASS_KEY:
               return parseGrass(properties, imageStore);
            case FLOOR_KEY:
               return parseFloor(properties, imageStore);

            case NOUN_BABA_KEY:
               return parseNounBaba(properties, imageStore);
            case NOUN_WALL_KEY:
               return parseNounWall(properties, imageStore);
            case NOUN_ROCK_KEY:
               return parseNounRock(properties, imageStore);
            case NOUN_FLAG_KEY:
               return parseNounFlag(properties, imageStore);
            case NOUN_SKULL_KEY:
               return parseNounSkull(properties, imageStore);
            case NOUN_GRASS_KEY:
               return parseNounGrass(properties, imageStore);

            case CONJ_IS_KEY:
               return parseConjIs(properties, imageStore);

            case VERB_YOU_KEY:
               return parseVerbYou(properties, imageStore);
            case VERB_PUSH_KEY:
               return parseVerbPush(properties, imageStore);
            case VERB_STOP_KEY:
               return parseVerbStop(properties, imageStore);
            case VERB_WIN_KEY:
               return parseVerbWin(properties, imageStore);
            case VERB_DEATH_KEY:
               return parseVerbDeath(properties, imageStore);

         }
      }

      return false;
   }

   //Parsing
   public boolean parseBackground(String [] properties, ImageStore imageStore)
   {
      if (properties.length == this.BGND_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[this.BGND_COL]),
                 Integer.parseInt(properties[this.BGND_ROW]));
         String id = properties[this.BGND_ID];
         setBackground(pt, new Background(id, imageStore.getImageList(id)));
      }

      return properties.length == this.BGND_NUM_PROPERTIES;
   }

   public boolean parseBaba(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, BABA_KEY, babas);
   }

   public boolean parseWall(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, WALL_KEY, walls);
   }

   public boolean parseRock(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, ROCK_KEY, rocks);
   }

   public boolean parseFlag(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, FLAG_KEY, flags);
   }
   public boolean parseSkull(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, SKULL_KEY, skulls);
   }

   public boolean parseGrass(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, GRASS_KEY, grass);
   }

   public boolean parseFloor(String [] properties, ImageStore imageStore) {
      return parseCharacter(properties, imageStore, FLOOR_KEY, floors);
   }

   public boolean parseCharacter(String [] properties, ImageStore imageStore,
                                 String key, List<Character> entities) {
      if (properties.length == this.CHAR_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[this.COL]),
                 Integer.parseInt(properties[this.ROW]));
         Character entity = createCharacter(properties[this.ID],
                 pt,
                 Integer.parseInt(properties[this.ACTION_PERIOD]),
                 Integer.parseInt(properties[this.ANIMATION_PERIOD]),
                 imageStore.getImageList(key));
         tryAddEntity(entity);
         entities.add(entity);
         if (key.equals("baba") || key.equals("flag") || key.equals("rock")) {
            higherEntities.add(entity);
         }
      }

      return properties.length == this.CHAR_NUM_PROPERTIES;
   }

   // Parse Nouns
   public boolean parseNounBaba(String [] properties, ImageStore imageStore) {
      return parseNoun(properties, imageStore, this.NOUN_BABA_KEY, this.babas);
   }

   public boolean parseNounWall(String [] properties, ImageStore imageStore) {
      return parseNoun(properties, imageStore, this.NOUN_WALL_KEY, this.walls);
   }

   public boolean parseNounRock(String [] properties, ImageStore imageStore) {
      return parseNoun(properties, imageStore, this.NOUN_ROCK_KEY, this.rocks);
   }

   public boolean parseNounFlag(String [] properties, ImageStore imageStore) {
      return parseNoun(properties, imageStore, this.NOUN_FLAG_KEY, this.flags);
   }

   public boolean parseNounSkull(String [] properties, ImageStore imageStore) {
      return parseNoun(properties, imageStore, this.NOUN_SKULL_KEY, this.skulls);
   }

   public boolean parseNounGrass(String [] properties, ImageStore imageStore) {
      return parseNoun(properties, imageStore, this.NOUN_GRASS_KEY, this.grass);
   }

   public boolean parseNoun(String [] properties, ImageStore imageStore, String key, List<Character> entities) {
      if (properties.length == this.NOUN_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[this.COL]),
                 Integer.parseInt(properties[this.ROW]));
         Entity entity = createNoun(properties[this.ID],
                 pt,
                 Integer.parseInt(properties[this.ACTION_PERIOD]),
                 Integer.parseInt(properties[this.ANIMATION_PERIOD]),
                 imageStore.getImageList(key + "_inactive"),
                 entities, key);
         tryAddEntity(entity);
         nouns.add((Noun)entity);
         higherEntities.add(entity);
      }
      return properties.length == this.NOUN_NUM_PROPERTIES;
   }

   // Parse Conjunction
   public boolean parseConjIs(String [] properties, ImageStore imageStore) {
      return parseConj(properties, imageStore, this.CONJ_IS_KEY);
   }

   public boolean parseConj(String [] properties, ImageStore imageStore, String key) {
      if (properties.length == this.CONJ_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[this.COL]),
                 Integer.parseInt(properties[this.ROW]));
         Entity entity = createConj(properties[this.ID],
                 pt,
                 Integer.parseInt(properties[this.ACTION_PERIOD]),
                 Integer.parseInt(properties[this.ANIMATION_PERIOD]),
                 imageStore.getImageList(key  + "_inactive"),
                 key);
         tryAddEntity(entity);
         higherEntities.add(entity);
      }
      return properties.length == this.CONJ_NUM_PROPERTIES;
   }

   // Parse Verbs

   public boolean parseVerbYou(String [] properties, ImageStore imageStore) {
      return parseVerb(properties, imageStore, this.VERB_YOU_KEY);
   }

   public boolean parseVerbPush(String [] properties, ImageStore imageStore) {
      return parseVerb(properties, imageStore, this.VERB_PUSH_KEY);
   }

   public boolean parseVerbStop(String [] properties, ImageStore imageStore) {
      return parseVerb(properties, imageStore, this.VERB_STOP_KEY);
   }

   public boolean parseVerbWin(String [] properties, ImageStore imageStore) {
      return parseVerb(properties, imageStore, this.VERB_WIN_KEY);
   }

   public boolean parseVerbDeath(String [] properties, ImageStore imageStore) {
      return parseVerb(properties, imageStore, this.VERB_DEATH_KEY);
   }

   public boolean parseVerb(String [] properties, ImageStore imageStore, String key) {
      if (properties.length == this.NOUN_NUM_PROPERTIES)
      {
         Point pt = new Point(Integer.parseInt(properties[this.COL]),
                 Integer.parseInt(properties[this.ROW]));
         Entity entity = createVerb(properties[this.ID],
                 pt,
                 Integer.parseInt(properties[this.ACTION_PERIOD]),
                 Integer.parseInt(properties[this.ANIMATION_PERIOD]),
                 imageStore.getImageList(key  + "_inactive"),
                 key);
         tryAddEntity(entity);
         higherEntities.add(entity);
      }
      return properties.length == this.NOUN_NUM_PROPERTIES;
   }


   //Creating

   public Character createCharacter(String id, Point position, int actionPeriod, int animationPeriod,
                          List<PImage> images) {
      return new Character(id, position, images, actionPeriod, animationPeriod);
   }

   public Noun createNoun(String id, Point position, int actionPeriod, int animationPeriod,
                          List<PImage> images, List<Character> c, String key) {
      return new Noun(id, position, images, actionPeriod, animationPeriod, c, key);
   }

   public Conjunction createConj(String id, Point position, int actionPeriod, int animationPeriod,
                          List<PImage> images, String key) {
      return new Conjunction(id, position, images, actionPeriod, animationPeriod, key);
   }

   public Verb createVerb(String id, Point position, int actionPeriod, int animationPeriod,
                          List<PImage> images, String key) {
      return new Verb(id, position, images, actionPeriod, animationPeriod, key);
   }

   public Dust createDust(String id, Point position, int actionPeriod, int animationPeriod,
                            List<PImage> images, EventScheduler scheduler) {
      return new Dust(id, position, images, actionPeriod, animationPeriod, this, scheduler);
   }

   //Adding/Deleting Entities
   public void tryAddEntity(Entity entity)
   {
      if (isOccupied(entity.getPosition()))
      {
         // arguably the wrong type of exception, but we are not
         // defining our own exceptions yet
         throw new IllegalArgumentException("position occupied");
      }

      addEntity(entity);
   }

   public boolean withinBounds(Point pos)
   {
      return pos.getY() >= 0 && pos.getY() < this.numRows &&
              pos.getX() >= 0 && pos.getX() < this.numCols;
   }

   public boolean isOccupied(Point pos)
   {
      return this.withinBounds(pos) &&
              (getOccupancyCell(pos) != null &&
                      getOccupancyCell(pos).getEntities().size() != 0);
   }

   public void addEntity(Entity entity)
   {
      if (this.withinBounds(entity.getPosition()))
      {
         setOccupancyCell(entity.getPosition(), entity);
         this.entities.add(entity);
      }
   }

   public void moveEntity(Entity entity, Point pos, EventScheduler scheduler)
   {
      Point oldPos = entity.getPosition();
      if (this.withinBounds(pos) && !pos.equals(oldPos))
      {

         removeEntityAt(entity, oldPos, scheduler);
         setOccupancyCell(pos, entity);
         entity.setPosition(pos);
      }
   }


   public void removeEntity(Entity entity, EventScheduler scheduler)
   {
      removeEntityAt(entity, entity.getPosition(), scheduler);
   }

   public void removeEntityAt(Entity e, Point pos, EventScheduler scheduler)
   {
      if (this.withinBounds(pos))
      {
         scheduler.scheduleEvent(e,
                 new FunctionAction(() -> {
                    getOccupancyCell(pos).removeEntity(e);

                    if (e instanceof Text) {
                       for (Noun noun : this.getNouns()) {
                          scheduler.scheduleEvent(noun,
                                  new FunctionAction(() -> noun.checkIfPhrase(this, scheduler))
                                  , 1);
                       }
                    }
                 }), 1);
         e.setPosition(new Point(-1, -1));
      }
   }

   public Optional<PImage> getBackgroundImage(Point pos)
   {
      if (this.withinBounds(pos))
      {
         return Optional.of(WorldView.getCurrentImage(getBackgroundCell(pos)));
      }
      else
      {
         return Optional.empty();
      }
   }

   public void setBackground(Point pos, Background background)
   {
      if (this.withinBounds(pos))
      {
         setBackgroundCell(pos, background);
      }
   }

   public Optional<OccupancyHolder> getOccupant(Point pos)
   {
      if (this.isOccupied(pos))
      {
         return Optional.of(getOccupancyCell(pos));
      }
      else
      {
         return Optional.empty();
      }
   }

   public OccupancyHolder getOccupancyCell(Point pos)
   {
      return this.occupancy[pos.getY()][pos.getX()];
   }

   public void setOccupancyCell(Point pos, Entity entity)
   {
      if (this.occupancy[pos.getY()][pos.getX()] == null) {
         this.occupancy[pos.getY()][pos.getX()] = new OccupancyHolder();
      }
      this.occupancy[pos.getY()][pos.getX()].setEntity(entity);
   }

   public Background getBackgroundCell(Point pos)
   {
      return this.background[pos.getY()][pos.getX()];
   }

   public void setBackgroundCell(Point pos, Background background)
   {
      this.background[pos.getY()][pos.getX()] = background;
   }
}


class OccupancyHolder {
      private Set<Entity> entities;
   public OccupancyHolder() {
      this.entities = new HashSet<>();
   }

   public Set<Entity> getEntities() {
      return entities;
   }

   public void setEntity(Entity e) {
      entities.add(e);
   }

   public boolean removeEntity(Entity e) {
      if (entities.contains(e)) {
         entities.remove(e);
         return true;
      }
      return false;

   }
}