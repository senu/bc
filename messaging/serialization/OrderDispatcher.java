package batman.messaging.serialization;

import batman.management.order.AttackMoveOrder;
import batman.management.order.BeMedicOrder;
import batman.management.order.Order;
import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SimpleMoveOrder;
import batman.management.order.ChangeRobotPolicyOrder;

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
		} else if (name.equals("SimpleMoveOrder")) {
			return new SimpleMoveOrder();
		} else if (name.equals("ChangeRobotPolicyOrder")) {
			return new ChangeRobotPolicyOrder();
		} else if (name.equals("SendMessageOrder")) {
			return new SendMessageOrder();
		} else if (name.equals("OrderGroup")) {
			return new OrderGroup();
		} else if (name.equals("AttackMoveOrder")) {
			return new AttackMoveOrder();
		} else if (name.equals("BeMedicOrder")) {
			return new BeMedicOrder();
		}

		System.out.println("zla nazawa: " + name);
		return null;
	}
}
