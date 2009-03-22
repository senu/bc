package batman.utils;

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

	public int compareTo(WeightedMapLocation o)
	{
		return y.compareTo(o.y);
	}

	@Override
	public boolean equals(Object obj)
	{
		return x.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return x.hashCode();
	}
}
