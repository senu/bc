package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.messaging.Messages;
import batman.utils.Utils;
import batman.pathfinding.GameMap;
import batman.pathfinding.MapTile;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;
import java.util.ArrayList;
import java.util.List;
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
			rc.broadcast(Messages.newSimpleMessage(Messages.MSG_PING));
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
		return Utils.closest(rc.senseAlliedArchons(), rc.getLocation());
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

//TODO czasem za duzo		debug_print("updateMap took:%d", Clock.getRoundNum() - rstart);
	}

	protected final MapTile scanLoc(MapLocation loc) throws GameActionException
	{
		MapTile tile = new MapTile();
		yieldMediumBC();

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

		tile.airRobot = rc.senseAirRobotAtLocation(loc);
		tile.groundRobot = rc.senseGroundRobotAtLocation(loc);
		tile.blockCount = rc.senseNumBlocksAtLocation(loc);
		tile.height = tt.getHeight();

		return tile;
	}

	/** Lokacja przed nosem. */
	protected MapLocation frontLoc()
	{
		return refreshLocation().add(rc.getDirection());
	}
}
