import java.util.List;
import processing.core.PImage;

final class Background
{
   private String id;
   private List<PImage> images;
   private int imageIndex;

   public Background(String id, List<PImage> images)
   {
      this.id = id;
      this.images = images;
   }
   //Get Functions
   public String getId() {return this.id;}
   public List<PImage> getImages() {return this.images;}
   public int getImageIndex() {return this.imageIndex;}
}
