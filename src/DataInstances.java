import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataInstances {
    private final static Logger LOGGER      = Logger.getLogger(DataInstances.class.getName());

    private VisualInfo          vi;
    private String              action;
    private char                my_side;
    private String              my_team;
    private static Pattern      msg_pattern = Pattern.compile("^\\(see\\s+(\\d+).*");
    private String              msg_number  = null;
    // in a visual message such as '(see 88 ((f l t) 66 -5)...etc ', the message
    // number is 88
    private String[]            actions     = Property.getInstance().getProperty("actions").split(";");

    public DataInstances(VisualInfo vi) {
        this.vi = vi;
        this.my_side = Property.getInstance().getProperty("player_side").charAt(0);
        this.my_team = Property.getInstance().getProperty("player_team");

        try {
            vi.parse();
            Matcher m = msg_pattern.matcher(vi.m_message);
            if (m.find())
                this.msg_number = m.group(1);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Encountered error parsing visual info", e);
        }
    }

    public VisualInfo getVi() {
        return vi;
    }

    public String getMsg_number() {
        return this.msg_number;
    }

    public String getAction() {
        String pureAction = null;
        for (String ac : actions) {
            if (this.action.startsWith("(" + ac)) {
                pureAction = ac;
                break;
            }
        }

        return pureAction;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private ObjectInfo getObject(String name) {

        for (int c = 0; c < vi.m_objects.size(); c++) {
            ObjectInfo object = (ObjectInfo) vi.m_objects.elementAt(c);
            if (object.m_type.compareTo(name) == 0)
                return object;
        }

        return null;
    }

    private ArrayList<ObjectInfo> getObjects(String name) {
        ArrayList<ObjectInfo> objects = new ArrayList<ObjectInfo>();
        for (int c = 0; c < vi.m_objects.size(); c++) {
            ObjectInfo obj = (ObjectInfo) vi.m_objects.elementAt(c);
            if (obj.m_type.compareTo(name) == 0)
                objects.add(obj);
        }
        return objects;
    }

    /**
     * This returns a see message information in the format of a ARFF instance
     * containing the input attributes along with the class attribute (the
     * action in this case) This method is typical for creating training data
     * instances.
     * 
     * @return
     */
    public String getARFFmessage() {
        return toString(true);
    }

    /**
     * This returns a see message information in the format of an ARFF instance
     * containing the input attributes but WITHOUT the class attribute (the
     * action). This method is typical for creating test data instances.
     * 
     * @return
     */
    public String getInputMessage() {
        return toString(false);
    }

    private String toString(boolean withAction) {
        StringBuilder sb = new StringBuilder();
        // sb.append(this.msg_number + "\n");
        String ball_distance = null, ball_direction = null, net_distance = null, net_direction = null;
        if (vi != null) {
            ArrayList<ObjectInfo> players = getObjects("player");
            if (getObject("ball") != null) {
                ball_distance = Float.toString(getObject("ball").m_distance);
                ball_direction = Float.toString(getObject("ball").m_direction);
            } else {
                ball_distance = "?";
                ball_direction = "?";
            }
            if (this.my_side == 'l') {
                if (getObject("goal r") != null) {
                    net_distance = Float.toString(getObject("goal r").m_distance);
                    net_direction = Float.toString(getObject("goal r").m_direction);
                } else {
                    net_distance = "?";
                    net_direction = "?";
                }
            } else if (this.my_side == 'r') {
                if (getObject("goal l") != null) {
                    net_distance = Float.toString(getObject("goal l").m_distance);
                    net_direction = Float.toString(getObject("goal l").m_direction);
                } else {
                    net_distance = "?";
                    net_direction = "?";
                }
            }
            if (!players.isEmpty()) {
                for (ObjectInfo i : players) {
                    PlayerInfo p = (PlayerInfo) i;
                    sb.append(ball_distance + "," + ball_direction + "," + net_distance + "," + net_direction + ","
                            + Float.toString(p.m_distance) + "," + Float.toString(p.m_direction) + ","
                            + (p.m_teamName.isEmpty() ? "?" : (p.m_teamName.equals(this.my_team) ? "friend" : "foe"))
                            + "," + (withAction == true ? getAction() : "?"));
                    sb.append("\n");
                }
            } else {
                sb.append(ball_distance + "," + ball_direction + "," + net_distance + "," + net_direction + "," + "?,"
                        + "?," + "?," + (withAction == true ? getAction() : "?") + "\n");
            }
        }

        return sb.toString();
    }
}