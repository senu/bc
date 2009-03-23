package batman.management.order;

import batman.management.executor.Executor;
import batman.management.*;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;
import battlecode.common.Message;

/**
 * Taka dziwna budowa, bo jest ograniczenie na wielkosc stosu.
 * @author senu
 */
public interface Order
{
	public String getOrderName();

	public ExecutionResult execute(Executor executor) throws GameActionException;

	public void deserialize(Message m, SerializationIterator it);

	public void serialize(MutableMessage m);
}
