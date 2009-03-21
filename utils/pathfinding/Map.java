package batman.utils.pathfinding;

import battlecode.common.MapLocation;
import java.util.HashMap;

/**
 * Mapa do A*, dla naziemnych.
 * @author senu
 */
public class Map
{
	/** Stan lokacji */
	public enum LocState
	{
		Unknown,
		Bad, //nie mozna chodzic
		Good,
		Occupied
	}
	private HashMap<MapLocation, LocState> map = new HashMap<MapLocation, LocState>();

	public final void addLoc(MapLocation loc, LocState state)
	{
		map.put(loc, state);
	}

	public final void getState(MapLocation loc)
	{
		map.get(loc);
	}
}
