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
public class SingleMoveOrder implements Order {

	public MapLocation where;

	public SingleMoveOrder(MapLocation where) {
		this.where = where;
	}

	public SingleMoveOrder() {
	}

	public ExecutionResult execute(Executor executor) throws GameActionException {
		return executor.executeSingleMoveOrder(this);
	}

	public String getOrderName() {
		return this.getClass().getSimpleName();
	}

	public void deserialize(Message m, SerializationIterator it) {
		where = m.locations[it.li++];
	}

	public void serialize(MutableMessage m) {
		m.strings.add(getOrderName());
		m.locations.add(where);
	}
}
