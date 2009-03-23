package batman.pathfinding;

import batman.utils.DebugUtils;
import batman.utils.Utils;
import batman.utils.WeightedMapLocation;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author senu
 */
public class AStar implements IPathFinder
{
	public Path findPath(MapLocation from, MapLocation to, GameMap map, RobotType rt)
	{
		PriorityQueue<WeightedMapLocation> queue = new PriorityQueue<WeightedMapLocation>();
		queue.add(new WeightedMapLocation(from, 0));

		// -> dist, h
		HashMap<MapLocation, Integer> costs = new HashMap<MapLocation, Integer>(100);
		HashMap<MapLocation, MapLocation> parents = new HashMap<MapLocation, MapLocation>(100);
		Set<MapLocation> byl = new HashSet<MapLocation>(100);

		MapLocation cur;

		if (from.equals(to)) {
			return Path.emptyPath;
		}

		int dcost;
		costs.put(from, new Integer(0));
		parents.put(from, from);

		int count = 0;

		while (!queue.isEmpty() && count < 600) {
			count++; //TODO
			WeightedMapLocation wcur = queue.remove();

			cur = wcur.x;
			dcost = costs.get(cur);

			//TODO

			if (byl.contains(cur) || (map.hasLoc(cur) && map.getTile(cur).state != MapTile.LocState.Ground)) {
				continue;
			}
			byl.add(cur);


			if (cur.equals(to)) {
				Path path = new Path();
				MapLocation parent = parents.get(cur);
				while (!parent.equals(from)) {
					path.getPath().add(cur);
					cur = parent;
					parent = parents.get(cur);
				//    debug_print("parent loop: %s -> %s -> %s [%s]", from, cur, to, parent);
				}

				path.getPath().add(cur);
				path.getPath().add(parent);

				java.util.Collections.reverse(path.getPath());

//				rc.setIndicatorString(0, from.toString());
//				rc.setIndicatorString(2, to.toString());
				return path;
			//TODOreturn cur;
			}

			for (Direction dir : Utils.movableDirections()) {

				MapLocation next = cur.add(dir);

				if (byl.contains(next)) {
					continue;
				}

				int ndcost = dcost + 1;
				int nhcost = Math.abs(to.getX() - next.getX()) + Math.abs(to.getY() - next.getY());

				if (costs.containsKey(next)) {
					if (costs.get(next) > ndcost) {
						costs.remove(next);

						costs.put(next, ndcost);
						parents.put(next, cur);
						queue.add(new WeightedMapLocation(next, ndcost + nhcost)); //TODO niepotrzebne czesto
					}
				} else {
					parents.put(next, cur);
					costs.put(next, ndcost);
					queue.add(new WeightedMapLocation(next, ndcost + nhcost)); //TODO niepotrzebne czesto
				}
			}
		}

		DebugUtils.debug_print("empty queue");
		return Path.emptyPath;
	}
}
