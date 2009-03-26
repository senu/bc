package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Stara się dojść do wskazanej pozycji (stupid walk)
 * attakując wszystko co jest w jego zasięgu.
 * @author senu
 */
public class AttackMoveOrder implements Order
{
	public MapLocation where;

	public AttackMoveOrder(MapLocation where)
	{
		this.where = where;
	}

	public AttackMoveOrder()
	{
	}

	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executeAttackMoveOrder(this);
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
