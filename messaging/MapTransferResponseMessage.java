package batman.messaging;

import batman.pathfinding.MapTile;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author senu
 */
public class MapTransferResponseMessage implements IMessage
{
	public List<MapTile> tiles;
	public List<MapLocation> locs;

	public Message serialize()
	{
		List<Integer> ints = new ArrayList<Integer>(tiles.size() * 4);

		ints.add(Messages.MSG_MAP_TRANSFER_RESPONSE);

		for (MapTile tile : tiles) {
			ints.add(tile.blockCount);
			ints.add(tile.height);
			ints.add(tile.roundSeen);
			ints.add(tile.state.ordinal());
			ints.add(tile.groundRobot.type.ordinal());
			ints.add(tile.groundRobot.team.ordinal());
			ints.add(tile.airRobot.type.ordinal());
			ints.add(tile.airRobot.team.ordinal());
		}

		Message m = new Message();

		m.ints = new int[ints.size()];
		int pos = 0;
		for (Integer i : ints) {
			m.ints[pos++] = i;
		}

		m.locations = locs.toArray(new MapLocation[0]);

		return m;
	}

	public void deserialize(Message msg)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}


}
