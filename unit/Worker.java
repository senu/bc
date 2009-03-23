package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.constants.StrategyConstants;
import batman.messaging.Messages;
import batman.messaging.message.IMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.utils.Utils;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author senu
 */
public class Worker extends Unit
{
	private MapLocation blockGoal = null;

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
		}
	}

	private final void handleInts() throws GameActionException
	{
		refreshLocation();

		//hunger
		if (health() <= StrategyConstants.WORKER_HUNGER_LEVEL) {

			for (;;) {
				curLoc = rc.getLocation();

				yieldIf(ByteCodeConstants.Medium);
				MapLocation loc = nearestArchon();

				if (loc != null) {
					if (inTransferRange(loc)) {
						rc.broadcast(Messages.hungryMessage(rc));
						while (health() <= StrategyConstants.WORKER_HUNGER_LEVEL) {
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
			int howFar;
			for (IMessage msg : getMessages()) {
				if (msg instanceof RequestBlockMessage && blockGoal == null) {
					RequestBlockMessage rmsg = (RequestBlockMessage) msg;
					blockGoal = rmsg.whereToUnload;
					howFar = rmsg.howFar;
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
			}
		}

	}

	private final void goRandom(int howFar) throws GameActionException
	{
		rc.setIndicatorString(1, "go_rand");
		MapLocation floc = Utils.randLocRange(rc.getLocation(), howFar, howFar, rand);
		for (int i = 1; i <= 30; i++) {
			goTo(floc);
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
			MapLocation floc = Utils.closest(blocks, curLoc);
			for (int i = 1; i <= 20; i++) { //go

				if (rc.getLocation().distanceSquaredTo(floc) == 1) {
					break;
				}

				goTo(floc);
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
				for (Direction dir : Utils.movableDirections()) {
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
			goTo(blockGoal);
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
}
