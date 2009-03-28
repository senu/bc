package batman.management.executor;

import batman.management.order.AttackMoveOrder;
import batman.management.order.BeMedicOrder;
import batman.management.order.Order;
import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SimpleMoveOrder;
import batman.management.result.ExecutionResult;
import batman.management.order.ChangeRobotPolicyOrder;
import batman.strategy.policy.CollisionPolicy;
import batman.unit.Soldier;
import batman.utils.DebugUtils;
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

	public ExecutionResult executeSimpleMoveOrder(SimpleMoveOrder order) throws GameActionException
	{
		DebugUtils.debug_print("simpleMoveOrder %s", order.where.toString());
		return target.stupidWalkGoTo(order.where, CollisionPolicy.WaitALitte);
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

	public ExecutionResult executeBeMedicOrder(BeMedicOrder order) throws GameActionException
	{
		return ExecutionResult.Failed;
	}

	public ExecutionResult executeAttackMoveOrder(AttackMoveOrder order) throws GameActionException
	{
		return target.attackMove(order.where);
	}
}
