package batman.unit;

import batman.management.executor.WorkerExecutor;
import batman.messaging.Recipient;
import batman.messaging.message.IMessage;
import batman.messaging.message.OrderMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.strategy.policy.custom.WorkerPolicy;
import batman.unit.state.UnitState;
import batman.unit.state.WorkerState;
import batman.utils.MapUtils;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author senu
 */
public class Worker extends Unit
{
	private MapLocation blockGoal = null;
	protected WorkerExecutor executor = new WorkerExecutor(this);
	public WorkerPolicy workerPolicy = WorkerPolicy.DoNothing;
	public WorkerState state = new WorkerState();

	public Worker(RobotController rc)
	{
		super(rc);
	}

	@Override
	public void beYourself() throws GameActionException
	{
		blockGoal = null;
		for (;;) {
			handleInts();

		/*
		if (workerPolicy == WorkerPolicy.BeMedic) {
		beMedic();
		}*/
		}
	}

	protected void beMedic() throws GameActionException
	{
		yieldSmallBC();
//		debug_print("beMedic");
		rc.setIndicatorString(0, "beMedic");
		List<RobotInfo> allies = getAlliedGroundUnits();
		Collections.sort(allies, new Comparator<RobotInfo>()
		{
			MapLocation from = refreshLocation();

			public int compare(RobotInfo o1, RobotInfo o2)
			{
				return Integer.valueOf(from.distanceSquaredTo(o1.location)).compareTo(from.distanceSquaredTo(o2.location));
			}
		});
		for (RobotInfo ri : allies) {
			if (ri.type == RobotType.WORKER) {
				if ((ri.eventualEnergon / ri.maxEnergon) < 0.3) {
					heal(ri);
					return; //
				}
			} else if ((ri.eventualEnergon / ri.maxEnergon) < 0.5) {
				heal(ri);
				return; //
			}
		}
	}

	protected void heal(RobotInfo ri) throws GameActionException
	{
		rc.setIndicatorString(0, "heal");
		if (inTransferRange(ri.location) && rc.senseGroundRobotAtLocation(ri.location) != null) { //TODO
			rc.transferEnergon(rc.getEnergonLevel() / 3, ri.location, RobotLevel.ON_GROUND); //TODO
		} else {
			pathFindMove(ri.location);
		}
	}

	protected final void handleInts() throws GameActionException
	{
		handleIntsDepth++;
		if (handleIntsDepth >= 3) {
			throw new ArithmeticException();
		}

		rc.setIndicatorString(0, "handleInts");
		refreshLocation();

		if (!state.hungry_FindArchon && isHungry()) {
//			debug_print("isHungry");
			onHungry();
			handleIntsDepth--;
			return;
		} else {
			//orders
			for (IMessage msg : getMessages()) {
				if (msg instanceof RequestBlockMessage && blockGoal == null) {
					onRequestBlock((RequestBlockMessage) msg);
				} else if (msg instanceof OrderMessage) {
					rc.setIndicatorString(0, "execute - start");
					((OrderMessage) msg).order.execute(executor);
					rc.setIndicatorString(0, "execute - end");
				}
			}

		}

		handleIntsDepth--;

	}

	protected void onRequestBlock(RequestBlockMessage msg) throws GameActionException
	{

		if (workerPolicy == WorkerPolicy.BeMedic) {
			return;
		}

		blockGoal = msg.whereToUnload;
		int howFar = msg.howFar;
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
		goRandom(howFar);
		if (rc.senseNumBlocksInCargo(rc.getRobot()) != 0) {
//						debug_print("zle, mam blocka");
		}

		if (findBlock()) {
			returnBlock();
		}
		blockGoal = null;

	}

	private final void goRandom(int howFar) throws GameActionException
	{
		rc.setIndicatorString(1, "go_rand");
		MapLocation floc = MapUtils.randLocRange(rc.getLocation(), howFar, howFar, rand);
		for (int i = 1; i <= 30; i++) {
			stupidWalkStep(floc);
			handleInts();
			curLoc = rc.getLocation();
			if (floc.equals(curLoc)) {
				break;
			}
		}
	}

	private final boolean findBlock() throws GameActionException
	{
		rc.setIndicatorString(1, "find_block");
		curLoc = rc.getLocation();
		MapLocation[] blocks = rc.senseNearbyBlocks();
		List<MapLocation> goodBlocks = new ArrayList<MapLocation>(blocks.length / 2);

		for (MapLocation loc : blocks) {
			if (loc.distanceSquaredTo(blockGoal) > 6) {
				goodBlocks.add(loc);
			}
		}

		blocks = goodBlocks.toArray(new MapLocation[0]);

		if (blocks.length > 0) {
			MapLocation floc = MapUtils.closest(blocks, curLoc);
			for (int i = 1; i <= 20; i++) { //go

				if (rc.getLocation().distanceSquaredTo(floc) == 1) {
					break;
				}

				stupidWalkStep(floc);
				handleInts();
				curLoc = rc.getLocation();
				if (floc.equals(curLoc)) {
					break;
				}
			}
			yieldMediumBC();

			curLoc = rc.getLocation();
			if (rc.canLoadBlockFromLocation(floc) && rc.senseNumBlocksInCargo(rc.getRobot()) == 0) {
				yieldMv();
				rc.loadBlockFromLocation(floc);
				rc.yield();
				return true;
			} else {
				for (Direction dir : MapUtils.movableDirections) {
					yieldMv();
					rc.setDirection(dir);
					rc.yield();
					if (rc.canMove(dir)) {
						rc.moveForward();
						rc.yield();
						yieldMv();
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

	private final void returnBlock() throws GameActionException
	{
		final int dropDist = 4;
		rc.setIndicatorString(1, "return_block");
		for (int i = 0; i <= 40; i++) { //TODO handle ints
			curLoc = rc.getLocation();
//			if (curLoc.distanceSquaredTo(worker_block_goal) <= 1) {
			if (curLoc.isAdjacentTo(blockGoal)) {
				break;
			}
			stupidWalkStep(blockGoal);
		}

		yieldMv();

		MapLocation where = blockGoal;
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
			yieldMediumBC();

			while (!rc.canUnloadBlockToLocation(where)) {//
				curLoc = rc.getLocation();
				if (rc.canMove(rc.getDirection().opposite())) {
					rc.moveBackward();
					rc.yield();
					yieldMv();
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

	@Override
	protected boolean checkRecipient(Recipient recipient) throws GameActionException
	{
		return (recipient.toWhom.flag & Recipient.RecipientType.All.flag) == Recipient.RecipientType.All.flag; //TODO medics
	}

	@Override
	protected UnitState getState()
	{
		return state;
	}
}
