package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.constants.StrategyConstants;
import batman.management.result.ExecutionResult;
import batman.messaging.Recipient;
import batman.messaging.message.IMessage;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.MapTransferRequestMessage;
import batman.messaging.message.MapTransferResponseMessage;
import batman.messaging.message.MessageImpl;
import batman.messaging.message.OrderMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.pathfinding.AStar;
import batman.pathfinding.FastAStar;
import batman.utils.MapUtils;
import batman.pathfinding.GameMap;
import batman.pathfinding.MapTile;
import batman.pathfinding.Path;
import batman.pathfinding.WalkResult;
import batman.strategy.RobotPolicy;
import batman.strategy.policy.CollisionPolicy;
import batman.strategy.policy.MapRefreshPolicy;
import batman.unit.state.UnitState;
import batman.utils.SimpleRobotInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author senu
 */
public abstract class Unit
{
	protected RobotController rc;
	protected Random rand = new Random();
	protected MapLocation curLoc;
	protected GameMap map = new GameMap();
	protected Team myTeam;
	public RobotPolicy policy;
	protected Map<Integer, Class> messageTypes = new HashMap<Integer, Class>();
	protected int handleIntsDepth = 0;
	protected int timeNow; //niekoniecznie aktualny numer rundy (mapa)

	//TODO remove
	class LocStatus
	{
		private static final int LOC_BAD = 1;
		private static final int LOC_GOOD = 2;
		private static final int LOC_UNSEEN = 3;
	}

	public Unit(RobotController rc)
	{
		this.rc = rc;
		this.rand.setSeed(rc.getRobot().getID());
		this.myTeam = rc.getTeam();
		this.policy = new RobotPolicy(rand);

		//TODO tak sie tego nie robi
		Class[] messageClasses = new Class[]{
			HungerMessage.class,
			MapTransferRequestMessage.class,
			MapTransferResponseMessage.class,
			OrderMessage.class,
			RequestBlockMessage.class,};

		try {
			for (Class clazz : messageClasses) {
				messageTypes.put(((MessageImpl) clazz.newInstance()).getMessageType(), clazz);
			}


		} catch (Exception e) {
			debug_print("serialization errror");
			e.printStackTrace();
		}


	}

	protected abstract UnitState getState();

	public abstract void beYourself() throws GameActionException;

	protected abstract void handleInts() throws GameActionException;

	/** True gdy wiadomosc nas dotyczy */
	protected abstract boolean checkRecipient(Recipient recipient) throws GameActionException;

	protected final MapLocation refreshLocation()
	{
		curLoc = rc.getLocation();
		return curLoc;
	}

	protected final void debug_print(String format, Object... args)
	{
		System.out.printf(rc.getRobot().getID() + " [" + Integer.toString(handleIntsDepth) + "]" + format + "\n", args);
	}

	protected final void ping() throws GameActionException
	{
//		for (int i = 1; i <= 10; i++) {
//TODO			rc.broadcast(Messages.newSimpleMessage(Messages.MSG_PING));
		rc.yield();
//		}
	}

	protected boolean hasEnergon(double howMuch)
	{
		return rc.getEnergonLevel() > howMuch;
	}

	/** Zwraca rowniez zle lokacje (poza mapa). */
	public List<MapLocation> getLocsInSensorRange(MapRefreshPolicy mapPolicy) throws GameActionException
	{
		List<MapLocation> retLocs = new ArrayList<MapLocation>(30);
		int range = rc.getRobotType().sensorRadius();
		refreshLocation();
		MapLocation mxy = new MapLocation(curLoc.getX() - range, curLoc.getY() - range); //minimum

		int currentRound = Clock.getRoundNum(), mapTileRound;
		MapTile tile;
		//TODO angle
		for (MapLocation mx = mxy; mx.getY() - mxy.getY() <= 2 * range; mx = mx.add(Direction.SOUTH)) {
			for (MapLocation loc = mx; loc.getX() - mxy.getX() <= 2 * range; loc = loc.add(Direction.EAST)) {
				tile = map.getTile(loc);
				if (tile != null) {
					mapTileRound = tile.roundSeen;
				} else {
					mapTileRound = 0;
				}
				if (currentRound - mapTileRound > mapPolicy.refreshWhenOlderThan) {  //TODO unknown
//					debug_print("do scanLoc: %d, %d", currentRound, mapTileRound);
					if (rc.canSenseSquare(loc)) {
						retLocs.add(loc);
					}
				} else {
//					debug_print("skipped scanLoc: %s", loc.toString());
				}
			}
		}
		return retLocs;
	}

