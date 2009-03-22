package batman.pathfinding;

import battlecode.common.Clock;
import battlecode.common.Robot;

/**
 *
 * @author senu
 */
public class MapTile
{
	/** Stan lokacji */
	public enum LocState
	{
		Unknown,
		Bad, //nie mozna chodzic
		Good,
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
	public Robot groundRobot;
	public Robot airRobot;
}
