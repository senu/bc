package batman.messaging.message;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.MapLocation;
import battlecode.common.Message;

/**
 * Prosba o przesylanie informacji o polach z przedzialu [minLoc, maxLoc],
 * ktore skanowano pozniej niz minRound.
 */
public class MapTransferRequestMessage extends MessageImpl
{
	public MapLocation minLoc,  maxLoc;
	public int minRound;

	public final int getMessageType()
	{
		return 4;
	}

	public MapTransferRequestMessage()
	{
	}

	public MapTransferRequestMessage(MapLocation minLoc, MapLocation maxLoc, int minRound)
	{
		this.minLoc = minLoc;
		this.maxLoc = maxLoc;
		this.minRound = minRound;
	}

	public Message serialize()
	{
		MutableMessage m = serializeStart();

		m.ints.add(minRound);
		m.locations.add(minLoc);
		m.locations.add(maxLoc);

		return m.serialize();

	}

	public void deserialize(Message msg)
	{
		SerializationIterator it = deserializeStart(msg);
		minRound = it.getInt();
		minLoc = it.getLoc();
		maxLoc = it.getLoc();
	}
}
