package batman.unit;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

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

	private final void beArchon() throws GameActionException
	{
		yieldMv();

		if (rand.nextBoolean()) {
			rc.setDirection(rc.getDirection().opposite());
			rc.yield();
		}

		goStupid(rand.nextInt(60)); //
		yieldMv();
		arch_find_flux(); //
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
			yield_mv();
			dir = rc.senseDirectionToUnownedFluxDeposit();
		}

		if (dir == Direction.NONE) {
			for (;;) {
				ping();
				return;
			}
		} else { //omni
			arch_buildWorker();

			for (int i = 1;; i++) {
				rc.setIndicatorString(1, "extract");
				arch_handleIU();
				rc.yield();
				if (rand.nextInt(50) == 0) {
					arch_requestBlock(rand.nextInt(5) + 3);
				}
				if (i % 100 == 0) {
					arch_buildWorker();
				}
			}
		}
	}

	private final void arch_buildWorker() throws GameActionException
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

	private final void arch_requestBlock(int howFar) throws GameActionException
	{
		rc.broadcast(Messages.newRequestBlockMessage(rc.getLocation(), howFar));
	}

	private final void arch_handleIU() throws GameActionException
	{
		Message[] msgs = rc.getAllMessages();
		for (Message msg : msgs) {
			if (msg.ints[0] == Messages.MSG_HUNGRY) {
				MapLocation loc = msg.locations[0];
				curLoc = rc.getLocation();
				if (loc.equals(curLoc) || loc.isAdjacentTo(curLoc)) {
					if (rc.senseGroundRobotAtLocation(loc) != null) { //TODO
						rc.transferEnergon(msg.ints[2], loc, RobotLevel.values()[msg.ints[1]]);
						rc.yield();
					}
				}
			}
		}
	}
}
