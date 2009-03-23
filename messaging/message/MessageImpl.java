package batman.messaging.message;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public abstract class MessageImpl implements IMessage
{
	public int round;
	public int priority;

	public abstract int getMessageType();

	public SerializationIterator deserializeStart(Message msg)
	{
		round = msg.ints[1];
		priority = msg.ints[2];
		return new SerializationIterator(msg, 3, 0, 0);
	}

	public int getPriority()
	{
		return priority;
	}

	public int getRound()
	{
		return round;
	}

	public MutableMessage serializeStart()
	{
		MutableMessage m = new MutableMessage();
		m.ints.add(getMessageType());
		m.ints.add(round);
		m.ints.add(priority);

		return m;
	}
}
