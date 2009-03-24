package batman.messaging.message;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import batman.pathfinding.MapTile;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotType;
import battlecode.common.Team;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author senu
 */
public class MapTransferResponseMessage extends MessageImpl
{
	public List<MapTile> tiles;
	public List<MapLocation> locs;

	public MapTransferResponseMessage()
	{
		tiles = new ArrayList<MapTile>();
		locs = new ArrayList<MapLocation>();
	}

	@Override
	public int getMessageType()
	{
		return 5;
	}

	public void serialize(MutableMessage m)
	{

		m.ints.add(tiles.size());
		m.ints.add(locs.size());

		List<Integer> ints = new ArrayList<Integer>(tiles.size() * 8);

		for (MapTile tile : tiles) {
			m.ints.add(tile.blockCount);
			m.ints.add(tile.height);
			m.ints.add(tile.roundSeen);
			m.ints.add(tile.state.ordinal());
			if (tile.groundRobot != null) {
				m.ints.add(tile.groundRobot.type.ordinal());
				m.ints.add(tile.groundRobot.team.ordinal());
			} else {
				m.ints.add(-1);
				m.ints.add(-1);
			}
			if (tile.airRobot != null) {
				m.ints.add(tile.airRobot.type.ordinal());
				m.ints.add(tile.airRobot.team.ordinal());
			} else {
				m.ints.add(-1);
				m.ints.add(-1);
			}
		}


	}

	public void deserialize(SerializationIterator it)
	{

		int tilesN = it.getInt();
		int locsN = it.getInt();


		for (int i = 0; i < tilesN; i++) {
			MapTile tile = new MapTile();

			tile.blockCount = it.getInt();
			tile.height = it.getInt();
			tile.roundSeen = it.getInt();
			tile.state = MapTile.LocState.values()[it.getInt()];
			tile.groundRobot.type = RobotType.values()[it.getInt()];
			tile.groundRobot.team = Team.values()[it.getInt()];
			tile.airRobot.type = RobotType.values()[it.getInt()];
			tile.airRobot.team = Team.values()[it.getInt()];

			tiles.add(tile);

		}

		for (int i = 0; i < locsN; i++) {
			locs.add(it.getLoc());
		}

	}
}
