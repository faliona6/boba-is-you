import processing.core.PImage;

import java.util.List;

public class Verb extends Text{

    public Verb(String id, Point position,
                List<PImage> images, int actionPeriod, int animationPeriod,
                String type) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.type = type;
    }

}
