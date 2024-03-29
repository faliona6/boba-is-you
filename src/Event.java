final class Event
{
   private Action action;
   private long time;
   private Entity entity;

   public Event(Action action, long time, Entity entity)
   {
      this.action = action;
      this.time = time;
      this.entity = entity;
   }

   //Get Functions
   public Action getAction() { return this.action; }
   public long getTime() { return this.time; }
   public Entity getEntity() { return this.entity; }
}
