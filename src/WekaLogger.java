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
    private static FileHandler     traceFileTxt;
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

        // log everything but FINEST to this log file
        String logFileName = "weka_" + systemName + "_log.txt";
        fileTxt = new FileHandler(logFileName, true);
        fileTxt.setFormatter(formatterTxt);
        fileTxt.setLevel(Level.FINER);
        logger.addHandler(fileTxt);

        // log FINEST to this log file
        String traceLogFileName = "weka_" + systemName + "_log.txt";
        traceFileTxt = new FileHandler(traceLogFileName, true);
        traceFileTxt.setFormatter(formatterTxt);
        traceFileTxt.setLevel(Level.FINEST);
        logger.addHandler(traceFileTxt);

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