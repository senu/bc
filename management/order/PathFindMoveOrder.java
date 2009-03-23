package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public class PathFindMoveOrder implements Order
{
	public MapLocation where;

	public PathFindMoveOrder(MapLocation where)
	{
		this.where = where;
	}

	public PathFindMoveOrder()
	{
	}

	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executePathFindMoveOrder(this);
	}

	public void deserialize(SerializationIterator it)
	{
		where = it.getLoc();
	}

	public void serialize(MutableMessage m)
	{
		m.locations.add(where);
	}

	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}
}
