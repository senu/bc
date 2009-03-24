package batman.management.executor;

import batman.management.order.OrderGroup;
import batman.management.order.PathFindMoveOrder;
import batman.management.order.SendMessageOrder;
import batman.management.order.SingleMoveOrder;
import batman.management.result.ExecutionResult;
import batman.management.order.ChangeRobotPolicyOrder;
import batman.strategy.RobotPolicy;
import battlecode.common.GameActionException;

/**
 *
 * @author senu
 */
public interface Executor
{
	public ExecutionResult executeSingleMoveOrder(SingleMoveOrder order) throws GameActionException;

	public ExecutionResult executeSendMessageOrder(SendMessageOrder order) throws GameActionException;

	public ExecutionResult executePathFindMoveOrder(PathFindMoveOrder order) throws GameActionException;

	public ExecutionResult executeChangeRobotPolicyOrder(ChangeRobotPolicyOrder order) throws GameActionException;

	public ExecutionResult executeOrderGroup(OrderGroup order) throws GameActionException;
}
