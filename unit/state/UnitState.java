package batman.unit.state;

import batman.management.order.Order;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 * @author senu
 */
public class UnitState
{
	public boolean hungry_FindArchon = false;
	public Queue<Order> orderQueue = new ArrayDeque<Order>();
}
