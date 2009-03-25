package batman.pathfinding;

import batman.utils.*;
import battlecode.common.MapLocation;

/**
 *
 * @author senu
 */
public class FastWeightedMapLocation implements
		Comparable<FastWeightedMapLocation>
{
	public int x,  y;
	public int distance,  heurestic;
	FastWeightedMapLocation parent;

	public FastWeightedMapLocation(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public FastWeightedMapLocation(int x, int y, int distance)
	{
		this.x = x;
		this.y = y;
		this.distance = distance;
	}

	public final int compareTo(FastWeightedMapLocation other)
	{
		int f = distance + heurestic;
		int fo = other.distance + other.heurestic;
		if (f < fo) {
			return -1;
		}
		if (f > fo) {
			return 1;
		}
		return 0;
	}

	@Override
	public final boolean equals(Object obj)
	{
		if (obj instanceof FastWeightedMapLocation) {
			return false;
		}
		return ((FastWeightedMapLocation) obj).x == x && ((FastWeightedMapLocation) obj).y == y;
	}

	@Override
	public int hashCode()
	{
		return x + 32 * y; //TODO?
	}
}
