package batman.management.executor;

import batman.management.order.Order;
import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SingleMoveOrder;
import batman.management.result.ExecutionResult;
import batman.management.order.ChangeRobotPolicyOrder;
import batman.unit.Soldier;
import battlecode.common.GameActionException;

/**
 *
 * @author senu
 */
public class SoldierExecutor implements Executor
{
	Soldier target;

	public SoldierExecutor(Soldier target)
	{
		this.target = target;
	}

	public ExecutionResult executeSendMessageOrder(SendMessageOrder order)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ExecutionResult executeSingleMoveOrder(SingleMoveOrder order) throws GameActionException
	{
		return target.singleMove(order.where);
	}

	public ExecutionResult executePathFindMoveOrder(PathFindMoveOrder order) throws GameActionException
	{
		return target.pathFindMove(order.where);
	}

	public ExecutionResult executeChangeRobotPolicyOrder(ChangeRobotPolicyOrder order) throws GameActionException
	{
		target.policy = order.newRobotPolicy;
		return ExecutionResult.OK;
	}

	public ExecutionResult executeOrderGroup(OrderGroup order) throws GameActionException
	{
		ExecutionResult res = ExecutionResult.OK;

		for (Order corder : order.orders) {
			res = corder.execute(this);
		}

		return res;
	}
}
