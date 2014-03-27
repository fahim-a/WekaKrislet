import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataInstance {
    private final static Logger LOGGER      = Logger.getLogger(DataInstance.class.getName());

    private VisualInfo          vi;
    private String              action;
    private char                my_side;
    private String              my_team;
    private static Pattern      msg_pattern = Pattern.compile("^\\(see\\s+(\\d+).*");
    private String              msg_number  = null;
    // in a visual message such as '(see 88 ((f l t) 66 -5)...etc ', the message
    // number is 88
    private String[]            actions     = Property.getInstance().getProperty("actions").split(";");
    private float				close		= Float.parseFloat(Property.getInstance().getProperty("close_distance"));

    public DataInstance(VisualInfo vi, String team, char side) {    	
        this.vi = vi;
        this.my_team = team;
        this.my_side = side;       

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
    	if(this.action == null) return null;
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
    
    
    private String getObjectAttributes(ObjectInfo objName){
    	String dis; //distance and direction
    	if (objName != null) {
            dis = Float.toString(objName.m_distance);
            //dir = Float.toString(objName.m_direction);
        } else {dis = "?"; /*dir = "?";*/}
    	//return (dis + "," + dir);
        return dis;
    }
    
    private String getPlayers(){
    	String returnString = null;
    	//how many close players we're interested in
    	int count = 2;
    	//first let's get all the players within sight
    	ArrayList<ObjectInfo> players = getObjects("player");
    	
    	//create a placeholder for the close players we find
    	ArrayList<PlayerInfo> close_players = new ArrayList<PlayerInfo>();
    	
    	//find the the first two close players
    	for(ObjectInfo obj : players){
    		PlayerInfo p = (PlayerInfo)obj;
    		if(p.m_distance <= close){
    			close_players.add(p);count--;
    			returnString += getObjectAttributes(p) + "," + (p.m_teamName.isEmpty() ? "?" : (p.m_teamName
						.equals(this.my_team) ? "friend" : "foe"));  
    			count--;
    		}
    		else{
    			returnString += "?,?,?";
    		}
    		if(count == 2) break;
    	}
    	    	
    	return returnString;
    }
    
   

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String ret = null;
        if (vi != null) {
        	
        	//get the ball
        	sb.append(getObjectAttributes(getObject("ball")));        	
        	sb.append(",");        	
        	
        	//if you are the left side, get the right ball or vice versa
        	sb.append(getObjectAttributes((this.my_side == 'l' ? getObject("goal r") : getObject("goal l"))));        	
        	sb.append(",");
        	
        	//sb.append(getPlayers());sb.append(",");
        	
        	//get the action, whether it exists or needs to be predicted.
        	sb.append((this.getAction() != null && !this.getAction().isEmpty()) ? this.getAction() : "?");
        	
        	ret = sb.toString();        	
        	
        	
        	if(ret.endsWith(",")) ret = ret.substring(0, sb.length() - 1);
        	        	       	
        	ret = ret + "\n";
        	
        }

        return ret;
    }
}