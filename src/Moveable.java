import java.util.List;
import java.util.Optional;
import java.util.*;
import processing.core.PImage;

abstract class Moveable extends Entity{

    public boolean playerMove(WorldModel world, Point dir, EventScheduler scheduler) {
        Point nextPos = new Point(this.position.getX() + dir.getX(), this.position.getY() + dir.getY());
        _move(world, nextPos, scheduler, dir);
        return true;
    }


    //Returns false if hasn't been moved
    public boolean _move(WorldModel world, Point nextPos, EventScheduler scheduler, Point dir) {
        boolean moved = false;
        if (!this.position.equals(nextPos)) {
            if ((this instanceof Text || (this instanceof Character && ((Character) this).getIsPush()))
                && !withinBounds(nextPos, world)) {
                return false;
            }
            Optional<OccupancyHolder> ocHolder = world.getOccupant(nextPos);
            if (ocHolder.isPresent()) {
                for (Entity neigh : ocHolder.get().getEntities()) {
                    if (neigh instanceof Character)
                    {
                        Character m_neigh = (Character) neigh;
                        if ((this instanceof Character && m_neigh.isDeath
                            && ((Character)this).getIsYou()) && !m_neigh.isStop
                                || this instanceof Character && ((Character)this).isDeath)
                        {
                            scheduleDeathDust(this.position, scheduler, world);
                            world.removeEntity(this, scheduler);
                            return false;
                        }
                        else if (m_neigh.isStop && !m_neigh.isYou) {
                                return false;
                        }
                        else if (m_neigh.isPush) {
                            if (!m_neigh._move(world,m_neigh.position.add(dir),scheduler, dir)) {
                                return false;
                            }
                        }
                        else if (m_neigh.isWin) {
                            scheduleWin(scheduler, world);
                        }
                    }
                    else if (neigh instanceof Text)
                    {
                        Text m_neigh = (Text) neigh;
                        if (!m_neigh._move(world,
                                new Point(m_neigh.position.getX() + dir.getX(), m_neigh.position.getY() + dir.getY()),
                                scheduler, dir)) {
                            return false;
                        }

                    }
                }
            }

            moved = scheduleMoveEntity(this, nextPos, scheduler, world);

            if (moved && this instanceof Character && this.sparkles != null) {
                this.sparkles._move(world, nextPos, scheduler, dir);
            }

        }
        return moved;
    }

    private boolean scheduleMoveEntity(Entity e, Point nextPos, EventScheduler scheduler, WorldModel world) {
        if (world.getBabas().contains(e)) {
            HashMap<Point, String> dic = new HashMap<>();
            dic.put(new Point(-1, 0), "baba");
            dic.put(new Point(0, -1), "baba_up");
            dic.put(new Point(1, 0), "baba_right");
            dic.put(new Point(0, 1), "baba_down");

            Point dir = this.position.getDir(nextPos);
            e.images = world.getImageStore().getImageList(dic.get(dir));
        }

        if (e instanceof Character && ((Character)e).getIsYou()) {
            scheduleDust(e.position, this.position.getDir(nextPos), scheduler, world);
        }
        scheduler.scheduleEvent(e,
                new FunctionAction(() -> world.moveEntity(e, nextPos, scheduler))
                , 0);
        return true;
    }

    private void scheduleDust(Point position, Point dir, EventScheduler scheduler, WorldModel world) {
        HashMap<Point, String> dic = new HashMap<>();
        dic.put(new Point(-1, 0), "dust_left");
        dic.put(new Point(0, -1), "dust_up");
        dic.put(new Point(1, 0), "dust_right");
        dic.put(new Point(0, 1), "dust_down");

        scheduler.scheduleEvent(this, new FunctionAction (() -> {
                    List<PImage> u = world.getImageStore().getImageList(dic.get(dir));
                    Dust x = world.createDust("dust", position, 100, 150,
                            world.getImageStore().getImageList(dic.get(dir)), scheduler);
                    world.getEntities().add(x);
                    x.scheduleActions(scheduler, world, world.getImageStore());
                }), 0);

    }

    private void scheduleDeathDust(Point position, EventScheduler scheduler, WorldModel world) {
        scheduler.scheduleEvent(this, new FunctionAction (() -> {
            List<PImage> u = world.getImageStore().getImageList("dust");
            Dust x = world.createDust("dust", position, 100, 100,
                    u, scheduler);
            world.getEntities().add(x);
            x.scheduleActions(scheduler, world, world.getImageStore());
        }), 0);
    }

    public void scheduleWin(EventScheduler scheduler, WorldModel world) {
        scheduler.scheduleEvent(this,
                new FunctionAction(() -> {
                    System.out.println("YOU WIN!!!!!");
                    WorldView.winScreen = world.getScreen().loadImage("images/winScreen1.png");
                }),
                1);
    }


    protected static boolean withinBounds(Point p, WorldModel world)
    {
        return p.getY() >= 0 && p.getY() < world.getNumRows() &&
                p.getX() >= 0 && p.getX() < world.getNumCols();
    }

    protected static boolean neighbors(Point p1, Point p2)
    {
        return p1.getX()+1 == p2.getX() && p1.getY() == p2.getY() ||
                p1.getX()-1 == p2.getX() && p1.getY() == p2.getY() ||
                p1.getX() == p2.getX() && p1.getY()+1 == p2.getY() ||
                p1.getX() == p2.getX() && p1.getY()-1 == p2.getY();
    }

}
