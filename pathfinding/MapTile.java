package batman.pathfinding;

import batman.utils.SimpleRobotInfo;
import battlecode.common.Clock;
import battlecode.common.Robot;
import java.io.Serializable;

/**
 *
 * @author senu
 */
public class MapTile implements Serializable
{
	public static final long serialVersionUID = 1L;

	/** Stan lokacji */
	public enum LocState
	{
		Unknown,
		Ground,
		Air,
		Bad, //nie mozna chodzic
	}

	public MapTile()
	{
	}

	public MapTile(LocState state)
	{
		this.state = state;
		init();
	}

	private void init()
	{
		this.roundSeen = Clock.getRoundNum();
	}
	/** Kiedy ostani razy skanowano lokacje */
	public int roundSeen;
	public int height;
	public int blockCount;
	public LocState state;
	public SimpleRobotInfo groundRobot;
	public SimpleRobotInfo airRobot;
}
