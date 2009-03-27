package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 *
 * @author senu
 */
public class SimpleMoveOrder implements Order
{
	public MapLocation where;

	public SimpleMoveOrder(MapLocation where)
	{
		this.where = where;
	}

	public SimpleMoveOrder()
	{
	}

	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executeSimpleMoveOrder(this);
	}

	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}

	public void deserialize(SerializationIterator it)
	{
		where = it.getLoc();
	}

	public void serialize(MutableMessage m)
	{
		m.strings.add(getOrderName());
		m.locations.add(where);
	}
}
