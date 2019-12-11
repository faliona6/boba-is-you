import processing.core.PImage;

import java.util.List;

public class Dust extends Entity {

    private WorldModel world;
    private EventScheduler scheduler;

    public Dust(String id, Point position,
                List<PImage> images, int actionPeriod, int animationPeriod, WorldModel world,
                EventScheduler scheduler) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.world = world;
        this.scheduler = scheduler;
    }

    @Override
    public void nextImage() {

        if (imageIndex >= this.images.size() - 1) {
            world.removeEntity(this, scheduler);
        }
        else {
            this.imageIndex = this.imageIndex + 1;
        }
    }
}