	/* Liczba z przedzialu 0.0 do 1.0*/
	protected final double health()
	{
		return rc.getEnergonLevel() / rc.getMaxEnergonLevel();
	}

	protected final MapLocation nearestArchon()
	{
		return MapUtils.closest(rc.senseAlliedArchons(), rc.getLocation());
	}

	protected final boolean inTransferRange(MapLocation loc)
	{
		return refreshLocation().isAdjacentTo(loc) || curLoc.equals(loc);
	}

	/** Czekaj, az ruch sie nie skonczy. */
	protected final void yieldMv()
	{
		/*while (rc.isMovementActive()) {
		rc.yield();
		}*/
		while (rc.isAttackActive() || rc.isMovementActive()) {
			rc.yield();
		}
	}

	/** Czekaj, az ruch i atak sie nie skonczy. */
	protected final void yieldAtt()
	{
		while (rc.isAttackActive() || rc.isMovementActive()) {
			rc.yield();
		}
	}

	/** Yield jesli pozostalo mu mniej BC niz potrzebuje */
	protected final void yieldIf(int need)
	{
		if (GameConstants.BYTECODES_PER_ROUND - Clock.getBytecodeNum() < need) {
			rc.yield();
		}
	}

	protected final void yieldMediumBC()
	{
		yieldIf(ByteCodeConstants.Medium);
	}

	protected final void yieldSmallBC()
	{
		yieldIf(ByteCodeConstants.Small);
	}

	protected final void yieldHalfBC()
	{
		yieldIf(ByteCodeConstants.Half);
	}

	/** Jezeli targetLoc == null, to szuka archona */
	public ExecutionResult stupidWalkGoTo(MapLocation targetLoc,
			CollisionPolicy colPolicy)
			throws GameActionException
	{
		boolean searchArchon = (targetLoc == null);
		//debug_print("stupidWalkGo %s", searchArchon ? "NULL" : targetLoc.toString());

		if (searchArchon) {
			targetLoc = nearestArchon();
		}

		yieldSmallBC();
		if (refreshLocation().equals(targetLoc)) {
			return ExecutionResult.OK;
		}

		Direction curDirection, nextDirection;
		nextDirection = curLoc.directionTo(targetLoc);
		curDirection = nextDirection;

		for (int i = 1; i < StrategyConstants.STUPID_GO_STEPS; i += 3) {
			if (searchArchon) {
				targetLoc = nearestArchon();
			}
			if (refreshLocation().equals(targetLoc)) {
				return ExecutionResult.OK;
			}
			nextDirection = curLoc.directionTo(targetLoc);


			if (nextDirection == curDirection.opposite()) { //nie chcemy sie cofac
				if (i % 2 == 0) {
					nextDirection = stupidTurnLeftOrRight(curDirection);
				} else {
					nextDirection = curDirection;
				}
			}

			if (!rc.canMove(nextDirection)) { //przeszkoda - omijamy ja dalej
				if (colPolicy == CollisionPolicy.GoRound) { //omijamy
					nextDirection = curDirection;
				} else { //czekamy //TODO a little
					MapLocation nextLoc = curLoc.add(nextDirection);
					if (rc.canSenseSquare(nextLoc)) {
						Robot obstacle = rc.senseGroundRobotAtLocation(nextLoc);
						if (obstacle != null && rc.senseRobotInfo(obstacle).team == myTeam) {//TODO air, TODO height
							sleep(1);
							i -= 2;
							continue;
						}
					}
					//jednak omijamy
					nextDirection = curDirection;
				}
			}

			for (int j = 1; !rc.canMove(nextDirection) && j <= 4; j++) { // kolejna przeszkoda - omijamy idac lewej
				//COPY PASTE
				if (colPolicy == CollisionPolicy.GoRound) { //omijamy
					nextDirection = stupidTurnLeftOrRight(nextDirection);
				} else { //czekamy //TODO a little
					MapLocation nextLoc = curLoc.add(nextDirection);
					if (rc.canSenseSquare(nextLoc)) {
						Robot obstacle = rc.senseGroundRobotAtLocation(nextLoc);
						if (obstacle != null && rc.senseRobotInfo(obstacle).team == myTeam) {//TODO air, TODO height
							sleep(1);
							i -= 2;
							continue;
						}
					}
					//jednak omijamy
					nextDirection = stupidTurnLeftOrRight(nextDirection);
				}
			}

			curDirection = nextDirection;

			stupidWalkStep(refreshLocation().add(nextDirection));
			handleInts();
		}

		return ExecutionResult.Failed;

	}

