package batman.unit;

import batman.constants.StrategyConstants;
import batman.management.order.Order;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.ChangeRobotPolicyOrder;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.IMessage;
import batman.messaging.message.OrderMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.pathfinding.AStar;
import batman.pathfinding.Path;
import batman.strategy.RobotPolicy;
import batman.strategy.policy.HungerPolicy;
import batman.utils.MapUtils;
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


		if (rc.senseAlliedArchons().length > 1) {
			rc.suicide();
		}


		for (int i = 0; i < 34; i++) {
			goTo(MapUtils.add(refreshLocation(), 30, 30));
			handleInts();
		}

		for (int i = 0; i < 17; i++) {
			goTo(MapUtils.add(refreshLocation(), -10, -10));
			handleInts();
		}

		//goStupid(rand.nextInt(20)); //

		yieldMv();

		buildSoldier();

		for (;;) {
			handleInts();
			rc.yield();
			if (rand.nextInt(350) == 0) {
				AStar alg = new AStar();
				MapLocation to = MapUtils.randLocRange(curLoc, 30, 30, rand);
				Path path = alg.findPath(refreshLocation(), to, map, rc.getRobotType());

//				debug_print("%d %d ----> %d %d", curLoc.getX(), curLoc.getY(), to.getX(), to.getY());
				//map.debug_print();
				//map.debug_print(path);
			}

			if (rand.nextInt(40) == 0) {
				RobotPolicy rp = new RobotPolicy();
				rp.hungerPolicy=HungerPolicy.HungryAt35;
				Order order = new ChangeRobotPolicyOrder(rp);
				rc.broadcast(new OrderMessage(order).finalSerialize());

				rc.yield();
				order = new PathFindMoveOrder(MapUtils.add(refreshLocation(), 15, 15));
				rc.broadcast(new OrderMessage(order).finalSerialize());
			}
		}

//		findFlux(); //
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
		rc.broadcast(new RequestBlockMessage(howFar, refreshLocation()).finalSerialize());
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

	protected final void handleInts() throws GameActionException
	{
		for (IMessage msg : getMessages()) {
			if (msg instanceof HungerMessage) { //TODO
				feed((HungerMessage) msg);
			}
		}

		if (rand.nextInt(5) == 0) {
			updateMap();
		}

		if (rand.nextInt(5) == 0) {
		}

	}

	/** Odpowiada na prosbe o energon. */
	protected void feed(HungerMessage msg) throws GameActionException
	{
		MapLocation loc = msg.where;
		refreshLocation();
		if (loc.equals(curLoc) || loc.isAdjacentTo(curLoc)) {
			if (rc.senseGroundRobotAtLocation(loc) != null) { //TODO
				int howMuch = msg.howMuch;
				if (hasEnergon(howMuch + StrategyConstants.ARCHON_MIN_ENERGON_LEVEL)) {
					rc.transferEnergon(howMuch, loc, msg.rl);
					rc.yield();
				}
			}
		}

	}
}
