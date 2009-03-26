package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.management.executor.SoldierExecutor;
import batman.messaging.Recipient;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.IMessage;
import batman.messaging.message.MapTransferResponseMessage;
import batman.messaging.message.OrderMessage;
import batman.unit.state.SoldierState;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import java.util.List;

/**
 *
 * @author senu
 */
public class Soldier extends Unit
{
	protected SoldierExecutor executor = new SoldierExecutor(this);
	public SoldierState state = new SoldierState();

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
			//rc.yield();
			if (!state.orderQueue.isEmpty()) {
//				debug_print("begin of execute");
				state.orderQueue.remove().execute(executor);
//				debug_print("end of execute");
			}
		}
	}

	protected void onHungry() throws GameActionException
	{
		/**
		try {
		throw new ArithmeticException();

		} catch (Exception e) {
		e.printStackTrace();
		}
		 */
		rc.setIndicatorString(0, "onHungry");
		for (;;) {
//			debug_print("onHungry loop");
			refreshLocation();

			yieldIf(ByteCodeConstants.Medium);
			MapLocation loc = nearestArchon();

			if (loc != null) {
				if (inTransferRange(loc)) {
//					debug_print("in transfer range hungry");
					rc.broadcast(new HungerMessage(rc).finalSerialize());
					state.hungry_FindArchon = false;
					sleep(5);
					return;
				} else if (!state.hungry_FindArchon) {
					rc.setIndicatorString(0, "onHungry - fp");
//					debug_print("hungry = fp");
					state.hungry_FindArchon = true;

					pathFindMove(loc);
					state.hungry_FindArchon = false; //?????!
					/*if (pathFindMove(loc) != ExecutionResult.OK) {
				state.hungry_FindArchon = false; //?????!
				}*/
				} else {
					return;
				}
			} else {
				//TODO
				debug_print("no archon");
				return;
			}
		}

	}

	protected void processMessages() throws GameActionException
	{
		for (IMessage inMsg : getMessages()) {
			if (inMsg instanceof OrderMessage) {
				OrderMessage msg = (OrderMessage) inMsg;
				state.orderQueue.add(msg.order);
			} else if (inMsg instanceof MapTransferResponseMessage) {
				handleMapTransfer((MapTransferResponseMessage) inMsg);
				debug_print("got map transfer");
				map.debug_print();
			}
		}
	}

	protected final void handleInts() throws GameActionException
	{
		rc.setIndicatorString(0, "handleInts");
		rc.setIndicatorString(1, policy.hungerPolicy.toString());

		if (isHungry()) {
//			debug_print("isHungry");
			onHungry();
			return;
		}

		List<RobotInfo> enemies = getEnemyGroundUnits();
		yieldAtt();
		for (RobotInfo enemy : enemies) {
			if (rc.canAttackSquare(enemy.location)) {
				rc.attackGround(enemy.location);
				break;
			}
		}

		processMessages();

	}

	@Override
	protected boolean checkRecipient(Recipient recipient) throws GameActionException
	{
		return (recipient.toWhom.flag & Recipient.RecipientType.Soldiers.flag) == Recipient.RecipientType.Soldiers.flag; //TODO medics
	}
}
