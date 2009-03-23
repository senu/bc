package batman.messaging.serialization;

import batman.management.order.Order;
import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SingleMoveOrder;
import batman.messaging.message.ChangeRobotPolicyOrder;

/**
 *
 * @author senu
 */
public class OrderDispatcher
{
	public static Order getOrderByRepresentation(String name)
	{

		if (name.equals("PathFindMoveOrder")) {
			return new PathFindMoveOrder();
		} else if (name.equals("SingleMoveOrder")) {
			return new SingleMoveOrder();
		} else if (name.equals("ChangeRobotPolicyOrder")) {
			return new ChangeRobotPolicyOrder();
		} else if (name.equals("SendMessageOrder")) {
			return new SendMessageOrder();
		} else if (name.equals("OrderGroup")) {
			return new OrderGroup();
		}

		return null;
	}
}
