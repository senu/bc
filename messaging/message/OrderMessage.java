package batman.messaging.message;

import batman.management.order.Order;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.OrderDispatcher;
import batman.messaging.serialization.SerializationIterator;

/**
 *
 * @author senu
 */
public class OrderMessage extends MessageImpl
{
	public Order order;

	public OrderMessage()
	{
	}

	public OrderMessage(Order order)
	{
		this.order = order;
	}

	public final int getMessageType()
	{
		return 6;
	}

	public void serialize(MutableMessage msg)
	{
		order.serialize(msg);
	}

	public void deserialize(SerializationIterator it)
	{
		order = OrderDispatcher.getOrderByRepresentation(it.getString());
		order.deserialize(it);
	}
}