	protected Direction stupidTurnLeftOrRight(Direction nextDirection)
	{
		if (policy.stupidWalkTurnLeft) {
			return nextDirection.rotateLeft();
		} else {
			return nextDirection.rotateRight();
		}
	}

	//TODO remove it
	protected final WalkResult stupidWalkStep(MapLocation nextLoc) throws GameActionException
	{
		if (refreshLocation().equals(nextLoc)) {
			return WalkResult.Finished;
		}
		yieldAtt();

		//nextLoc = a_star_loc(curLoc, nextLoc); //TODO

		Direction nextDir = curLoc.directionTo(nextLoc);

		if (rc.canMove(nextDir)) {
			yieldSmallBC();
			rc.setDirection(nextDir);
			rc.yield();
			yieldAtt();

			if (rc.canMove(rc.getDirection())) {
				rc.moveForward();
			} else {
				return WalkResult.CannotReachLoc;
			}

			rc.yield();
			yieldAtt();
		} else {
			if (rc.canSenseSquare(nextLoc) && rc.senseGroundRobotAtLocation(nextLoc) == null) {
				map.setTile(nextLoc, new MapTile(MapTile.LocState.Bad)); //TODO to blokuje pole na zawsze, gdy stoi tam robot
			}
			return WalkResult.CannotReachLoc;
		}
		return WalkResult.Walking;
	}

	protected void updateMap() throws GameActionException
	{
		int rstart = Clock.getRoundNum();
		timeNow = rstart;
		List<MapLocation> locs = getLocsInSensorRange(policy.mapRefreshPolicy);
		for (MapLocation loc : locs) {
			map.setTile(loc, scanLoc(loc));
		}

//		debug_print("updateMap took: %d", Clock.getRoundNum() - rstart);
	}

	protected final MapTile scanLoc(MapLocation loc) throws GameActionException
	{
		MapTile tile = new MapTile();
		yieldSmallBC();

		TerrainTile tt = rc.senseTerrainTile(loc);

		switch (tt.getType()) {
			case LAND:
				tile.state = MapTile.LocState.Ground;
				break;
			case VOID:
				tile.state = MapTile.LocState.Air;
				break;
			case OFF_MAP:
				tile.state = MapTile.LocState.Bad;
				break;
		}

		Robot robot = rc.senseGroundRobotAtLocation(loc);
		if (robot != null) {
			tile.airRobot = new SimpleRobotInfo(rc.senseRobotInfo(robot));
		} else {
			tile.airRobot = null;
		}

		robot = rc.senseAirRobotAtLocation(loc);
		if (robot != null) {
			tile.groundRobot = new SimpleRobotInfo(rc.senseRobotInfo(robot));
		} else {
			tile.groundRobot = null;
		}

		tile.blockCount = rc.senseNumBlocksAtLocation(loc);
		tile.height = tt.getHeight();
		tile.roundSeen = timeNow;

		return tile;
	}

	/** Lokacja przed nosem. */
	protected MapLocation frontLoc()
	{
		return refreshLocation().add(rc.getDirection());
	}

	public ExecutionResult singleMove(MapLocation where) throws GameActionException
	{
		if (curLoc.equals(where)) {
			return ExecutionResult.OK;
		}

		yieldMv();

		Direction nextDir = curLoc.directionTo(where);
		rc.setDirection(nextDir);
		rc.yield();

		if (rc.canMove(rc.getDirection())) {
			rc.moveForward();
			rc.yield();
		} else {
			return ExecutionResult.Failed;
		}

		yieldMv();
		return ExecutionResult.OK;

	}

