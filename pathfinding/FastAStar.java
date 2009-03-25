package batman.pathfinding;

import batman.utils.DebugUtils;
import batman.utils.MapUtils;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Zakodowany lepiej astar.
 * Bo 6000 bytecodu to za malo.
 * @author senu
 */
public class FastAStar implements IPathFinder
{
	public Path findPath(MapLocation from, MapLocation to, GameMap map, RobotType rt)
	{
		HashMap<MapLocation, FastWeightedMapLocation> nodes = new HashMap<MapLocation, FastWeightedMapLocation>(100);
		Set<MapLocation> byl = new HashSet<MapLocation>(100);
		SortedSet<FastWeightedMapLocation> openSet = new TreeSet<FastWeightedMapLocation>();

		MapLocation cur;

		DebugUtils.debug_print("fast astar start");

		if (from.equals(to)) {
			return Path.emptyPath;
		}

		int dcost;
		openSet.add(new FastWeightedMapLocation(from.getX(), from.getY(), 0));

		int count = 0;

		while (!openSet.isEmpty() && count < 300) {
			count++; //TODO
//			DebugUtils.debug_print("fast astar bc2: %d", Clock.getBytecodeNum());

			FastWeightedMapLocation wcur = openSet.first();
			cur = new MapLocation(wcur.x, wcur.y);

			if (cur.equals(to)) {//TODO
				Path path = new Path();
//				MapLocation parent = nodes.get(wcur).parent;
				while (wcur.parent != wcur) {
					path.getPath().add(new MapLocation(wcur.x, wcur.y));
					if (wcur.parent == null) {
						break;
					}
					wcur = wcur.parent;
				}

				java.util.Collections.reverse(path.getPath());
				return path;
			}

			byl.add(cur);
			openSet.remove(wcur);
			DebugUtils.debug_print("fast astar bc3: %d %s", Clock.getBytecodeNum(), cur.toString());

			for (Direction dir : MapUtils.movableDirections) {

				MapLocation next = cur.add(dir);
				FastWeightedMapLocation wnext = nodes.get(next);
				if (wnext == null) {
					wnext = new FastWeightedMapLocation(next.getX(), next.getY());
				}
				if (canMoveIn(map, cur)) {

					int ndcost = wcur.distance + 1; //TODO height, sqrt2

					if (ndcost < wnext.distance) {
						if (openSet.remove(wnext)) {
							DebugUtils.debug_print("ok, removed worse");
						}
				//		byl.remove(wnext); //TODO wtf
					}

					if (!openSet.contains(wnext) && !byl.contains(wnext)) {
						wnext.distance = ndcost;
						wnext.heurestic = Math.abs(to.getX() - next.getX()) + Math.abs(to.getY() - next.getY());
						wnext.parent = wcur;
						openSet.add(wnext);
					}

				}

			}
		}

//TODO		DebugUtils.debug_print("empty queue");
		return Path.emptyPath;
	}

	protected boolean canMoveIn(GameMap map, MapLocation loc)
	{
		if (!map.hasLoc(loc)) {
			return true;
		}

		MapTile tile = map.getTile(loc);
		return tile.state == MapTile.LocState.Ground && tile.groundRobot == null;
	}
}
