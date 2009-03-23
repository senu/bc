package batman.messaging.message;

import batman.management.order.Order;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.OrderDispatcher;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public class OrderMessage extends MessageImpl
{
	public Order order;

	public OrderMessage(Order order)
	{
		this.order = order;
	}

	public final int getMessageType()
	{
		return 6;
	}

	public Message serialize()
	{
		MutableMessage msg = serializeStart();
		order.serialize(msg);
		return msg.serialize();
	}

	public void deserialize(Message msg)
	{
		SerializationIterator it = deserializeStart(msg);
		order = OrderDispatcher.getOrderByRepresentation(it.getString());
		order.deserialize(msg, it);
	}
}
