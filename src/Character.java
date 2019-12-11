import processing.core.PImage;

import java.util.List;

public class Character extends Moveable{

    protected boolean isStop = false;
    protected boolean isYou = false;
    protected boolean isWin = false;
    protected boolean isDeath = false;
    protected boolean isPush = false;

    public boolean getIsStop() { return this.isStop; }
    public boolean getIsYou() { return this.isYou; }
    public boolean getIsWin() { return this.isWin; }
    public boolean getIsDeath() { return this.isDeath; }
    public boolean getIsPush() { return this.isPush; }

    public void setIsYou(boolean b) {
        this.isYou = b;
    }
    public void setIsStop(boolean b) {
        this.isStop = b;
    }
    public void setIsPush(boolean b) {
        this.isPush = b;
    }
    public void setIsWin(boolean b) {
        this.isWin = b;
    }
    public void setIsDeath(boolean b) {
        this.isDeath = b;
    }

    public Character(String id, Point position,
                List<PImage> images, int actionPeriod, int animationPeriod) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;

    }

    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
    }
    
    public void setAllAttributesFalse() {
        isStop = false;
        isYou = false;
        isWin = false;
        isDeath = false;
        isPush = false;
    }

}
