package batman.unit;

import batman.constants.StrategyConstants;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import batman.messaging.Messages;
import battlecode.common.GameConstants;
import battlecode.common.TerrainTile;

/**
 *
 * @author senu
 */
public class Archon extends Unit
{
	public Archon(RobotController rc)
	{
		super(rc);
	}

	public final void beYourself() throws GameActionException
	{
		yieldMv();

		if (rand.nextBoolean()) {
			rc.setDirection(rc.getDirection().opposite());
			rc.yield();
		}

		goStupid(rand.nextInt(60)); //
		yieldMv();


		/*buildSoldier();
		
		for (;;) {
		handleInts();
		rc.yield();
		if (rand.nextInt(350) == 0) {
		map.debug_print();
		}} */

		findFlux(); //
	}

	private final void goStupid(int howLong) throws GameActionException
	{
		rc.setIndicatorString(1, "go_stupid");
		for (int i = 1; i <= howLong; i++) {

			yieldMv();

			if (rc.canMove(rc.getDirection())) {
				rc.moveForward();
			} else {
				rc.setDirection(rc.getDirection().rotateLeft());
			}

			rc.yield();

			if (rand.nextInt(5) == 0) {
				updateMap();
			}

		}
	}

	private final void findFlux() throws GameActionException
	{
		rc.setIndicatorString(1, "find_flux");
		//ping();

		Direction dir = rc.senseDirectionToUnownedFluxDeposit();

		while (dir != Direction.OMNI && dir != Direction.NONE) {
			rc.setDirection(dir);
			rc.yield();
			if (rc.canMove(rc.getDirection())) {
				rc.moveForward();
			} else {
//				ping();
				//rc.setDirection(rc.getDirection().rotateLeft());
				goStupid(20);
			}
			rc.yield();
			yieldMv();
			dir = rc.senseDirectionToUnownedFluxDeposit();
		}

		if (dir == Direction.NONE) {
			for (;;) {
				ping();
				return;
			}
		} else { //omni
			buildWorker();

			for (int i = 1;; i++) {
				rc.setIndicatorString(1, "extract");
				handleInts();
				rc.yield();
				if (rand.nextInt(50) == 0) {
					requestBlock(rand.nextInt(5) + 3);
				}
				if (i % 100 == 0) {
					buildWorker();
				}
			}
		}
	}

	private final void buildWorker() throws GameActionException
	{
		try {
			while (rc.senseGroundRobotAtLocation(rc.getLocation().add(rc.getDirection())) != null) {
				rc.setDirection(rc.getDirection().rotateRight());
				rc.yield();
			}
			if (rc.senseGroundRobotAtLocation(rc.getLocation().add(rc.getDirection())) == null) {
				rc.spawn(RobotType.WORKER);
				rc.yield();
			}
		} catch (Exception e) {
			debug_print("spawn worker, exn");
			e.printStackTrace();
		}
	}

	private final void requestBlock(int howFar) throws GameActionException
	{
		rc.broadcast(Messages.newRequestBlockMessage(rc.getLocation(), howFar));
	}

	private void buildSoldier() throws GameActionException
	{
		while (!hasEnergon(RobotType.SOLDIER.spawnCost())) {
			handleInts();
		}

		if (rc.senseGroundRobotAtLocation(frontLoc()) == null && rc.senseTerrainTile(frontLoc()).getType() == TerrainTile.TerrainType.LAND) {
			rc.spawn(RobotType.SOLDIER);
			rc.yield();
		}
	}

	private final void handleInts() throws GameActionException
	{
		Message[] msgs = rc.getAllMessages();
		for (Message msg : msgs) {
			if (msg.ints[0] == Messages.MSG_HUNGRY) {
				feed(msg);
			}
		}

		if (rand.nextInt(5) == 0) {
			updateMap();
		}
		if (rand.nextInt(150) == 0) {
			map.debug_print();
		}

	}

	/** Odpowiada na prosbe o energon. */
	protected void feed(Message msg) throws GameActionException
	{
		MapLocation loc = msg.locations[0];
		refreshLocation();
		if (loc.equals(curLoc) || loc.isAdjacentTo(curLoc)) {
			if (rc.senseGroundRobotAtLocation(loc) != null) { //TODO
				int howMuch = msg.ints[2];
				if (hasEnergon(howMuch + StrategyConstants.ARCHON_MIN_ENERGON_LEVEL)) {
					rc.transferEnergon(howMuch, loc, RobotLevel.values()[msg.ints[1]]);
					rc.yield();
				}
			}
		}

	}
}
