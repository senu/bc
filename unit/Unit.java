package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.management.result.ExecutionResult;
import batman.messaging.message.IMessage;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.MapTransferRequestMessage;
import batman.messaging.message.MapTransferResponseMessage;
import batman.messaging.message.MessageImpl;
import batman.messaging.message.OrderMessage;
import batman.messaging.message.RequestBlockMessage;
import batman.pathfinding.AStar;
import batman.utils.MapUtils;
import batman.pathfinding.GameMap;
import batman.pathfinding.MapTile;
import batman.pathfinding.Path;
import batman.strategy.RobotPolicy;
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
	public RobotPolicy policy = new RobotPolicy();

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
	}

	public abstract void beYourself() throws GameActionException;

	protected final MapLocation refreshLocation()
	{
		curLoc = rc.getLocation();
		return curLoc;
	}

	protected final void debug_print(String format, Object... args)
	{
		System.out.printf(rc.getRobot().getID() + " " + format + "\n", args);
	}

	protected final void ping() throws GameActionException
	{
		for (int i = 1; i <= 10; i++) {
//TODO			rc.broadcast(Messages.newSimpleMessage(Messages.MSG_PING));
			rc.yield();
		}
	}

	protected boolean hasEnergon(double howMuch)
	{
		return rc.getEnergonLevel() > howMuch;
	}

	/** Zwraca rowniez zle lokacje (poza mapa). */
	public List<MapLocation> getLocsInSensorRange() throws GameActionException
	{
		List<MapLocation> retLocs = new ArrayList<MapLocation>();
		int range = rc.getRobotType().sensorRadius();
		refreshLocation();
		MapLocation mxy = new MapLocation(curLoc.getX() - range, curLoc.getY() - range); //minimum

		//TODO angle
		for (MapLocation mx = mxy; mx.getY() - mxy.getY() <= 2 * range; mx = mx.add(Direction.SOUTH)) {
			for (MapLocation loc = mx; loc.getX() - mxy.getX() <= 2 * range; loc = loc.add(Direction.EAST)) {

				if (rc.canSenseSquare(loc)) {
					retLocs.add(loc);
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
		while (rc.isMovementActive()) {
			rc.yield();
		}
	}

	protected final void yieldAtt()
	{
		while (rc.isAttackActive()) {
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

	//TODO remove it
	protected final void goTo(MapLocation nextLoc) throws GameActionException
	{
		if (curLoc.equals(nextLoc)) {
			return;
		}
		yieldMv();

		//nextLoc = a_star_loc(curLoc, nextLoc); //TODO

		Direction nextDir = curLoc.directionTo(nextLoc);

		if (rc.canMove(nextDir)) {
			yieldSmallBC();
			rc.setDirection(nextDir);
			rc.yield();

			if (rc.canMove(rc.getDirection())) {
				rc.moveForward();
			} else {
				ping();
			}

			rc.yield();
			yieldMv();
		} else {
//			debug_print("bad loc");
			if (rc.canSenseSquare(nextLoc) && rc.senseGroundRobotAtLocation(nextLoc) == null) {
				map.setTile(nextLoc, new MapTile(MapTile.LocState.Bad)); //TODO to blokuje pole na zawsze, gdy stoi tam robot
			}
		}
	}

	protected void updateMap() throws GameActionException
	{
		int rstart = Clock.getRoundNum();
		List<MapLocation> locs = getLocsInSensorRange();
		for (MapLocation loc : locs) {
			map.setTile(loc, scanLoc(loc));
		}

//		debug_print("updateMap took:%d", Clock.getRoundNum() - rstart);
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

	protected abstract void handleInts() throws GameActionException;

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

		AStar astar = new AStar();
		int rstart = Clock.getRoundNum();
		rc.setIndicatorString(2, "Astar");
		Path path = astar.findPath(curLoc, where, map, rc.getRobotType());
		rc.setIndicatorString(2, "");
		debug_print("astar took:%d", Clock.getRoundNum() - rstart);

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

	public ExecutionResult sleep(int howLong)
	{
		rc.setIndicatorString(0, "sleep");
		for (int i = 1; i <= howLong; i++) {
			rc.yield();
		}

		return ExecutionResult.OK;
	}

	public List<IMessage> getMessages()
	{
		Message[] msgs = rc.getAllMessages();
		List<IMessage> ret = new ArrayList<IMessage>();


		//TODO in ctor

		//TODO tak sie tego nie robi
		Class[] messageClasses = new Class[]{
			HungerMessage.class,
			MapTransferRequestMessage.class,
			MapTransferResponseMessage.class,
			OrderMessage.class,
			RequestBlockMessage.class,};

		Map<Integer, Class> messageTypes = new HashMap<Integer, Class>();

		try {
			for (Class clazz : messageClasses) {
				messageTypes.put(((MessageImpl) clazz.newInstance()).getMessageType(), clazz);
			}

			for (Message m : msgs) {
				if (m.ints[0] != 123456789) {
					continue;
				}
				int type = m.ints[1];
				IMessage newMsg = ((IMessage) messageTypes.get(type).newInstance());
				newMsg.finalDeserialize(m);
				ret.add(newMsg);
			}

		} catch (Exception e) {
			debug_print("serialization errror");
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
		ArrayList<RobotInfo> ret = new ArrayList<RobotInfo>(10);
		for (Robot robot : rc.senseNearbyGroundRobots()) {
			RobotInfo ri = rc.senseRobotInfo(robot);
			if (ri.team == myTeam) {
				ret.add(ri);
			}

		}
		return ret;
	}

	protected List<RobotInfo> getEnemyGroundUnits() throws GameActionException
	{
		ArrayList<RobotInfo> ret = new ArrayList<RobotInfo>(10);
		for (Robot robot : rc.senseNearbyGroundRobots()) {
			RobotInfo ri = rc.senseRobotInfo(robot);
			if (ri.team != myTeam) {
				ret.add(ri);
			}

		}

		return ret;
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
}
