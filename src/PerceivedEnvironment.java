import java.io.Serializable;

import weka.core.Instance;
import weka.core.SparseInstance;

/**
 * Note: Ensure the class attributes are defined in the exact same order in the
 * properties file
 * 
 */
@SuppressWarnings("serial")
public class PerceivedEnvironment implements Serializable {
    public static double UNKNOWN_VALUE = -1d;

    private double       ball_dis      = UNKNOWN_VALUE;
    private double       ball_dir      = UNKNOWN_VALUE;
    private double       net_dis       = UNKNOWN_VALUE;
    private double       net_dir       = UNKNOWN_VALUE;
    private double       player_1_dis  = UNKNOWN_VALUE;
    private double       player_1_dir  = UNKNOWN_VALUE;
    private PlayerTeam   player_1_team = null;
    private double       player_2_dis  = UNKNOWN_VALUE;
    private double       player_2_dir  = UNKNOWN_VALUE;
    private PlayerTeam   player_2_team = null;

    private PerceivedEnvironment() {
        super();
    }

    /**
     * Generates the class based on a string version of Krislet's perceived
     * environment
     * 
     * @param envString
     *            (e.g. '0.4,-88.0,26.8,3.0,2.7,-80.0,?,2.7,-80.0,?')
     */
    public PerceivedEnvironment(String envString) {
        this();
        if (envString != null && !envString.isEmpty()) {
            String[] input = envString.split(",");

            if (input != null && input.length == 10) {

                // need to try catch each attempt to parse an attribute. In the
                // event one fails, mark that specific attribute as being
                // unknown

                try {
                    ball_dis = Double.parseDouble(input[0]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    ball_dir = Double.parseDouble(input[1]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    net_dis = Double.parseDouble(input[2]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    net_dir = Double.parseDouble(input[3]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    player_1_dis = Double.parseDouble(input[4]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    player_1_dir = Double.parseDouble(input[5]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    player_1_team = PlayerTeam.valueOf(input[6]);
                } catch (IllegalArgumentException | NullPointerException e) {
                }

                try {
                    player_2_dis = Double.parseDouble(input[7]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    player_2_dir = Double.parseDouble(input[8]);
                } catch (NumberFormatException | NullPointerException e) {
                }

                try {
                    player_2_team = PlayerTeam.valueOf(input[9]);
                } catch (IllegalArgumentException | NullPointerException e) {
                }
            } else {
                throw new IllegalArgumentException("Invalid perceived environment string: " + envString);
            }
        }
    }

    public Instance buildWekaInstance(Instance sampleInstance) {
        SparseInstance si = new SparseInstance(sampleInstance);
        si.setDataset(sampleInstance.dataset());

        // if the values are set, use them. If not, mark as missing

        if (ball_dis != UNKNOWN_VALUE)
            si.setValue(0, ball_dis);
        else
            si.setMissing(0);

        if (ball_dir != UNKNOWN_VALUE)
            si.setValue(1, ball_dir);
        else
            si.setMissing(1);

        if (net_dis != UNKNOWN_VALUE)
            si.setValue(2, net_dis);
        else
            si.setMissing(2);

        if (net_dir != UNKNOWN_VALUE)
            si.setValue(3, net_dir);
        else
            si.setMissing(3);

        if (player_1_dis != UNKNOWN_VALUE)
            si.setValue(4, player_1_dis);
        else
            si.setMissing(4);

        if (player_1_dir != UNKNOWN_VALUE)
            si.setValue(5, player_1_dir);
        else
            si.setMissing(5);

        if (player_1_team != null)
            si.setValue(6, PlayerTeam.FOE.equals(player_1_team) ? 1 : 0);
        else
            si.setMissing(6);

        if (player_2_dis != UNKNOWN_VALUE)
            si.setValue(7, player_1_dis);
        else
            si.setMissing(7);

        if (player_2_dir != UNKNOWN_VALUE)
            si.setValue(8, player_1_dir);
        else
            si.setMissing(8);

        if (player_2_team != null)
            si.setValue(9, PlayerTeam.FOE.equals(player_2_team) ? 1 : 0);
        else
            si.setMissing(9);

        // the action is what we are trying to determine! Explicitly mark it as
        // missing
        si.setMissing(10);

        return si;
    }
}