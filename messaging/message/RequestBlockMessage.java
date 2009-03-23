package batman.messaging.message;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.MapLocation;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public class RequestBlockMessage extends MessageImpl
{
	/** Jak daleko ma odejsc by szukac bloku */
	int howFar;
	MapLocation whereToUnload;

	@Override
	public int getMessageType()
	{
		return 2;
	}

	public Message serialize()
	{
		MutableMessage m = serializeStart();

		m.ints.add(howFar);
		m.locations.add(whereToUnload);

		return m.serialize();

	}

	public void deserialize(Message msg)
	{
		SerializationIterator it = deserializeStart(msg);
		howFar = it.getInt();
		whereToUnload = it.getLoc();
	}
}
