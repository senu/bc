package batman.pathfinding;

import batman.utils.DebugUtils;
import batman.utils.MapUtils;
import batman.pathfinding.WeightedMapLocation;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author senu
 */
public class AStar implements IPathFinder
{
	class CostAndParent
	{
		public CostAndParent(int cost, MapLocation parent)
		{
			this.cost = cost;
			this.parent = parent;
		}
		public int cost;
		public MapLocation parent;
	}

	public Path findPath(MapLocation from, MapLocation to, GameMap map, RobotType rt)
	{
		TreeSet<WeightedMapLocation> queue = new TreeSet<WeightedMapLocation>();
		//PriorityQueue<WeightedMapLocation> queue = new PriorityQueue<WeightedMapLocation>();
		queue.add(new WeightedMapLocation(from, 0));

		// -> dist, h
//		HashMap<MapLocation, Integer> costs = new HashMap<MapLocation, Integer>(100);
//		HashMap<MapLocation, MapLocation> parents = new HashMap<MapLocation, MapLocation>(100);
		HashMap<MapLocation, CostAndParent> nodes = new HashMap<MapLocation, CostAndParent>(100);
		//Set<MapLocation> byl = new HashSet<MapLocation>(100);
		//zaklada sie, ze from nalezy do mapy


		int Mx = map.Mx;
		int My = map.My;
		int mx = map.mx;
		int my = map.my;
		if (to.getX() < mx) {
			mx = to.getX();
		}
		if (to.getX() > Mx) {
			Mx = to.getX();
		}
		if (to.getY() < my) {
			my = to.getY();
		}
		if (to.getY() > My) {
			My = to.getY();
		}
		//
		if (from.getX() < mx) {
			mx = from.getX();
		}
		if (from.getX() > Mx) {
			Mx = from.getX();
		}
		if (from.getY() < my) {
			my = from.getY();
		}
		if (from.getY() > My) {
			My = from.getY();
		}

		mx -= 25;
		my -= 25;
		Mx += 25;
		My += 25;

		boolean[][] byl = new boolean[My - my + 1][Mx - mx + 1];

		MapLocation cur;

		DebugUtils.debug_print("astar start");

		if (from.equals(to)) {
			return Path.emptyPath;
		}

		int dcost;
		nodes.put(from, new CostAndParent(0, from));

		int count = 0;

		while (!queue.isEmpty() && count < 300) {
			count++; //TODO
//			DebugUtils.debug_print("astar bc1: %d", Clock.getBytecodeNum());
			WeightedMapLocation wcur = queue.first();
			queue.remove(wcur);

			cur = wcur.x;
			dcost = nodes.get(cur).cost;

			//TODO
//			DebugUtils.debug_print("astar bc3: %d %s", Clock.getBytecodeNum(), cur.toString());
			int x = cur.getX();
			int y = cur.getY();
			if (y - my < 0 || My - y < 0 || x - mx < 0 || Mx - x < 0) {
				continue;
			}
			if (byl[y - my][x - mx] || !canMoveIn(map, cur)) {
				continue;
			}


			//byl.add(cur);
			byl[y - my][x - mx] = true;


//			DebugUtils.debug_print("astar bc2: %d", Clock.getBytecodeNum());
			if (cur.equals(to)) {
				Path path = new Path();
				MapLocation parent = nodes.get(cur).parent;
				while (!parent.equals(from)) {
					path.getPath().add(cur);
					cur = parent;
					parent = nodes.get(cur).parent;
				//    debug_print("parent loop: %s -> %s -> %s [%s]", from, cur, to, parent);
				}

				path.getPath().add(cur);
				path.getPath().add(parent);

				java.util.Collections.reverse(path.getPath());

//				DebugUtils.debug_print("astar end");
				return path;
			}

			for (Direction dir : MapUtils.movableDirections) {

//				DebugUtils.debug_print("astar bc3: %d", Clock.getBytecodeNum());
				MapLocation next = cur.add(dir);
//				DebugUtils.debug_print("astar bc3b: %d", Clock.getBytecodeNum());

				x = next.getX();
				y = next.getY();
				if (y - my < 0 || My - y < 0 || x - mx < 0 || Mx - x < 0) {
					continue;
				}
				if (byl[y - my][x - mx]) {
					continue;
				}

				int ndcost = dcost + 1;
				int nhcost = Math.abs(to.getX() - next.getX()) + Math.abs(to.getY() - next.getY());

				CostAndParent cap = nodes.get(next);
				if (cap != null) {
					if (cap.cost > ndcost) {
						cap.cost = ndcost;
						cap.parent = cur;
						queue.remove(new WeightedMapLocation(next, count));///?
						queue.add(new WeightedMapLocation(next, ndcost + nhcost));
					}
				} else {
					nodes.put(next, new CostAndParent(ndcost, cur));
					queue.add(new WeightedMapLocation(next, ndcost + nhcost));
				}
			}
		}

		DebugUtils.debug_print("empty queue");
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
