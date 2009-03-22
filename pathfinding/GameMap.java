package batman.pathfinding;

import battlecode.common.MapLocation;
import java.util.HashMap;

/**
 * Mapa do A*, dla naziemnych.
 * @author senu
 */
public class GameMap
{
	private HashMap<MapLocation, MapTile> map = new HashMap<MapLocation, MapTile>();

	public final void setTile(MapLocation loc, MapTile state)
	{
		map.put(loc, state);
	}

	public final MapTile getTile(MapLocation loc)
	{
		return map.get(loc);
	}

	public void debug_print()
	{
//		MapLocation min =
//		int mx, my, Mx=, My;
		for (MapLocation loc : map.keySet()) {
		}
	}
}
