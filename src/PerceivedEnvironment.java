import java.io.Serializable;

import weka.core.Instance;
import weka.core.SparseInstance;

@SuppressWarnings("serial")
public class PerceivedEnvironment implements Serializable {
	public static double UNKNOWN_VALUE = -1d;

	private double ball_dis = UNKNOWN_VALUE;
	private double ball_dir = UNKNOWN_VALUE;
	private double net_dis = UNKNOWN_VALUE;
	private double net_dir = UNKNOWN_VALUE;
	private double player_dis = UNKNOWN_VALUE;
	private double player_dir = UNKNOWN_VALUE;
	private PlayerTeam player_team = null;

	/**
	 * <code>
	 * 
	 * @attribute ball_distance NUMERIC
	 * @attribute ball_direction NUMERIC
	 * @attribute net_distance NUMERIC
	 * @attribute net_direction NUMERIC
	 * @attribute player_distance NUMERIC
	 * @attribute player_direction NUMERIC
	 * @attribute player_team {friend,foe}
	 * @attribute action {dash,turn,kick} </code>
	 * 
	 * @param distanceToBall
	 * @param directionToBall
	 * @param distanceToNet
	 * @param distanceToPlayer
	 * @param directionToPlayer
	 * @param playerTeam
	 */

	public PerceivedEnvironment(double distanceToBall, double directionToBall,
			double distanceToNet, double directionToNet,
			double distanceToPlayer, double directionToPlayer,
			PlayerTeam playerTeam) {
		super();
		this.ball_dis = distanceToBall;
		this.ball_dir = directionToBall;
		this.net_dis = distanceToNet;
		this.net_dir = directionToNet;
		this.player_dis = distanceToPlayer;
		this.player_dir = directionToPlayer;
		this.player_team = playerTeam;
	}

	public PerceivedEnvironment() {
		super();
	}

	/**
	 * Generates the class based on a string version of Krislet's perceived
	 * environment
	 * 
	 * @param envString
	 *            (e.g. '0.4,-88.0,26.8,3.0,2.7,-80.0,?')
	 */
	public PerceivedEnvironment(String envString) {
		super();
		if (envString != null && !envString.isEmpty()) {
			String[] input = envString.split(",");

			if (input != null && input.length == 7) {

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
					player_dis = Double.parseDouble(input[4]);
				} catch (NumberFormatException | NullPointerException e) {
				}

				try {
					player_dir = Double.parseDouble(input[5]);
				} catch (NumberFormatException | NullPointerException e) {
				}

				try {
					player_team = PlayerTeam.valueOf(input[6]);
				} catch (IllegalArgumentException | NullPointerException e) {
				}
			}
		}
	}

	public double getDistanceToBall() {
		return ball_dis;
	}

	public void setDistanceToBall(double distanceToBall) {
		this.ball_dis = distanceToBall;
	}

	public double getDirectionToBall() {
		return ball_dir;
	}

	public void setDirectionToBall(double directionToBall) {
		this.ball_dir = directionToBall;
	}

	public double getDistanceToNet() {
		return net_dis;
	}

	public void setDistanceToNet(double distanceToNet) {
		this.net_dis = distanceToNet;
	}

	public double getDistanceToPlayer() {
		return player_dis;
	}

	public void setDistanceToPlayer(double distanceToPlayer) {
		this.player_dis = distanceToPlayer;
	}

	public double getDirectionToPlayer() {
		return player_dir;
	}

	public void setDirectionToPlayer(double directionToPlayer) {
		this.player_dir = directionToPlayer;
	}

	public PlayerTeam getPlayerTeam() {
		return player_team;
	}

	public void setPlayerTeam(PlayerTeam playerTeam) {
		this.player_team = playerTeam;
	}

	public double getDirectionToNet() {
		return net_dir;
	}

	public void setDirectionToNet(double directionToNet) {
		this.net_dir = directionToNet;
	}

	public Instance buildWekaInstance(Instance sampleInstance) {
		SparseInstance si = new SparseInstance(sampleInstance);
		si.setDataset(sampleInstance.dataset());

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

		if (player_dis != UNKNOWN_VALUE)
			si.setValue(4, player_dis);
		else
			si.setMissing(4);

		if (player_dir != UNKNOWN_VALUE)
			si.setValue(5, player_dir);
		else
			si.setMissing(5);

		if (player_team != null)
			si.setValue(6, PlayerTeam.FOE.equals(player_team) ? 1 : 0);
		else
			si.setMissing(6);

		si.setMissing(7);

		return si;
	}
}