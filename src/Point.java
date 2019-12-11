final class Point
{
   private final int x;
   private final int y;

   public Point(int x, int y)
   {
      this.x = x;
      this.y = y;
   }

   //Get Functions
   public int getX() { return this.x; }
   public int getY() { return this.y; }

   public String toString()
   {
      return "(" + x + "," + y + ")";
   }

   public boolean equals(Object other)
   {
      return other instanceof Point &&
         ((Point)other).x == this.x &&
         ((Point)other).y == this.y;
   }

   public int hashCode()
   {
      int result = 17;
      result = result * 31 + x;
      result = result * 31 + y;
      return result;
   }

   public boolean adjacent(Point p2)
   {
      return (this.x == p2.x && Math.abs(this.y - p2.y) == 1) ||
              (this.y == p2.y && Math.abs(this.x - p2.x) == 1);
   }

   public int distanceSquared(Point p2)
   {
      int deltaX = this.x - p2.x;
      int deltaY = this.y - p2.y;

      return deltaX * deltaX + deltaY * deltaY;
   }

   public Point midPoint(Point p2) {
      return new Point((x + p2.x) / 2, (y + p2.y) / 2);
   }

   public Point add(Point p2) {
      return new Point(this.x + p2.x, this.y + p2.y);
   }

   // Returns Point direction of where the conj. is given point of
   // moved noun/verb

   public Point getDir(Point p2) {
      Point x = new Point(p2.x - this.x, p2.y - this.y);
      return x;
   }


}
