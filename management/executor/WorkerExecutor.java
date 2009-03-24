package batman.management.executor;

import batman.management.order.BeMedicOrder;
import batman.management.order.ChangeRobotPolicyOrder;
import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SingleMoveOrder;
import batman.management.result.ExecutionResult;
import batman.strategy.policy.HungerPolicy;
import batman.strategy.policy.custom.WorkerPolicy;
import batman.unit.Worker;
import battlecode.common.GameActionException;

/**
 *
 * @author senu
 */
public class WorkerExecutor implements Executor
{
	public Worker target;

	public WorkerExecutor(Worker target)
	{
		this.target = target;
	}

	public ExecutionResult executeChangeRobotPolicyOrder(ChangeRobotPolicyOrder order) throws GameActionException
	{
		return ExecutionResult.Failed;
	}

	public ExecutionResult executeOrderGroup(OrderGroup order) throws GameActionException
	{
		return ExecutionResult.Failed;
	}

	public ExecutionResult executePathFindMoveOrder(PathFindMoveOrder order) throws GameActionException
	{
		return target.pathFindMove(order.where);
	}

	public ExecutionResult executeSendMessageOrder(SendMessageOrder order) throws GameActionException
	{
		return ExecutionResult.Failed;
	}

	public ExecutionResult executeSingleMoveOrder(SingleMoveOrder order) throws GameActionException
	{
		return ExecutionResult.Failed;
	}

	public ExecutionResult executeBeMedicOrder(BeMedicOrder order) throws GameActionException
	{
		target.policy.hungerPolicy = HungerPolicy.HungryAt35;
		target.workerPolicy = WorkerPolicy.BeMedic;
		return ExecutionResult.OK;
	}
}
