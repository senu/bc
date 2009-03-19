package batman.utils;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import java.util.Random;

/**
 *
 * @author senu
 */
public class Utils
{
	public final static MapLocation closest(MapLocation[] locations, MapLocation from)
	{
		MapLocation best = null;
		int bd = 100000;

		for (MapLocation loc : locations) {
			int dist = from.distanceSquaredTo(loc);
			if (dist < bd) {
				bd = dist;
				best = loc;
			}
		}

		return best;
	}

	public final static MapLocation add(MapLocation from, int dx, int dy)
	{
		return new MapLocation(from.getX() + dx, from.getY() + dy);
	}

	public final static MapLocation randLocRange(MapLocation from, int dx, int dy, Random r)
	{
		return Utils.add(from, r.nextInt(2 * dx) - dx, r.nextInt(2 * dy) - dy);
	}
	public final static Direction[] movableDirections() {
		return new Direction[] {
					Direction.EAST,
					Direction.NORTH,
					Direction.NORTH_EAST, Direction.NORTH_WEST,
					Direction.SOUTH,
					Direction.SOUTH_EAST, Direction.SOUTH_WEST,
					Direction.WEST
		};

	}
}
