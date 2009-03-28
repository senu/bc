package batman.unit;

import batman.constants.StrategyConstants;
import batman.management.order.AttackMoveOrder;
import batman.management.order.Order;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SimpleMoveOrder;
import batman.messaging.Recipient;
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
import batman.pathfinding.WalkResult;
import batman.unit.state.ArchonState;
import batman.utils.MapUtils;
import battlecode.common.Clock;
import battlecode.common.FluxDeposit;
import battlecode.common.FluxDepositInfo;
import battlecode.common.GameConstants;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author senu
 */
public class Archon extends Unit
{
	/** Wspolpraca archonow. */
	int[] archonIds = new int[6];
	int myIdx, pairIdx, leaderIdx;
	ArchonState state = new ArchonState();
	ArrayList<Integer> mySoldiers = new ArrayList<Integer>(6);

	public Archon(RobotController rc)
	{
		super(rc);
		policy.minUnitEnergonLevel_Feed = StrategyConstants.ARCHON_MIN_ENERGON_LEVEL;
		policy.healIfWeakerThan = 0.6;
		state.closeCombat = false;
		state.buildSoldiers = false;
	}

	public final void beYourself() throws GameActionException
	{
		yieldMv();

		groupArchons();

		if (myIdx != 0 && myIdx != 1) {
//			rc.suicide();
		} else {
			debug_print("leader: %d", leaderIdx);
		}
		if (rc.senseAlliedArchons().length > 1) {
//			rc.suicide();
		}

//		MapLocation startLoc = MapUtils.add(refreshLocation(), 7 + leaderIdx *3/2, leaderIdx * 3/2 + 5);
		rc.setIndicatorString(0, "go Start");

		if (!state.followLeader) {
			for (Direction dir : MapUtils.movableDirections) {
				if (rc.canMove(dir)) {
					rc.setDirection(dir);
					rc.yield();
					break;
				}
			}
			goStupid(rand.nextInt(12) + 13);

		} else {
			MapLocation lastLoc = refreshLocation();
			for (int i = 1, j = 1; i < 260 && j < 16; i++) {
				if (lastLoc.equals(refreshLocation())) {
					j++;
					sleep(1);
				} else {
					j = 1;
					lastLoc = curLoc;
				}
				followTheLeader();
			}
		}

		rc.setIndicatorString(0, "at Start");
		rc.setIndicatorString(1, "at Start");

		//	goStupid(rand.nextInt(60)); //

		yieldMv();
		test();

//		findFlux(); //
	}

	/** Grupuje archony w pary. */
	protected void groupArchons() throws GameActionException
	{
		MapLocation[] locs = rc.senseAlliedArchons();
		for (int i = 0; i < locs.length; i++) {
			MapLocation loc = locs[i];
			Robot archon = rc.senseAirRobotAtLocation(loc);
			archonIds[i] = archon.getID();
		}

		Arrays.sort(archonIds);
		for (int i = 0; i < archonIds.length; i++) {
			if (archonIds[i] == rc.getRobot().getID()) {
				myIdx = i;
				if (i % 2 == 0) {
					pairIdx = i + 1;
					leaderIdx = myIdx;
					rc.setIndicatorString(2, "leader");
				} else {
					pairIdx = i - 1;
					leaderIdx = pairIdx;
					state.followLeader = true;
					rc.setIndicatorString(2, "follower");
				}
				break;
			}
		}

		debug_print("%d %d %d", myIdx, pairIdx, leaderIdx);
	}

	protected void test() throws GameActionException
	{

		state.buildSoldiers = true;

		/*
		//for (int loop = 0;; loop++) {
			handleInts();
			rc.yield();
			if (false) {
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

			if (Clock.getRoundNum() >= 100) {
				break;
			}
		}
		 *
		 */


		MapLocation targetLoc = refreshLocation();
		for (int loop = 0;; loop++) {
			if (!state.closeCombat) {
				targetLoc = findAndDestroy(targetLoc);
			}

			handleInts();
		}

	}

