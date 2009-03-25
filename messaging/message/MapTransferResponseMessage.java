package batman.messaging.message;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import batman.pathfinding.MapTile;
import batman.utils.DebugUtils;
import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author senu
 */
public class MapTransferResponseMessage extends MessageImpl
{
	public List<MapTile> tiles;
	public List<MapLocation> locs;

	public MapTransferResponseMessage(Set<Entry<MapLocation, MapTile>> entries)
	{
		tiles = new ArrayList<MapTile>(entries.size());
		locs = new ArrayList<MapLocation>(entries.size());
		for (Entry<MapLocation, MapTile> entry : entries) {
			tiles.add(entry.getValue());
			locs.add(entry.getKey());
		}
	}

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

		int ts = Clock.getRoundNum();
		m.ints.add(tiles.size());

		m.ints.ensureCapacity(m.ints.size() + tiles.size() * 8);

		ObjectOutputStream oos = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			oos = new ObjectOutputStream(bos);
			for (Iterator<MapTile> it = tiles.iterator(); it.hasNext();) {
				MapTile tile = it.next();
//				DebugUtils.debug_print("loop: %d", Clock.getBytecodeNum());
				oos.writeObject(tile);
			/*
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
			 */
			}
			oos.close();
			m.strings.add(new String(bos.toByteArray(), "8859_1"));
		} catch (IOException ex) {
			DebugUtils.debug_print("java ser.");
			ex.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException ex) {
			}
		}

//		DebugUtils.debug_print("msg serialize %s", new String(bos.toByteArray(), "8859_1"));
		m.locations.addAll(locs);
		DebugUtils.debug_print("msg serialize  took: %d", Clock.getRoundNum() - ts);
	}

	public void deserialize(SerializationIterator it)
	{

		int tilesN = it.getInt();

		ObjectInputStream oos = null;
		try {
			oos = new ObjectInputStream(new ByteArrayInputStream(it.getString().getBytes("8859_1")));
			for (int i = 0; i < tilesN; i++) {
				MapTile tile = (MapTile) oos.readObject();

				/*
				tile.blockCount = it.getInt();
				tile.height = it.getInt();
				tile.roundSeen = it.getInt();
				tile.state = MapTile.LocState.values()[it.getInt()];
				tile.groundRobot.type = RobotType.values()[it.getInt()];
				tile.groundRobot.team = Team.values()[it.getInt()];
				tile.airRobot.type = RobotType.values()[it.getInt()];
				tile.airRobot.team = Team.values()[it.getInt()];

				 */
				tiles.add(tile);

			}
		} catch (Exception ex) {
			DebugUtils.debug_print("java ser.");
			ex.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException ex) {
			}
		}
		for (int i = 0; i < tilesN; i++) {
			locs.add(it.getLoc());
		}

	}
}
