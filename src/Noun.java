import processing.core.PImage;
import java.util.function.*;
import java.util.List;
import java.util.*;
import java.util.Optional;

public class Noun extends Text{
    private List<Character> effectedEntities;
    private Set<Text> curActive = new HashSet<>(); // All of the verbs and nouns connected

    public Noun(String id, Point position,
                List<PImage> images, int actionPeriod, int animationPeriod, List<Character> effectedEntities,
                String type) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.effectedEntities = effectedEntities;
        this.type = type;
    }

    public List<Character> getEffectedEntities() {
        return this.effectedEntities;
    }
    public Set<Text> getCurActive() {
        return this.curActive;
    }

    public void clearCurActive() {
        curActive.clear();
    }

    public void checkIfPhrase(WorldModel world, EventScheduler scheduler) {
                        // Right                        Down
        Point dir[] = {new Point(1, 0), new Point(0, 1)};
        checkStillHasPhrase(world, scheduler);
        for (Point d : dir) {
            Point nextPos = this.position.add(d);
            Optional<OccupancyHolder> ocHolder = world.getOccupant(nextPos);

            if (ocHolder.isPresent()) { // Check for conjunction
                for (Entity e : ocHolder.get().getEntities()) {
                    if (e instanceof Conjunction) {
                        ocHolder = world.getOccupant(nextPos.add(d));

                        if (ocHolder.isPresent()) { //Check for noun/verb
                            for (Entity e1 : ocHolder.get().getEntities()) {
                                if (e1 instanceof Text && !curActive.contains(e1)) {
                                    if (e1 instanceof Verb) {
                                        activateVerb((Verb) e1, world, scheduler);
                                    } else if (e1 instanceof Noun) {
                                        activateNoun((Noun) e1, world, scheduler);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkStillHasPhrase(WorldModel world, EventScheduler scheduler) {
        Point dir[] = {new Point(1, 0), new Point(0, 1)};
        boolean stillActive = false;
        for (Text t : curActive) {
            stillActive = checkNounStillHasPhrase(this, t, world, scheduler);
            if (!stillActive) {
                if (t instanceof Verb) {
                    deactivateVerb((Verb) t, world, scheduler);
                } else if (t instanceof Noun) {
                    deactivateNoun((Noun) t, world, scheduler);
                }
            }
        }
        return stillActive;
    }

    //Check to see if noun is still active with verb/noun t.
    private boolean checkNounStillHasPhrase(Noun noun, Text t, WorldModel world, EventScheduler scheduler) {
        Point dir[] = {new Point(1, 0), new Point(0, 1)};
        boolean stillActive = false;
        for (Point d : dir) {
            Optional<OccupancyHolder> ocHolder = world.getOccupant(noun.position.add(d));
            if (ocHolder.isPresent()) {
                for (Entity occupant : ocHolder.get().getEntities()) {
                    if (occupant instanceof Conjunction) {
                        ocHolder = world.getOccupant(noun.position.add(d).add(d));
                        if (ocHolder.isPresent()) {
                            for (Entity oc2 : ocHolder.get().getEntities()) {
                                if (!oc2.equals(t)) {
                                    stillActive = stillActive || false;
                                } else {
                                    stillActive = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return stillActive;
    }

    private boolean checkTextStillHasPhrase(Text t, WorldModel world, EventScheduler scheduler) {
        for (Noun n : world.getNouns()) {
            if (n.getCurActive().contains(t)) {
                return true;
            }
        }
        return false;
    }

    private void deactivateVerb(Verb verb, WorldModel world, EventScheduler scheduler) {
        if (verb.getType().equals("verb_you")) {
            doToAllEffectedEnt(ch -> {
                ch.setIsYou(false);
                world.getPlayers().remove(ch);
                if (world.getPlayers().size() == 0) {
                    WorldView.restartImage = world.getScreen().loadImage("images/restart.png");
                }
            }, scheduler);
        }
        if (verb.getType().equals("verb_push")) {
            doToAllEffectedEnt(ch -> ch.setIsPush(false), scheduler);
        }
        if (verb.getType().equals("verb_stop")) {
            doToAllEffectedEnt(ch -> ch.setIsStop(false), scheduler);
            System.out.println(this.type + " DEACTIVATED");
        }
        if (verb.getType().equals("verb_death")) {
            doToAllEffectedEnt(ch -> ch.setIsDeath(false), scheduler);
        }
        if (verb.getType().equals("verb_win")) {
            doToAllEffectedEnt(ch -> {
                ch.setIsWin(false);
                if (ch.sparkles != null) {
                    world.removeEntity(ch.sparkles, scheduler);
                }


                }, scheduler);
        }
        scheduleRemoveCurActive(verb, scheduler);
        scheduleDisablePhrase(verb, world, scheduler);
        scheduleCheckIfPhrase(world, scheduler);

    }

    private void deactivateNoun(Noun noun, WorldModel world, EventScheduler scheduler) {
        scheduleRemoveCurActive(noun, scheduler);
        scheduleDisablePhrase(noun, world, scheduler);
        scheduleCheckIfPhrase(world, scheduler);
    }


    private void activateVerb(Verb verb, WorldModel world, EventScheduler scheduler) {
        if (verb.getType().equals("verb_win")) {
            doToAllEffectedEnt(ch ->  {
                ch.setIsWin(true);
                scheduleSparkles(scheduler, world, ch);

                if (ch.isYou && ch.isWin) {
                    ch.scheduleWin(scheduler, world);
                }
            }, scheduler);
        }
        if (verb.getType().equals("verb_you")) {
            doToAllEffectedEnt(ch -> {
                ch.setIsYou(true);
                if (ch.isWin) {
                    ch.scheduleWin(scheduler, world);
                }
                world.getPlayers().add(ch);
            }, scheduler);
        }

        if (verb.getType().equals("verb_push")) {
            doToAllEffectedEnt(ch -> ch.setIsPush(true), scheduler);
        }

        if (verb.getType().equals("verb_stop")) {
            doToAllEffectedEnt(ch -> ch.setIsStop(true), scheduler);
        }



        if (verb.getType().equals("verb_death")) {
            doToAllEffectedEnt(ch -> ch.setIsDeath(true), scheduler);
        }
        enablePhrase(verb, world, scheduler);
        curActive.add(verb);
    }

    private void scheduleSparkles(EventScheduler scheduler, WorldModel world, Entity ch) {
        scheduler.scheduleEvent(this, new FunctionAction (() -> {
            Character e = world.createCharacter("sparkle", ch.position, 100, 200,
                    world.getImageStore().getImageList("sparkle"));
            world.addEntity(e);
            world.getHigherEntities().add(e);
            ch.sparkles = e;
            e.scheduleActions(scheduler, world, world.getImageStore());
        }), 0);
    }

    private void doToAllEffectedEnt(Consumer<Character> con, EventScheduler scheduler) {

        scheduler.scheduleEvent(this,
                new FunctionAction(() -> {
                    for (Character c : effectedEntities) {
                        con.accept(c);
                    }
                }), 2);
    }

    private void activateNoun(Noun noun, WorldModel world, EventScheduler scheduler) {
        if (noun.type.equals("noun_wall")) {
            activateNoun(ch -> {world.getWalls().add(ch);
                ch.images = world.getImageStore().getImageList("wall");
                }, scheduler, noun, world);
        }

        if (noun.type.equals("noun_baba")) {
            activateNoun(ch -> {
                world.getBabas().add(ch);
                ch.images = world.getImageStore().getImageList("baba");
                }, scheduler, noun, world);
        }

        if (noun.type.equals("noun_rock")) {
            activateNoun(ch -> {
                world.getRocks().add(ch);
                ch.images = world.getImageStore().getImageList("rock");
                }, scheduler, noun, world);
        }

        if (noun.type.equals("noun_flag")) {
            activateNoun(ch -> {
                world.getFlags().add(ch);
                ch.images = world.getImageStore().getImageList("flag");
            }, scheduler, noun, world);
        }

        if (noun.type.equals("noun_skull")) {
            activateNoun(ch -> {
                world.getSkulls().add(ch);
                ch.images = world.getImageStore().getImageList("skull");
            }, scheduler, noun, world);
        }
        enablePhrase(noun, world, scheduler);
        curActive.add(noun);
    }

    // Text is either noun/verb that is enabled
    private void enablePhrase(Text t, WorldModel world, EventScheduler scheduler) {

        scheduler.scheduleEvent(this, new FunctionAction(() -> {

            Point mid = t.position.midPoint(this.position);
            Optional<OccupancyHolder> ocHolder = world.getOccupant(mid);
            if (ocHolder.isPresent()) {
                for (Entity occupant : ocHolder.get().getEntities()) {
                    if (occupant instanceof Conjunction) {
                        occupant.images = world.getImageStore().getImageList(((Conjunction) occupant).getType());
                    }
                }
            }
            this.images = world.getImageStore().getImageList(this.type);
            t.images = world.getImageStore().getImageList(t.getType());
        }), 1);
    }
    private void scheduleDisablePhrase(Text t, WorldModel world, EventScheduler scheduler) {
        scheduler.scheduleEvent(t, new FunctionAction( () -> {

            if (!this.checkStillHasPhrase(world, scheduler)) {
                this.images = world.getImageStore().getImageList(this.type + "_inactive");
            }
            if (!checkTextStillHasPhrase(t, world, scheduler)) {
                t.images = world.getImageStore().getImageList(t.getType() + "_inactive");
            }
        }), 1);
    }

    private void activateNoun(Consumer<Character> con, EventScheduler scheduler, Noun noun, WorldModel world) {
        scheduler.scheduleEvent(this,
                new FunctionAction(() -> {
                    if (!noun.type.equals(this.type)) {
                        for (Character c : effectedEntities) {
                            con.accept(c);
                            c.setAllAttributesFalse();
                            c.imageIndex = 1;
                            noun.getEffectedEntities().add(c);
                            if (world.getPlayers().contains(c)) {
                                world.getPlayers().remove(c);
                            }
                        }
                        noun.clearCurActive();
                        noun.checkIfPhrase(world, scheduler);

                        effectedEntities = new ArrayList<>();
                    }
                }), 0);
    }

    private void scheduleRemoveCurActive(Entity e, EventScheduler scheduler) {
        scheduler.scheduleEvent(e,
                new FunctionAction(() -> {
                    curActive.remove(e);
                }), 1);
    }

    private void scheduleCheckIfPhrase(WorldModel world, EventScheduler scheduler) {
        scheduler.scheduleEvent(this,
                new FunctionAction(() -> {
                    this.checkIfPhrase(world, scheduler);
                }), 1);
    }

}
