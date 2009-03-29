package batman.unit;

import batman.constants.StrategyConstants;
import batman.management.order.AttackMoveOrder;
import batman.management.order.Order;
import batman.management.order.SimpleMoveOrder;
import batman.messaging.Recipient;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.IMessage;
import batman.messaging.message.OrderMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.pathfinding.WalkResult;
import batman.unit.state.ArchonState;
import batman.unit.state.UnitState;
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

		if (!state.beFollower) {
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
					state.beFollower = true;
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

		if (!state.beFollower) {
			findFlux();
		}

		MapLocation targetLoc = refreshLocation();
		for (int loop = 0;; loop++) {
			if (!state.closeCombat) {
				if (state.beFollower) {
					followTheLeader();
				} else {
					targetLoc = seekAndDestroy(targetLoc);
					if (state.enemyIsACoward) {
						break;
					}
					rc.setIndicatorString(1, String.format("fad: %s -> %s", refreshLocation(), targetLoc));
				}
			}

			handleInts();
		}


		if (state.enemyIsACoward) {
			rc.setIndicatorString(2, "goRandFlux");
			goStupid(rand.nextInt(90));
		}

		//zbieraj
		for (int loop = 0;; loop++) {
			findFlux();
			goStupid(rand.nextInt(90));
			handleInts();
		}

	}

	protected void checkGameState() throws GameActionException
	{
		Direction fluxDir = rc.senseDirectionToUnownedFluxDeposit();
		if (Clock.getRoundNum() > 1200) {
			if (fluxDir == Direction.NONE) {
				if (state.enemyIsACoward == false) {
					state.enemyIsACoward = true;
					state.buildSoldiers = false;
					state.buildWorkers = true;
					findFlux();
				}

			} else {
				state.enemyIsACoward = false;
				state.buildSoldiers = true;
				state.buildWorkers = false;
			}
		}

	}

	protected MapLocation seekAndDestroy(MapLocation targetLoc) throws GameActionException
	{
		if (!state.beFollower) { //lead
			if (targetLoc.equals(refreshLocation())) {
				Direction fluxDir = rc.senseDirectionToUnownedFluxDeposit();
				if (fluxDir != Direction.NONE && fluxDir != Direction.OMNI) {
					targetLoc = refreshLocation().add(fluxDir);
				} else {
					targetLoc = MapUtils.randLocRange(curLoc, 25, 25, rand); //TODO_
				}
//				rc.setIndicatorString(2, String.format("leader: new loc: %s -> %s", refreshLocation(), targetLoc));
			} else {
				WalkResult wr = moveArmy(targetLoc);
				if (wr != WalkResult.Walking) {
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
		try {
			boolean found = false;
			for (Robot robot : rc.senseNearbyAirRobots()) {
				if (robot.getID() == archonIds[leaderIdx]) {

					targetLoc = rc.senseRobotInfo(robot).location;

					for (int i = 1; targetLoc.distanceSquaredTo(refreshLocation()) > 3 && i <= 4; i++) {
						moveArmy(targetLoc);
					}
					found = true;
					break;
				}
				handleInts();
			}

			if (found == false) { //?
				rc.setIndicatorString(1, "lost leader");
				//COPY PASTE
				if (targetLoc.equals(refreshLocation())) {
					Direction fluxDir = rc.senseDirectionToUnownedFluxDeposit();
					if (fluxDir != Direction.NONE && fluxDir != Direction.OMNI) {
						targetLoc = refreshLocation().add(fluxDir);
					} else {
						targetLoc = MapUtils.randLocRange(curLoc, 25, 25, rand); //TODO_
					}
					rc.setIndicatorString(2, String.format("leader: new loc: %s -> %s", refreshLocation(), targetLoc));
				} else {
					WalkResult wr = moveArmy(targetLoc);
					if (wr != WalkResult.Walking) {
						targetLoc = curLoc;
					}
				}

			}

		} catch (Exception e) {
			debug_print("ZLE");
			e.printStackTrace();

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
//		stupidWalk(groundLoc, CollisionPolicy.GoRound); //TODO_
//		ret = WalkResult.Walking;
		ret = stupidWalkStep(targetLoc); //TODO_
		if (ret == WalkResult.CannotReachLoc) {
			yieldSmallBC();
			for (Direction dir : MapUtils.movableDirections) {
				if (rc.canMove(dir)) {
					rc.setDirection(dir);
					rc.yield();
					if (rc.canMove(rc.getDirection())) {
						rc.moveForward();
						rc.yield();
						yieldMv();
						break;
					}
				}
			}
		}
		if (!state.beFollower) {
			waitForFollower();
			for (int k = 1; k <= 25; k++) {
				int count = 0;
				refreshLocation();
				for (RobotInfo ri : getAlliedGroundUnits()) {
					if (ri.location.distanceSquaredTo(curLoc) <= 3) {
						count++;
					}
				}
				if (count >= 5) {
					break;
				}
				order = new SimpleMoveOrder(refreshLocation());
				rc.broadcast(new OrderMessage(order).finalSerialize());
				rc.setIndicatorString(0, String.format("s sleep: %d %s %s --> %s", Clock.getRoundNum(), ret.toString(), refreshLocation(), targetLoc));
				sleep(1);
			}
		}

		return ret;
	}

	protected void buildSoldiersIfNeeded() throws GameActionException
	{
		if (!state.buildSoldiers) {
			return;
		}
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
			if (!state.enemyIsACoward) {
				waitForFollower();
			}

		}
	}

	protected void waitForFollower() throws GameActionException
	{
		for (Robot robot : rc.senseNearbyAirRobots()) {
			if (robot.getID() == archonIds[pairIdx]) {
				MapLocation archonLoc = rc.senseRobotInfo(robot).location;
				for (int j = 1; archonLoc.distanceSquaredTo(refreshLocation()) > 2 && j <= 6; j++) {
					sleep(1);
				}
				break;
			}
		}

	}

	private final void findFlux() throws GameActionException
	{
		rc.setIndicatorString(1, "find_flux");
		//ping();

		Direction dir;
		if (state.enemyIsACoward) {
			dir = rc.senseDirectionToOwnedFluxDeposit();
		} else {
			dir = rc.senseDirectionToUnownedFluxDeposit();
		}

		for (int i = 1; i <= 3 && dir != Direction.OMNI && dir != Direction.NONE;) {
			if (stupidWalkStep(refreshLocation().add(dir)) != WalkResult.Walking) {
				i++;
			}
			if (state.enemyIsACoward) {
				dir = rc.senseDirectionToOwnedFluxDeposit();
			} else {
				dir = rc.senseDirectionToUnownedFluxDeposit();
			}

		}

		if (dir == Direction.NONE) {
			goStupid(7);
			return;
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


			if (!state.enemyIsACoward) {
				captureFluxDeposidUnderArchon();
			} else {

				for (int i = 1;; i++) {
					rc.setIndicatorString(1, "extract");
					handleInts();
					rc.yield();
					if (rand.nextInt(50) == 0) {
						requestBlock(rand.nextInt(5) + 3);
					}
					if (i % 100 == 60) {
						buildWorker();
					}
				}

			}
		}
	}

	protected boolean buildUnit(RobotType type)
	{
		try {
			if (!hasEnergon(type.spawnCost() + 4.0)) {
//				debug_print("cannot spawn unit: not enough energon");
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
//				debug_print("cannot spawn unit");
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
		try {
			rc.setIndicatorString(1, Boolean.toString(state.enemyIsACoward));
			timeNow = Clock.getRoundNum();

			handleIntsDepth++;
			for (IMessage msg : getMessages()) {
				if (msg instanceof HungerMessage) { //TODO
					feed((HungerMessage) msg);
				} else if (msg instanceof RequestBlockMessage) {
					goStupid(40);
				}
			}

			checkAndHandleCombat();

			if (!state.closeCombat && !state.beFollower && !state.captureingFlux) { //zajmij flux, by nam nie bruzdzil :)
				for (FluxDeposit dep : rc.senseNearbyFluxDeposits()) {
					FluxDepositInfo fdi = rc.senseFluxDepositInfo(dep);
					if (fdi.team != myTeam) {
						state.captureingFlux = true;
						while (!refreshLocation().equals(fdi.location)) { //dojdz
							moveArmy(curLoc.add(curLoc.directionTo(fdi.location)));
							handleInts();
						}
						captureFluxDeposidUnderArchon();
						break;
					}
				}
				state.captureingFlux = false;
			}

			/*
			if (rand.nextInt(10) == 0) {
			updateMap();
			}
			 */

			if (state.buildSoldiers && timeNow % 20 <= (state.closeCombat ? 2 : 3)) {
				buildSoldiersIfNeeded();
			}

			if (timeNow % 4 == 1) {
				checkGameState();
				healSomeGroundUnits();
			}

			/*
			if (state.captureingFlux) {

			if (rand.nextInt(40) == 0) {
			requestBlock(rand.nextInt(5) + 3);
			}
			if (rand.nextInt(20) == 0) {
			buildWorker();
			}
			} */

			handleIntsDepth--;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void captureFluxDeposidUnderArchon() throws GameActionException
	{

		for (int i = 1; i <= GameConstants.ROUNDS_TO_CAPTURE + 20; i++) {
			FluxDeposit cfd = rc.senseFluxDepositAtLocation(refreshLocation());
			if (cfd == null) {
				break;
			} else {
				if (rc.senseFluxDepositInfo(cfd).roundsUntilCapture == 0) {
					break;
				}
			}
			sleep(1);
		}

		state.captureingFlux = false;
	}

	protected void checkAndHandleCombat() throws GameActionException
	{
//		debug_print("check and handle combat");
		yieldMediumBC();
		List<RobotInfo> enemies = getEnemies();
		if (!enemies.isEmpty()) {
			if (!state.closeCombat) {
				state.closeCombat = true;
				policy.minUnitEnergonLevel_Feed = 22;
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
			for (RobotInfo ri : enemies) {
				if (curLoc.distanceSquaredTo(ri.location) < curLoc.distanceSquaredTo(enemyLoc)) {
					enemyLoc = ri.location;
				}
			}

			if (rand.nextInt() % 6 == 0) {
				rc.broadcast(new OrderMessage(new AttackMoveOrder(enemyLoc)).finalSerialize());
				rc.yield(); //TODO to trwa 2 tury
			}
			if (refreshLocation().distanceSquaredTo(enemyLoc) <= 5) {
				if (health() < 0.38) {
					stupidWalkStep(curLoc.add(curLoc.directionTo(enemyLoc).opposite()));
				}
			} else {
				if (rand.nextInt() % 10 == 0) {
					stupidWalkStep(enemyLoc);
				}
			}
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
				if (state.enemyIsACoward && ri.type == RobotType.SOLDIER) {
					return;
				}
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

	@Override
	protected UnitState getState()
	{
		return state;
	}
}
