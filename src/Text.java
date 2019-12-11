public abstract class Text extends Moveable{
    protected String type; // "is", "and", etc.
    protected boolean activated = false;

    public String getType() {
        return this.type;
    }
}
