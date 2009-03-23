package batman.messaging.message;

import batman.management.executor.Executor;
import batman.management.order.Order;
import batman.management.result.ExecutionResult;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import batman.strategy.RobotPolicy;
import battlecode.common.GameActionException;

/**
 *
 * @author senu
 */
public class ChangeRobotPolicyOrder implements Order
{
	public RobotPolicy newRobotPolicy;

	public ChangeRobotPolicyOrder(RobotPolicy newRobotPolicy)
	{
		this.newRobotPolicy = newRobotPolicy;
	}

	public ChangeRobotPolicyOrder()
	{
		newRobotPolicy = new RobotPolicy();
	}

	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}

	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executeChangeRobotPolicyOrder(this);
	}

	public void deserialize(SerializationIterator it)
	{
		newRobotPolicy.deserialize(it);
	}

	public void serialize(MutableMessage m)
	{
		newRobotPolicy.serialize(m);
	}
}
