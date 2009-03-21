package batman;

import batman.utils.Messages;
import batman.unit.Archon;
import batman.unit.Unit;
import batman.utils.Utils;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class RobotPlayer implements Runnable
{
	private final int _smallBc = 50;
	private final int _mediumBc = 1000;
	private Random rand = new Random();
	private int round = 0;
	private final RobotController rc;
	private Goal goal;
	private MapLocation lastLoc;
	private MapLocation curLoc;
	private MapLocation goalLoc;
	private HashMap<MapLocation, Integer> map = new HashMap<MapLocation, Integer>();
	private MapLocation worker_block_goal = null;

	class LocStatus
	{
		private static final int LOC_BAD = 1;
		private static final int LOC_GOOD = 2;
		private static final int LOC_UNSEEN = 3;
	}
	private RobotType robotType;

	class Pair<T1, T2>
	{
		public Pair(T1 x, T2 y)
		{
			this.x = x;
			this.y = y;
		}
		public T1 x;
		public T2 y;
	}

	class WeightedMapLocation extends Pair<MapLocation, Integer> implements
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

			if (byl.contains(cur) || (map.containsKey(cur) && map.get(cur).equals(LocStatus.LOC_BAD))) {
				continue;
			}

			byl.add(cur);

			if (cur.equals(to)) {
				MapLocation parent = parents.get(cur);
				while (!parent.equals(from)) {
					cur = parent;
					parent = parents.get(cur);
				//    debug_print("parent loop: %s -> %s -> %s [%s]", from, cur, to, parent);
				}

				rc.setIndicatorString(0, from.toString());
				rc.setIndicatorString(2, to.toString());
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

	private void debug_printHashmap(HashMap<MapLocation, Integer> map)
	{
		//       Set <MapLocation> keys = map.keySet();
//        MapLocation min(0,0);
		//      for ( MapLocation loc : keys) {
		//    }
	}

	/** Yield jesli pozostalo mu mniej BC niz potrzebuje */
	private final void yieldIf(int need)
	{
		if (GameConstants.BYTECODES_PER_ROUND - Clock.getBytecodeNum() < need) {
			rc.yield();
		}
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

	/* Liczba z przedzialu 0.0 do 1.0*/
	private final double health()
	{
		return rc.getEnergonLevel() / rc.getMaxEnergonLevel();
	}

	public final void goTo(MapLocation nextLoc) throws GameActionException
	{
		if (curLoc.equals(nextLoc)) {
			return;
		}
		yield_mv();

		//nextLoc = a_star_loc(curLoc, nextLoc); //TODO

		Direction nextDir = curLoc.directionTo(nextLoc);

		if (rc.canMove(nextDir)) {
			yieldIf(_smallBc);
			rc.setDirection(nextDir);
			rc.yield();

			if (rc.canMove(rc.getDirection())) {
				rc.moveForward();
			} else {
				ping();
			}

			rc.yield();
			yield_mv();
		} else {
//			debug_print("bad loc");
			if (rc.canSenseSquare(nextLoc) && rc.senseGroundRobotAtLocation(nextLoc) == null) {
				map.put(nextLoc, LocStatus.LOC_BAD); //TODO to blokuje pole na zawsze, gdy stoi tam robot
			}
		}
	}

	private final void yield_mv()
	{
		while (rc.isMovementActive()) {
			rc.yield();
		}
	}

	private final void ping() throws GameActionException
	{
		for (int i = 1; i <= 10; i++) {
			rc.broadcast(Messages.newSimpleMessage(Messages.MSG_PING));
			rc.yield();
			goal = null;
		}
	}



	//woker --------------------------------------------------------------------
	private final MapLocation worker_nearestArchon()
	{
		return Utils.closest(rc.senseAlliedArchons(), rc.getLocation());
	}

	private final void worker_handleIU() throws GameActionException
	{
		curLoc = rc.getLocation();

		//hunger
		if (health() <= 0.35) {

			for (;;) {
				curLoc = rc.getLocation();

				yieldIf(_mediumBc);
				MapLocation loc = worker_nearestArchon();

				if (loc != null) {
					if (curLoc.isAdjacentTo(loc) || curLoc.equals(loc)) {
						rc.broadcast(Messages.hungryMessage(Messages.MSG_HUNGRY, rc));
						while (health() <= 0.35) {
							rc.yield();
						}
						return;
					} else {
//						debug_print("hungry");
						goTo(loc);
					}
				}
			}
		} else {
			//orders
			Message[] msgs = rc.getAllMessages();
			int howFar;
			for (Message msg : msgs) {
				if (msg.ints[0] == Messages.MSG_FIND_BLOCK && worker_block_goal == null) {
					worker_block_goal = msg.locations[0];
					howFar = msg.ints[1];
					//				debug_print("before go rand");

					//cp


					/*
					while (health() < 0.6) {
					curLoc = rc.getLocation();

					yieldIf(_mediumBc);
					MapLocation loc = worker_nearestArchon();

					if (loc != null) {
					if (curLoc.distanceSquaredTo(loc) <= 1) {
					rc.broadcast(Messages.hungryMessage(Messages.MSG_HUNGRY, rc));
					while (health() <= 0.6) {
					rc.yield();
					}
					return;
					} else {
					//						debug_print("hungry");
					goTo(loc);
					}
					}
					}
					 */

					///
					worker_go_rand(howFar);
					if (rc.senseNumBlocksInCargo(rc.getRobot()) != 0) {
//						debug_print("zle, mam blocka");
					}

					if (worker_find_block()) {
						worker_return_block();
					}
					worker_block_goal = null;
				}
			}
		}

	}

	private final void worker_go_rand(int howFar) throws GameActionException
	{
		rc.setIndicatorString(1, "go_rand");
		MapLocation floc = Utils.randLocRange(rc.getLocation(), howFar, howFar, rand);
		for (int i = 1; i <= 30; i++) {
			goTo(floc);
			worker_handleIU();
			curLoc = rc.getLocation();
			if (floc.equals(curLoc)) {
				break;
			}
		}
	}

	private final boolean worker_find_block() throws GameActionException
	{
		rc.setIndicatorString(1, "find_block");
		curLoc = rc.getLocation();
		MapLocation[] blocks = rc.senseNearbyBlocks();
		List<MapLocation> goodBlocks = new ArrayList<MapLocation>(blocks.length / 2);

		for (MapLocation loc : blocks) {
			if (loc.distanceSquaredTo(worker_block_goal) > 6) {
				goodBlocks.add(loc);
			}
		}

		blocks = goodBlocks.toArray(new MapLocation[0]);

		if (blocks.length > 0) {
			MapLocation floc = Utils.closest(blocks, curLoc);
			for (int i = 1; i <= 20; i++) { //go

				if (rc.getLocation().distanceSquaredTo(floc) == 1) {
					break;
				}

				goTo(floc);
				worker_handleIU();
				curLoc = rc.getLocation();
				if (floc.equals(curLoc)) {
					break;
				}
			}
			yieldIf(_mediumBc);

			curLoc = rc.getLocation();
			if (rc.canLoadBlockFromLocation(floc) && rc.senseNumBlocksInCargo(rc.getRobot()) == 0) {
				yield_mv();
				rc.loadBlockFromLocation(floc);
				rc.yield();
				return true;
			} else {
				for (Direction dir : Utils.movableDirections()) {
					yield_mv();
					rc.setDirection(dir);
					rc.yield();
					if (rc.canMove(dir)) {
						rc.moveForward();
						rc.yield();
						yield_mv();
						if (rc.canLoadBlockFromLocation(floc) && rc.senseNumBlocksInCargo(rc.getRobot()) == 0) {
							rc.loadBlockFromLocation(floc);
							rc.yield();
							return true;
						}
					}
				}
				return false;
			}
		} else {
			return false;
		}
	}

	private final void worker_return_block() throws GameActionException
	{
		final int dropDist = 4;
		rc.setIndicatorString(1, "return_block");
		for (int i = 0; i <= 40; i++) { //TODO handle ints
			curLoc = rc.getLocation();
//			if (curLoc.distanceSquaredTo(worker_block_goal) <= 1) {
			if (curLoc.isAdjacentTo(worker_block_goal)) {
				break;
			}
			goTo(worker_block_goal);
		}

		yield_mv();

		MapLocation where = worker_block_goal;
		/*
		if (curLoc.distanceSquaredTo(worker_block_goal) <= dropDist) {
		if (!rc.canUnloadBlockToLocation(where)) {
		for (Direction dir : Utils.movableDirections()) {
		where = rc.getLocation().add(dir);
		if (rc.canUnloadBlockToLocation(where)) {
		break;
		}
		}
		}
		}
		 */
		if (true) {
			yieldIf(_mediumBc);

			while (!rc.canUnloadBlockToLocation(where)) {//
				curLoc = rc.getLocation();
				if (rc.canMove(rc.getDirection().opposite())) {
					rc.moveBackward();
					rc.yield();
					yield_mv();
					where = curLoc;
				} else {
//					worker_go_rand(3);
					break;
				}
			}

			if (rc.canUnloadBlockToLocation(where)) {
				rc.unloadBlockToLocation(where);
				rc.yield();
			} else {
			}
		}
	}

	public final void beWorker() throws GameActionException
	{
		worker_block_goal = null;
		for (;;) {
			worker_handleIU();
		}

	}

	public void run()
	{
		init();
		while (true) {
			try {

				if (robotType == RobotType.WORKER) {
					beWorker();
				} else {
					Unit unit = new Archon(rc);
					unit.beYourself();
				}

			} catch (Exception e) {
				debug_print("caught exception:");
				e.printStackTrace();
			}
		}
	}
}
