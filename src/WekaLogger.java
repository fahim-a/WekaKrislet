import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WekaLogger {
    private static FileHandler     fileTxt;
    private static SimpleFormatter formatterTxt;
    private static ConsoleHandler  console;

    static public void setup() throws IOException {

        // Get the global logger to configure it
        Logger logger = Logger.getLogger("");
        Handler[] existingHandlers = logger.getHandlers();
        if (existingHandlers != null && existingHandlers.length > 0) {
            for (Handler handler : existingHandlers)
                logger.removeHandler(handler);
        }
        logger.setLevel(Level.ALL);

        // create txt Formatter
        formatterTxt = new SimpleFormatter();

        // log everything to file
        fileTxt = new FileHandler("weka_log.txt", true);
        fileTxt.setFormatter(formatterTxt);
        fileTxt.setLevel(Level.ALL);
        logger.addHandler(fileTxt);

        // only log info (and above) msgs in the console
        console = new ConsoleHandler();
        console.setFormatter(formatterTxt);
        console.setLevel(Level.INFO);
        logger.addHandler(console);
    }
}