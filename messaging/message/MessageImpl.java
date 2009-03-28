package batman.messaging.message;

import batman.messaging.Recipient;
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
	public Recipient recipient = new Recipient();

	public int getPriority()
	{
		return priority;
	}

	public int getRound()
	{
		return round;
	}

	public Recipient getRecipient()
	{
		return recipient;
	}

	public final void finalDeserialize(Message msg)
	{
		SerializationIterator it = new SerializationIterator(msg, 2, 0, 0);
		round = it.getInt();
		priority = it.getInt();
		recipient.deserialize(it);

		deserialize(it);

	}

	public final Message finalSerialize()
	{
		MutableMessage m = new MutableMessage();
//		m.strings.add(this.getClass().getName());
		m.ints.add(123456788);
		m.ints.add(getMessageType());
		m.ints.add(round);
		m.ints.add(priority);
		recipient.serialize(m);

		serialize(m);
		return m.serialize();
	}
}
