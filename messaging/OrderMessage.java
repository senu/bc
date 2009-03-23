package batman.messaging;

import batman.management.order.Order;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.OrderDispatcher;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public class OrderMessage implements IMessage
{
	public Order order;

	public OrderMessage(Order order)
	{
		this.order = order;
	}

	public static final int getSerializedId()
	{
		return 6;
	}

	public Message serialize()
	{
		MutableMessage msg = new MutableMessage();
		order.serialize(msg);
		return msg.serialize();
	}

	public void deserialize(Message msg)
	{
		SerializationIterator it = new SerializationIterator();
		it.ii++; //type
		order = OrderDispatcher.getOrderByRepresentation(msg.strings[it.si++]);
		order.deserialize(msg, it);
	}
}
