package batman.messaging;

import batman.management.order.Order;
import batman.messaging.serialization.MutableMessage;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public class OrderMessage implements IMessage
{
	public Order order;

	public Message serialize()
	{
		MutableMessage msg = new MutableMessage();
		order.serialize(msg);
		return msg.serialize();
	}

	public void deserialize(Message msg)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