	public ExecutionResult pathFindMove(MapLocation where) throws GameActionException
	{
		rc.setIndicatorString(0, "pathFindMove");

		/*		try {
		if (rc.getRobotType() == RobotType.WORKER) {
		throw new ArithmeticException();
		}
		} catch (Exception e) {
		e.printStackTrace();
		}
		 */
//		debug_print("path find move");
		updateMap();

		FastAStar fastastar = new FastAStar();
		AStar astar = new AStar();
		int rstart = Clock.getRoundNum();
		rc.setIndicatorString(2, "Astar");
		Path path = astar.findPath(curLoc, where, map, rc.getRobotType());
		rc.setIndicatorString(2, "");
		debug_print("astar took:%d", Clock.getRoundNum() - rstart);
//		rstart = Clock.getRoundNum();
//		rc.setIndicatorString(2, "Astar");
//		path = fastastar.findPath(curLoc, where, map, rc.getRobotType());
//		rc.setIndicatorString(2, "");
//		debug_print("FAST astar took:%d", Clock.getRoundNum() - rstart);

//		debug_print("path find move 0.6");

		if (path == Path.emptyPath) {
			if (!where.equals(refreshLocation())) {
				return ExecutionResult.Failed;
			}
			return ExecutionResult.OK;

		}

//		debug_print("path find move 2");
		path.debug_print(map);
		if (rc.getRobotType() == RobotType.SOLDIER) {
			//map.debug_print();
			map.debug_print(path);
		}

		path.next(); //first loc == curLoc

		for (int i = 0; path.hasNext(); i++) {

			if (i % 5 == 0) {
				updateMap();
			}

//			debug_print("%d at: %s", Clock.getRoundNum(), refreshLocation().toString());
			if (rc.getRobotType() == RobotType.SOLDIER) {
//				map.debug_print();
//				map.debug_print(path);
			}

			MapLocation next = path.getNext();
			yieldMv();

			Direction nextDir = curLoc.directionTo(next);
			if (map.hasLoc(next) && map.getTile(next).state != MapTile.LocState.Ground) { //TODO
				debug_print("newPath");
				path = astar.findPath(refreshLocation(), where, map, rc.getRobotType());
				if (path == Path.emptyPath) {
					return ExecutionResult.OK;
				}
				path.next();
				continue;
			} else {
				rc.setDirection(nextDir);
				rc.yield();
			}

			if (rc.canMove(rc.getDirection())) {
				rc.moveForward();
				rc.yield();
			} else {
//TODO!				debug_print("newPath -- to nie powinno sie czesto zdarzac");
				path = astar.findPath(refreshLocation(), where, map, rc.getRobotType());
				if (path == Path.emptyPath) {
					return ExecutionResult.Failed;
				}
				path.next();
				continue;

//				return ExecutionResult.Failed;
			}

			handleInts();
			yieldMv();
		}

		return ExecutionResult.OK;
	}

	public ExecutionResult sleep(int howLong) throws GameActionException
	{
//		rc.setIndicatorString(0, "sleep");
		for (int i = 1; i <= howLong; i++) {
			rc.yield();
			handleInts();
		}

		return ExecutionResult.OK;
	}

