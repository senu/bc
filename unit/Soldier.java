package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.messaging.Messages;
import batman.constants.StrategyConstants;
import batman.management.executor.SoldierExecutor;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.IMessage;
import batman.messaging.message.OrderMessage;
import batman.strategy.policy.HungerPolicy;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 *
 * @author senu
 */
public class Soldier extends Unit
{
	protected SoldierExecutor executor = new SoldierExecutor(this);

	public Soldier(RobotController rc)
	{
		super(rc);
	}

	@Override
	public void beYourself() throws GameActionException
	{
		for (;;) {
			handleInts();
			rc.yield();
			updateMap();
			rc.yield();
		}
	}

	protected void onHungry() throws GameActionException
	{
		for (;;) {
			refreshLocation();

			yieldIf(ByteCodeConstants.Medium);
			MapLocation loc = nearestArchon();

			if (loc != null) {
				if (inTransferRange(loc)) {
					rc.broadcast(new HungerMessage(rc).finalSerialize());
					while (isHungry()) {
						rc.yield();
					}
					return;
				} else {
//						debug_print("hungry");
					goTo(loc); //TODO
				}
			}
		}

	}

	protected void processMessages() throws GameActionException
	{
		for (IMessage inMsg : getMessages()) {
			if (inMsg instanceof OrderMessage) {
				OrderMessage msg = (OrderMessage) inMsg;
				msg.order.execute(executor);
			}
		}
	}


	protected final void handleInts() throws GameActionException
	{
		if (isHungry()) {
			onHungry();
		} else {
			processMessages();
		}
	}
}
