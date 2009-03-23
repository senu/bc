package batman.pathfinding;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

/**
 *
 * @author senu
 */
public interface IPathFinder {
	/**
	 * Znajdz droge z from do to.
	 *
	 * @param map informacje o planszy.
	 * @param rt używany przy rozpatrywaniu pól planszy (air/ground, wysokosc)
	 *
	 * @return sciezka z from to to, albo pusta sciezka
	 */
	public Path findPath(MapLocation from, MapLocation to, GameMap map, RobotType rt);
}
