package batman.unit;

import batman.constants.StrategyConstants;
import batman.management.order.Order;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.ChangeRobotPolicyOrder;
import batman.management.order.OrderGroup;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.IMessage;
import batman.messaging.message.MapTransferResponseMessage;
import batman.messaging.message.OrderMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.strategy.RobotPolicy;
import batman.strategy.policy.HungerPolicy;
import batman.utils.MapUtils;
import battlecode.common.Clock;
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
			goTo(MapUtils.add(refreshLocation(), 40, 40));
			handleInts();
		}

		for (int i = 0; i < 17; i++) {
			goTo(MapUtils.add(refreshLocation(), -20, -20));
			handleInts();
		}

		//	goStupid(rand.nextInt(60)); //

		yieldMv();
		test();

//		findFlux(); //
	}

	protected void test() throws GameActionException
	{
		buildSoldier();

		for (int loop=0;;loop++) {
			handleInts();
			rc.yield();
			if (loop % 300 == 90) {
				int ts = Clock.getRoundNum();
				MapTransferResponseMessage msg = new MapTransferResponseMessage(map.getTileSet());
//				debug_print("create maptransfer msg took: %d", Clock.getRoundNum()-ts);
				rc.broadcast(msg.finalSerialize());
//				debug_print("and serialize map took: %d", Clock.getRoundNum()-ts);
				rc.yield();


//				AStar alg = new AStar();
//				MapLocation to = MapUtils.randLocRange(curLoc, 30, 30, rand);
//				Path path = alg.findPath(refreshLocation(), to, map, rc.getRobotType());

			//				debug_print("%d %d ----> %d %d", curLoc.getX(), curLoc.getY(), to.getX(), to.getY());
			//map.debug_print();
			//map.debug_print(path);
			}
			if (loop % 120 == 100) {
				RobotPolicy rp = new RobotPolicy();
				rp.hungerPolicy = HungerPolicy.DieStarving;
				Order order1 = new ChangeRobotPolicyOrder(rp);
				Order order2 = new PathFindMoveOrder(MapUtils.add(refreshLocation(), 17, 20));
//			Order order3 = new PathFindMoveOrder(MapUtils.add(curLoc, 5, 5));

				OrderGroup group = new OrderGroup();
				group.orders.add(order1);
				group.orders.add(order2);
//			group.orders.add(order3);

				rc.broadcast(new OrderMessage(group).finalSerialize());
				rc.yield();
			}
		}

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


			//buildWorker();

//			rc.broadcast(new OrderMessage(new BeMedicOrder()).finalSerialize());

			buildSoldier();

//			for (;;) {
				/*
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
			//			if (rand.nextInt(120) == 0) {
			RobotPolicy rp = new RobotPolicy();
			rp.hungerPolicy = HungerPolicy.HungryAt35;
			Order order1 = new ChangeRobotPolicyOrder(rp);
			Order order2 = new PathFindMoveOrder(MapUtils.add(refreshLocation(), 5, 0));
			Order order3 = new PathFindMoveOrder(MapUtils.add(curLoc, 5, 5));

			OrderGroup group = new OrderGroup();
			group.orders.add(order1);
			group.orders.add(order2);
			group.orders.add(order3);

			rc.broadcast(new OrderMessage(group).finalSerialize());
			rc.yield();
			 */
//			}
//			}

//			buildWorker();


			for (int i = 1;; i++) {
				rc.setIndicatorString(1, "extract");
				handleInts();
				rc.yield();
				if (rand.nextInt(50) == 0) {
					requestBlock(rand.nextInt(5) + 3);
				}
				if (i % 100 == 20) {
					buildWorker();
				}
				if (i % 70 == 0 && i > 600) {
					buildSoldier();
					rc.broadcast(new OrderMessage(new PathFindMoveOrder(MapUtils.randLocRange(refreshLocation(), 2, 2, rand))).finalSerialize());
				}
			}
		}
	}

	protected void buildUnit(RobotType type)
	{
		try {
			while (!hasEnergon(type.spawnCost())) {
				handleInts();
			}

			int i = 0;
			while (rc.senseGroundRobotAtLocation(frontLoc()) != null || rc.senseTerrainTile(frontLoc()).getType() != TerrainTile.TerrainType.LAND) {
				rc.setDirection(rc.getDirection().rotateLeft());
				rc.yield();
				i++;
				if (i > 7) {
					break;
				}
			}

			if (rc.senseGroundRobotAtLocation(frontLoc()) == null && rc.senseTerrainTile(frontLoc()).getType() == TerrainTile.TerrainType.LAND) {
				rc.spawn(type);
				rc.yield();
			}

		} catch (Exception e) {
			debug_print("spawn unit, exn" + type.toString());
			e.printStackTrace();
		}
	}

	private void buildSoldier() throws GameActionException
	{
		buildUnit(RobotType.SOLDIER);
	}

	private final void buildWorker() throws GameActionException
	{
		buildUnit(RobotType.WORKER);
	}

	private final void requestBlock(int howFar) throws GameActionException
	{
		rc.broadcast(new RequestBlockMessage(howFar, refreshLocation()).finalSerialize());
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
			yieldMediumBC();
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
