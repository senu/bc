package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * O1;O2;O3...
 * @author senu
 */
public class OrderGroup implements Order
{
	List<Order> orders = new ArrayList<Order>();

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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void serialize(MutableMessage m)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
