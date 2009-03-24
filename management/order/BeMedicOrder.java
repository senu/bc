package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import battlecode.common.GameActionException;

/**
 *
 * @author senu
 */
public class BeMedicOrder extends SimpleOrder
{
	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executeBeMedicOrder(this);
	}
}
