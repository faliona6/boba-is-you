import processing.core.PImage;

import java.util.List;

abstract class Entity {

    protected String id;
    protected Point position;
    protected List<PImage> images;
    protected int actionPeriod;
    protected int animationPeriod;
    protected int imageIndex;
    protected Moveable sparkles;



    // Getters
    public String getId() { return this.id; }
    public Point getPosition() { return this.position; }
    public List<PImage> getImages() { return this.images; }
    public int getAnimationPeriod() { return this.animationPeriod; }
    public int getImageIndex() { return this.imageIndex; }
    public int getActionPeriod() { return this.actionPeriod; }


    // Setters
    public void setPosition(Point position) {
        this.position = position;
    }

    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {

    }

    public Action createActivityAction(WorldModel world, ImageStore imageStore)
    {
        return new Activity(this, world, imageStore);
    }

    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this,
                createActivityAction(world, imageStore),
                this.actionPeriod);
        scheduler.scheduleEvent(this, createAnimationAction(0),
                this.getAnimationPeriod());
    }

    public Action createAnimationAction(int repeatCount)
    {
        return new Animation(this, repeatCount);
    }

    public void nextImage() {

        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

}
