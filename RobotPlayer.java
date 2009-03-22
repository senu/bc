package batman;

import batman.unit.Archon;
import batman.unit.Soldier;
import batman.unit.Unit;
import batman.unit.Worker;
import batman.utils.Utils;
import batman.utils.WeightedMapLocation;
import battlecode.common.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class RobotPlayer implements Runnable
{
	private Random rand = new Random();
	private int round = 0;
	private final RobotController rc;
	private Goal goal;
	private MapLocation lastLoc;
	private MapLocation curLoc;
	private MapLocation goalLoc;
	private HashMap<MapLocation, Integer> map = new HashMap<MapLocation, Integer>();

	private RobotType robotType;


	/**
	 * A*
	 *
	 * Znajdz droge z from do to
	 *
	 * @return : from - gdy sie nie da, albo nastepne pole gdzie trzeba pojsc
	 */
	private MapLocation a_star_loc(MapLocation from, MapLocation to)
	{
		PriorityQueue<WeightedMapLocation> queue = new PriorityQueue<WeightedMapLocation>();
		queue.add(new WeightedMapLocation(from, 0));

		// -> dist, h
		HashMap<MapLocation, Integer> costs = new HashMap<MapLocation, Integer>(100);
		HashMap<MapLocation, MapLocation> parents = new HashMap<MapLocation, MapLocation>(100);
		Set<MapLocation> byl = new HashSet<MapLocation>(100);

		MapLocation cur;

		if (from.equals(to)) {
			return from;
		}

		int dcost;
		costs.put(from, new Integer(0));
		parents.put(from, from);

		int count = 0;

		while (!queue.isEmpty() && count < 400) {
			count++; //TODO
			WeightedMapLocation wcur = queue.remove();

			cur = wcur.x;
			dcost = costs.get(cur);

			//TODO

			/*if (byl.contains(cur) || (map.containsKey(cur) && map.get(cur).equals(LocStatus.LOC_BAD))) {
			continue;
			}*/
			byl.add(cur);

			if (cur.equals(to)) {
				MapLocation parent = parents.get(cur);
				while (!parent.equals(from)) {
					cur = parent;
					parent = parents.get(cur);
				//    debug_print("parent loop: %s -> %s -> %s [%s]", from, cur, to, parent);
				}

//				rc.setIndicatorString(0, from.toString());
//				rc.setIndicatorString(2, to.toString());
				return cur;
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

		debug_print("empty queue");
		return from;
	}

	public RobotPlayer(RobotController rc)
	{
		this.rc = rc;
		this.rand.setSeed(rc.getRobot().getID());
	}

	private void debug_print(String format, Object... args)
	{
		System.out.printf(rc.getRobot().getID() + " " + format + "\n", args);
	}

	private final void init()
	{
		lastLoc = rc.getLocation();
		robotType = rc.getRobotType();
		goal = Goal.GOAL_RAND;

		if (robotType == RobotType.WORKER) {
			goal = Goal.GOAL_NONE;
		}

//		debug_print("hello!, i'm %s", robotType);
	}

	public void run()
	{
		init();
		while (true) {
			try {

				Unit unit;
				if (robotType == RobotType.WORKER) {
					unit = new Worker(rc);
				} else if (robotType == RobotType.SOLDIER) {
					unit = new Soldier(rc);
				} else {
					unit = new Archon(rc);
				}

				unit.beYourself();

			} catch (Exception e) {
				debug_print("caught exception:");
				e.printStackTrace();
			}
		}
	}
}
