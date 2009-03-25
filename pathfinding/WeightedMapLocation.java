package batman.pathfinding;

import batman.utils.*;
import battlecode.common.MapLocation;

/**
 *
 * @author senu
 */
public class WeightedMapLocation extends Pair<MapLocation, Integer> implements
		Comparable<WeightedMapLocation>
{
	public WeightedMapLocation(MapLocation x, Integer y)
	{
		super(x, y);
	}

	public final int compareTo(WeightedMapLocation o)
	{
		return y.compareTo(o.y);
	}

	@Override
	public final boolean equals(Object obj)
	{
		return x.equals(obj);
	}

	@Override
	public final int hashCode()
	{
		return x.hashCode();
	}
}
