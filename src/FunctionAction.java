
import java.util.function.Function;

public class FunctionAction implements Action{
    private Runnable function;

    public FunctionAction(Runnable function)
    {
        this.function = function;
    }

    public void executeAction(EventScheduler e)
    {
        function.run();
    }
}
