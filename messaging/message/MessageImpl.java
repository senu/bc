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

	public int getPriority()
	{
		return priority;
	}

	public int getRound()
	{
		return round;
	}

	public final void finalDeserialize(Message msg)
	{
		round = msg.ints[1];
		priority = msg.ints[2];

		deserialize(new SerializationIterator(msg, 3, 0, 0));

	}

	public final Message finalSerialize()
	{
		MutableMessage m = new MutableMessage();
//		m.strings.add(this.getClass().getName());
		m.ints.add(getMessageType());
		m.ints.add(round);
		m.ints.add(priority);

		serialize(m);
		return m.serialize();
	}
}