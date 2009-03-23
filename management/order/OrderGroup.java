package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.Message;

/**
 * O1;O2;O3...
 * @author senu
 */
public class OrderGroup implements Order
{


	public ExecutionResult execute(Executor executor)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}

	public void deserialize(Message msg)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Message serialize()
	{
		throw new UnsupportedOperationException("Not supported yet.");
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
