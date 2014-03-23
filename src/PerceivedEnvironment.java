import java.io.Serializable;

import weka.core.Instance;
import weka.core.SparseInstance;

@SuppressWarnings("serial")
public class PerceivedEnvironment implements Serializable {
	public static double UNKNOWN_VALUE = -1d;

	private double distanceToBall = UNKNOWN_VALUE;
	private double directionToBall = UNKNOWN_VALUE;
	private double distanceToNet = UNKNOWN_VALUE;
	private double directionToNet = UNKNOWN_VALUE;
	private double distanceToPlayer = UNKNOWN_VALUE;
	private double directionToPlayer = UNKNOWN_VALUE;
	private PlayerTeam playerTeam = null;

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
		this.distanceToBall = distanceToBall;
		this.directionToBall = directionToBall;
		this.distanceToNet = distanceToNet;
		this.directionToNet = directionToNet;
		this.distanceToPlayer = distanceToPlayer;
		this.directionToPlayer = directionToPlayer;
		this.playerTeam = playerTeam;
	}

	public PerceivedEnvironment() {
		super();
	}

	public double getDistanceToBall() {
		return distanceToBall;
	}

	public void setDistanceToBall(double distanceToBall) {
		this.distanceToBall = distanceToBall;
	}

	public double getDirectionToBall() {
		return directionToBall;
	}

	public void setDirectionToBall(double directionToBall) {
		this.directionToBall = directionToBall;
	}

	public double getDistanceToNet() {
		return distanceToNet;
	}

	public void setDistanceToNet(double distanceToNet) {
		this.distanceToNet = distanceToNet;
	}

	public double getDistanceToPlayer() {
		return distanceToPlayer;
	}

	public void setDistanceToPlayer(double distanceToPlayer) {
		this.distanceToPlayer = distanceToPlayer;
	}

	public double getDirectionToPlayer() {
		return directionToPlayer;
	}

	public void setDirectionToPlayer(double directionToPlayer) {
		this.directionToPlayer = directionToPlayer;
	}

	public PlayerTeam getPlayerTeam() {
		return playerTeam;
	}

	public void setPlayerTeam(PlayerTeam playerTeam) {
		this.playerTeam = playerTeam;
	}

	public double getDirectionToNet() {
		return directionToNet;
	}

	public void setDirectionToNet(double directionToNet) {
		this.directionToNet = directionToNet;
	}

	public Instance buildWekaInstance(Instance sampleInstance) {
		SparseInstance si = new SparseInstance(sampleInstance);
		si.setDataset(sampleInstance.dataset());

		if (distanceToBall != UNKNOWN_VALUE)
			si.setValue(0, distanceToBall);
		else
			si.setMissing(0);

		if (directionToBall != UNKNOWN_VALUE)
			si.setValue(1, directionToBall);
		else
			si.setMissing(1);

		if (distanceToNet != UNKNOWN_VALUE)
			si.setValue(2, distanceToNet);
		else
			si.setMissing(2);

		if (directionToNet != UNKNOWN_VALUE)
			si.setValue(3, directionToNet);
		else
			si.setMissing(3);

		if (distanceToPlayer != UNKNOWN_VALUE)
			si.setValue(4, distanceToPlayer);
		else
			si.setMissing(4);

		if (directionToPlayer != UNKNOWN_VALUE)
			si.setValue(5, directionToPlayer);
		else
			si.setMissing(5);

		if (playerTeam != null)
			si.setValue(6, PlayerTeam.FOE.equals(playerTeam) ? 1 : 0);
		else
			si.setMissing(6);

		si.setMissing(7);

		return si;
	}
}