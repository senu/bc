package batman.messaging.serialization;

import batman.management.order.Order;
import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SingleMoveOrder;

/**
 *
 * @author senu
 */
public class OrderDispatcher
{
	public Order getOrderByRepresentation(String name)
	{

		if (name.equals("PathFindMoveOrder")) {
			return new PathFindMoveOrder();
		} else if (name.equals("SinleMoveOrder")) {
			return new SingleMoveOrder();
		} else if (name.equals("SendMessageOrder")) {
			return new SendMessageOrder();
		} else if (name.equals("OrderGroup")) {
			return new OrderGroup();
		}

		return null;
	}
}