	protected MapLocation findAndDestroy(MapLocation targetLoc) throws GameActionException
	{
		if (!state.followLeader) { //lead
			if (targetLoc.equals(refreshLocation())) {
				Direction fluxDir = rc.senseDirectionToOwnedFluxDeposit();
				if (fluxDir != Direction.NONE && fluxDir != Direction.OMNI) {
					targetLoc = refreshLocation().add(fluxDir);
				} else {
					targetLoc = MapUtils.randLocRange(curLoc, 25, 25, rand);
				}
			} else {
				if (moveArmy(targetLoc) != WalkResult.Walking) {
					targetLoc = curLoc;
				}
			}

		} else { //follow
			targetLoc = followTheLeader(); //with army
		}

		return targetLoc;

	}

	protected MapLocation followTheLeader() throws GameActionException
	{
		MapLocation targetLoc = curLoc;

		yieldMediumBC();
		for (Robot robot : rc.senseNearbyAirRobots()) {
			if (robot.getID() == archonIds[leaderIdx]) {

				targetLoc = rc.senseRobotInfo(robot).location;

				for (int i = 1; targetLoc.distanceSquaredTo(refreshLocation()) > 3 && i <= 4; i++) {
					moveArmy(targetLoc);
				}
				break;
			}
			handleInts();
		}

		return targetLoc;
	}

	protected WalkResult moveArmy(MapLocation targetLoc) throws GameActionException
	{
		MapLocation groundLoc = refreshLocation().add(curLoc.directionTo(targetLoc));
		WalkResult ret;

		for (int i = 1; rc.senseTerrainTile(groundLoc).getType() != TerrainType.LAND && i <= 3; i++) {
			groundLoc = groundLoc.add(curLoc.directionTo(targetLoc));
		}

		Order order = new SimpleMoveOrder(groundLoc);
		ret = stupidWalkStep(targetLoc);
		if (!state.followLeader) {
			rc.broadcast(new OrderMessage(order).finalSerialize());
			sleep(6);
		}

		return ret;
	}

