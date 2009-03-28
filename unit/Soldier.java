package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.constants.StrategyConstants;
import batman.management.executor.SoldierExecutor;
import batman.management.result.ExecutionResult;
import batman.messaging.Recipient;
import batman.messaging.message.HungerMessage;
import batman.messaging.message.IMessage;
import batman.messaging.message.MapTransferResponseMessage;
import batman.messaging.message.OrderMessage;
import batman.strategy.policy.CollisionPolicy;
import batman.strategy.policy.EnemySpottedPolicy;
import batman.strategy.policy.HungerPolicy;
import batman.unit.state.SoldierState;
import battlecode.common.Clock;
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
		policy.hungerPolicy = HungerPolicy.HungryAt50;
		policy.collisionPolicy = CollisionPolicy.AlwaysWait;
		policy.minUnitEnergonLevel_Feed = StrategyConstants.SOLDIER_MIN_ENERGON_LEVEL;
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
		rc.setIndicatorString(2, "onHungry");
		for (;;) {
//			debug_print("onHungry loop");
			refreshLocation();

			yieldIf(ByteCodeConstants.Medium);
			MapLocation loc = nearestArchon();

			if (loc != null) {
				if (inTransferRange(loc)) {
//					debug_print("in transfer range hungry");
					if (--state.hungryMessageDelay <= 0) {
						rc.broadcast(new HungerMessage(rc).finalSerialize());
						state.hungryMessageDelay = 10;
						rc.yield();
					}
					state.hungry_FindArchon = false;
					return;
				} else if (!state.hungry_FindArchon) {
					rc.setIndicatorString(0, "onHungry - fp");
					state.hungry_FindArchon = true;

					stupidWalkGoTo(null, CollisionPolicy.GoRound);
					state.hungry_FindArchon = false; //?????!
				} else {
					return;
				}
			} else {
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
		handleIntsDepth++;
		if (handleIntsDepth >= 3) {
			throw new ArithmeticException();
		}
		rc.setIndicatorString(0, "handleInts");
		rc.setIndicatorString(1, policy.hungerPolicy.toString());

		if (!state.hungry_FindArchon && isHungry()) {
//			debug_print("isHungry");
			onHungry();
			handleIntsDepth--;
			return;
		}

		List<RobotInfo> enemies = getEnemyGroundUnits();
		yieldAtt();
		for (RobotInfo enemy : enemies) {
			if (rc.canAttackSquare(enemy.location)) {
				if (enemy.type.isAirborne()) {
					rc.attackAir(enemy.location);
				} else {
					rc.attackGround(enemy.location);
				}
				break;
			}
		}

		if (Clock.getBytecodeNum() <= ByteCodeConstants.BC75P) {
			healSomeGroundUnits();
		}

		processMessages();
		handleIntsDepth--;

	}

	@Override
	protected boolean checkRecipient(Recipient recipient) throws GameActionException
	{
		return (recipient.toWhom.flag & Recipient.RecipientType.Soldiers.flag) == Recipient.RecipientType.Soldiers.flag; //TODO medics
	}

	public ExecutionResult attackMove(MapLocation where) throws GameActionException
	{
		policy.enemySpottedPolicy = EnemySpottedPolicy.FireAtWill;

		for (int i = 0; i < 20; i++) {
			stupidWalkStep(where);
			handleInts();
		}

		return ExecutionResult.OK;
	}
}
