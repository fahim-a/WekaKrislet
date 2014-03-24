import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WekaLogger {
    private static FileHandler     fileTxt;
    private static SimpleFormatter formatterTxt;
    private static ConsoleHandler  console;

    public static void setup(String systemName) throws IOException {

        // Get the global logger to configure it
        Logger logger = Logger.getLogger("");
        Handler[] existingHandlers = logger.getHandlers();
        if (existingHandlers != null && existingHandlers.length > 0) {
            for (Handler handler : existingHandlers)
                logger.removeHandler(handler);
        }
        logger.setLevel(Level.ALL);

        // create txt Formatter
        formatterTxt = new SimpleFormatter() {
            boolean firstLog = true;

            @Override
            public String format(LogRecord record) {
                if (firstLog) {
                    firstLog = false;
                    return record.getMessage();
                } else
                    return super.format(record);
            }
        };

        File logFile = new File("weka_log.txt");
        boolean fileExistsAlready = logFile.exists();

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

        // print a separator so it becomes easier to find the logs we are
        // interested in
        String line = systemName + " - Log session started " + new Date() + "\n";
        String pad = buildPad(line.length(), '=');
        StringBuilder sb = new StringBuilder();
        if (fileExistsAlready)
            sb.append("\n\n\n\n");
        sb.append(pad);
        sb.append(line);
        sb.append(pad);
        sb.append("\n\n");
        logger.log(Level.FINE, sb.toString());
    }

    public static String buildPad(int capacity, char c) {
        StringBuilder outputBuffer = new StringBuilder(capacity);
        for (int i = 0; i < capacity - 1; i++) {
            outputBuffer.append(c);
        }
        outputBuffer.append('\n');
        return outputBuffer.toString();
    }
}