	protected void buildSoldiersIfNeeded() throws GameActionException
	{
		int count = 0;
		ArrayList<Integer> newSoldiers = new ArrayList<Integer>(6);
		for (Robot r : rc.senseNearbyGroundRobots()) {
			if (mySoldiers.contains(r.getID())) { //TODO clean list
				count++;
			} else {
				newSoldiers.add(r.getID());
			}
		}

		if (Clock.getRoundNum() % 10 == 0) { //clean
			mySoldiers = newSoldiers;
		}

		if (count == 0) {
			mySoldiers.clear();
		//		debug_print("mySoldiers clear");
		}
		if (count < 4) {
			buildSoldier();
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

			//wait for second archon
			for (Robot robot : rc.senseNearbyAirRobots()) {
				if (robot.getID() == archonIds[pairIdx]) {
					MapLocation archonLoc = rc.senseRobotInfo(robot).location;
					for (int j = 1; archonLoc.distanceSquaredTo(refreshLocation()) > 2 && j <= 4; j++) {
						sleep(1);
					}
					break;
				}
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
					rc.broadcast(new OrderMessage(new PathFindMoveOrder(MapUtils.randLocRange(refreshLocation(), 5, 5, rand))).finalSerialize());
				}
				if (i % 70 == 0 && i > 600) {
					buildSoldier();
					buildSoldier();
				}
			}
		}
	}

	protected boolean buildUnit(RobotType type)
	{
		try {
			if (!hasEnergon(type.spawnCost())) {
				debug_print("cannot spawn unit: not enough energon");
				return false;
			}
//			while (!hasEnergon(type.spawnCost())) {
//				handleInts();
//			}

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
				return true;
			} else {
				debug_print("cannot spawn unit");
			}

		} catch (Exception e) {
			debug_print("spawn unit, exn" + type.toString());
			e.printStackTrace();
		}
		return false;
	}

	private void buildSoldier() throws GameActionException
	{
		yieldMv();
		if (buildUnit(RobotType.SOLDIER)) {
//			debug_print("built soldier");
			Robot robot = rc.senseGroundRobotAtLocation(frontLoc());
			if (robot != null) {
				mySoldiers.add(robot.getID());
			//debug_print("%s", mySoldiers.toString());
			} else {
				debug_print("something went wrong");
			}
		}
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
//		rc.setIndicatorString(0, Boolean.toString(state.closeCombat));
		timeNow = Clock.getRoundNum();

		handleIntsDepth++;
		if (handleIntsDepth > 3) {
			handleIntsDepth--;
			throw new ArithmeticException();
		}
		for (IMessage msg : getMessages()) {
			if (msg instanceof HungerMessage) { //TODO
				feed((HungerMessage) msg);
			}
		}

		checkAndHandleCombat();

		if (!state.closeCombat && !state.followLeader && !state.captureingFlux) { //zajmij flux, by nam nie bruzdzil :)
			for (FluxDeposit dep : rc.senseNearbyFluxDeposits()) {
				FluxDepositInfo fdi = rc.senseFluxDepositInfo(dep);
				if (fdi.team != myTeam) {
					state.captureingFlux = true;
					while (!refreshLocation().equals(fdi.location)) { //dojdz
						moveArmy(curLoc.add(curLoc.directionTo(fdi.location)));
						handleInts();
					}
					for (int i = 1; i <= GameConstants.ROUNDS_TO_CAPTURE + 20; i++) {
						FluxDeposit cfd = rc.senseFluxDepositAtLocation(refreshLocation());
						if (cfd == null) {
							break;
						} else {
							if (rc.senseFluxDepositInfo(cfd).roundsUntilCapture == 0) {
								break;
							}
						}
						rc.yield();
					}
					break;
				}
			}
			state.captureingFlux = false;
		}

		if (rand.nextInt(10) == 0) {
			updateMap();
		}

		if (state.buildSoldiers && timeNow % 16 <= (state.closeCombat ? 0 : 2)) {
			buildSoldiersIfNeeded();
		}

		if (timeNow % 4 == 1) {
			healSomeGroundUnits();
		}

		handleIntsDepth--;
	}

	protected void checkAndHandleCombat() throws GameActionException
	{
//		debug_print("check and handle combat");
		yieldMediumBC();
		List<RobotInfo> enemies = getEnemies();
		if (!enemies.isEmpty()) {
			if (!state.closeCombat) {
				state.closeCombat = true;
				policy.minUnitEnergonLevel_Feed = 50;
				state.buildSoldiers = true;

			/*
			if (getAlliedGroundUnits().size() < 4) { //dobuduj wojo
			for (int i = 1; i <= 60 && getAlliedGroundUnits().size() < 4; i++) {
			buildSoldier();
			sleep(3);
			}
			return;
			}
			 */
			}

			MapLocation enemyLoc = enemies.get(0).location;
			rc.broadcast(new OrderMessage(new AttackMoveOrder(enemyLoc)).finalSerialize());
			rc.yield(); //TODO to trwa 2 tury
			stupidWalkStep(enemyLoc);
		} else {
			state.closeCombat = false;
			policy.minUnitEnergonLevel_Feed = StrategyConstants.ARCHON_MIN_ENERGON_LEVEL;
		}
	}

	/** Odpowiada na prosbe o energon. */
	protected void feed(HungerMessage msg) throws GameActionException
	{
		MapLocation loc = msg.where;
		refreshLocation();

		if (loc.equals(curLoc) || loc.isAdjacentTo(curLoc)) {
			yieldMediumBC();
			Robot robot = rc.senseGroundRobotAtLocation(loc);
			if (robot != null) { //TODO
				RobotInfo ri = rc.senseRobotInfo(robot);
				double howMuch = Math.min(ri.maxEnergon - ri.eventualEnergon, GameConstants.ENERGON_RESERVE_SIZE);
				feed(loc, msg.rl, howMuch);
			}

		}

	}

	@Override
	protected boolean checkRecipient(Recipient recipient) throws GameActionException
	{
		return (recipient.toWhom.flag & Recipient.RecipientType.Archons.flag) == Recipient.RecipientType.Archons.flag; //TODO medics
	}
}
