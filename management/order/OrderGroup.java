package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;

/**
 * O1;O2;O3...
 * @author senu
 */
public class OrderGroup implements Order
{
	public OrderList orders = new OrderList(5);

	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executeOrderGroup(this);
	}

	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}

	public void deserialize(SerializationIterator it)
	{
		orders.deserialize(it);
	}

	public void serialize(MutableMessage m)
	{
		m.strings.add(getOrderName());
		orders.serialize(m);
	}
}
