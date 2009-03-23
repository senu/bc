package batman.messaging;

import battlecode.common.MapLocation;
import battlecode.common.Message;

/**
 * Prosba o przesylanie informacji o polach z przedzialu [minLoc, maxLoc],
 * ktore skanowano pozniej niz minRound.
 */
public class MapTransferRequestMessage implements IMessage
{
	public MapLocation minLoc,  maxLoc;
	public int minRound;

	public static final int getSerializedId()
	{
		return 4;
	}

	public MapTransferRequestMessage(MapLocation minLoc, MapLocation maxLoc, int minRound)
	{
		this.minLoc = minLoc;
		this.maxLoc = maxLoc;
		this.minRound = minRound;
	}

	public Message serialize()
	{
		Message m = new Message();

		m.ints = new int[]{Messages.MSG_MAP_TRANSFER_REQUEST, minRound};
		m.locations = new MapLocation[]{minLoc, maxLoc};

		return m;
	}

	public void deserialize(Message msg)
	{
		minLoc = msg.locations[0];
		maxLoc = msg.locations[1];
		minRound = msg.ints[1];
	}
}
