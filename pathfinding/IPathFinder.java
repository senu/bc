package batman.pathfinding;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

/**
 *
 * @author senu
 */
public interface IPathFinder {
	public Path findPath(MapLocation from, MapLocation to, GameMap map, RobotType rt);
}