	public List<IMessage> getMessages()
	{
		Message[] msgs = rc.getAllMessages();
		List<IMessage> ret = new ArrayList<IMessage>();

		try {
			for (Message m : msgs) {
				if (m == null || m.ints == null|| m.ints[0] != 123456788) {
					continue;
				}

				IMessage newMsg = null;
				int type = m.ints[1];
				newMsg = ((IMessage) messageTypes.get(type).newInstance());
				if (newMsg == null) {
					debug_print("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
				}

				if (newMsg.getRecipient() == null) {
					debug_print("AXXXXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
				}

				if (!checkRecipient(newMsg.getRecipient())) {
					continue;
				}
				newMsg.finalDeserialize(m);
				ret.add(newMsg);
			}
		} catch (Exception e) {
			debug_print("tutaj: ");
			e.printStackTrace();
		}

		return ret;

	}

	protected boolean isHungry()
	{
		return health() <= policy.hungerPolicy.hungerLevel;
	}

	protected List<RobotInfo> getAlliedGroundUnits() throws GameActionException
	{
		yieldMediumBC();
		ArrayList<RobotInfo> ret = new ArrayList<RobotInfo>(10);
		for (Robot robot : rc.senseNearbyGroundRobots()) {
			RobotInfo ri = rc.senseRobotInfo(robot);
			if (ri.team == myTeam) {
				ret.add(ri);
			}

		}
		return ret;
	}

	protected ArrayList<RobotInfo> getEnemyGroundUnits(
			ArrayList<RobotInfo> appendTo) throws GameActionException
	{
		ArrayList<RobotInfo> ret = appendTo;
		if (appendTo == null) {
			ret = new ArrayList<RobotInfo>(10);
		}
		for (Robot robot : rc.senseNearbyGroundRobots()) {
			RobotInfo ri = rc.senseRobotInfo(robot);
			if (ri.team != myTeam) {
				ret.add(ri);
			}

		}

		return ret;
	}

	protected ArrayList<RobotInfo> getEnemyAirUnits(
			ArrayList<RobotInfo> appendTo) throws GameActionException
	{
		ArrayList<RobotInfo> ret = appendTo;
		if (appendTo == null) {
			ret = new ArrayList<RobotInfo>(10);
		}
		for (Robot robot : rc.senseNearbyAirRobots()) {
			RobotInfo ri = rc.senseRobotInfo(robot);
			if (ri.team != myTeam) {
				ret.add(ri);
			}

		}

		return ret;
	}

	protected ArrayList<RobotInfo> getEnemies() throws GameActionException
	{
		yieldHalfBC();
		return getEnemyGroundUnits(getEnemyAirUnits(null));
	}

	protected void handleMapTransfer(MapTransferResponseMessage msg)
	{
		for (int i = 0; i < msg.locs.size(); i++) {
			MapLocation loc = msg.locs.get(i);
			MapTile tile = map.getTile(loc);
			MapTile newTile = msg.tiles.get(i);
			if (tile != null) {
				if (tile.roundSeen < newTile.roundSeen) {
					map.setTile(loc, tile);
				}
			} else {
				map.setTile(loc, newTile);
			}
		}
	}

	/** Nie sprawdza czy robot jest w zasiegu! */
	protected void feed(MapLocation where, RobotLevel rl, double howMuch) throws GameActionException
	{
		double maxTransfer = Math.max(0, rc.getEnergonLevel() - policy.minUnitEnergonLevel_Feed);
		double realHowMuch = Math.min(maxTransfer, howMuch);
		yieldSmallBC();
		if (realHowMuch > 0) {
			rc.transferEnergon(realHowMuch, where, rl);
		}
	}

	protected void healSomeGroundUnits() throws GameActionException //ale nie workerow
	{
		try {
			yieldSmallBC();
			refreshLocation();
			Robot robot;
			for (Direction dir : MapUtils.movableDirections) { //TOOD under
				MapLocation loc = curLoc.add(dir);
				if (rc.canSenseSquare(loc)) {
					robot = rc.senseGroundRobotAtLocation(loc);
				} else {
					robot = null;
				}
				if (robot != null) {
					RobotInfo ri = rc.senseRobotInfo(robot);
					if (ri.team == myTeam && ri.type == RobotType.SOLDIER &&
							(ri.eventualEnergon / ri.maxEnergon) < policy.healIfWeakerThan &&
							rc.getEnergonLevel() > policy.minUnitEnergonLevel_Feed) {
						double howMuch = Math.min(ri.maxEnergon - ri.eventualEnergon, GameConstants.ENERGON_RESERVE_SIZE);
						feed(loc, RobotLevel.ON_GROUND, howMuch);
					//debug_print("feed %f", howMuch);
					}
				}
			}
		} catch (Exception e) {
			debug_print("ZLE: heal failed");
		}
	}

	protected void onHungry() throws GameActionException
	{
		/**
		try {
		throw new ArithmeticException();

		} catch (Exception e) {
		e.printStackTrace();
		}
		 */
		rc.setIndicatorString(0, "onHungry");
		rc.setIndicatorString(2, "onHungry");
		for (;;) {
//			debug_print("onHungry loop");
			refreshLocation();

			yieldIf(ByteCodeConstants.Medium);
			MapLocation loc = nearestArchon();

			if (loc != null) {
				if (inTransferRange(loc)) {
//					debug_print("in transfer range hungry");
					if (--getState().hungryMessageDelay <= 0) {
						rc.broadcast(new HungerMessage(rc).finalSerialize());
						getState().hungryMessageDelay = 10;
						rc.yield();
					}
					getState().hungry_FindArchon = false;
					return;
				} else if (!getState().hungry_FindArchon) {
					rc.setIndicatorString(0, "onHungry - fp");
					getState().hungry_FindArchon = true;

					stupidWalkGoTo(null, CollisionPolicy.GoRound);
					getState().hungry_FindArchon = false; //?????!
				} else {
					return;
				}
			} else {
				debug_print("no archon");
				return;
			}
		}

	}
}
