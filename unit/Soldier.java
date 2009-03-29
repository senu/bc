package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.constants.StrategyConstants;
import batman.management.executor.SoldierExecutor;
import batman.management.result.ExecutionResult;
import batman.messaging.Recipient;
import batman.messaging.message.IMessage;
import batman.messaging.message.MapTransferResponseMessage;
import batman.messaging.message.OrderMessage;
import batman.strategy.policy.CollisionPolicy;
import batman.strategy.policy.EnemySpottedPolicy;
import batman.strategy.policy.HungerPolicy;
import batman.unit.state.SoldierState;
import batman.unit.state.UnitState;
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
	protected UnitState getState()
	{
		return state;
	}

	@Override
	public void beYourself() throws GameActionException
	{
		for (;;) {
			handleInts();
			rc.yield();
//			updateMap();
			//rc.yield();
			if (!state.orderQueue.isEmpty()) {
//				debug_print("begin of execute");
				state.orderQueue.remove().execute(executor);
//				debug_print("end of execute");
			}
		}
	}

	protected void processMessages() throws GameActionException
	{
		for (IMessage inMsg : getMessages()) {
			if (inMsg instanceof OrderMessage) {
				OrderMessage msg = (OrderMessage) inMsg;
				if (state.orderQueue.size() > 3) {
					state.orderQueue.clear();
				}
				state.orderQueue.add(msg.order);
				//TODO_
		//		if (state.orderQueue.size() > 4) {
		//			debug_print("orderQueue: %s %d", state.orderQueue, state.orderQueue.size());
		//		}
			} else if (inMsg instanceof MapTransferResponseMessage) {
				handleMapTransfer((MapTransferResponseMessage) inMsg);
				debug_print("got map transfer");
				map.debug_print();
			}
		}
	}

	protected final void handleInts() throws GameActionException
	{
//		handleIntsDepth++;
//		rc.setIndicatorString(0, "handleInts");
//		rc.setIndicatorString(1, policy.hungerPolicy.toString());

		if (!state.hungry_FindArchon && isHungry()) {
//			debug_print("isHungry");
			onHungry();
//			handleIntsDepth--;
			return;
		}

		List<RobotInfo> enemies = getEnemies();
		yieldAtt();
		for (RobotInfo enemy : enemies) {
			if (rc.canAttackSquare(enemy.location)) {
				if (enemy.type.isAirborne()) {
					rc.attackAir(enemy.location);
				} else {
					rc.attackGround(enemy.location);
				}
				rc.yield();
				break;
			}
		}

		if (Clock.getBytecodeNum() <= ByteCodeConstants.BC75P) {
			healSomeGroundUnits();
		}

		processMessages();
//		handleIntsDepth--;

	}

	@Override
	protected boolean checkRecipient(Recipient recipient) throws GameActionException
	{
		return (recipient.toWhom.flag & Recipient.RecipientType.Soldiers.flag) == Recipient.RecipientType.Soldiers.flag; //TODO medics
	}

	public ExecutionResult attackMove(MapLocation where) throws GameActionException
	{
		policy.enemySpottedPolicy = EnemySpottedPolicy.FireAtWill;

		stupidWalkGoTo(where, CollisionPolicy.GoRound);

		return ExecutionResult.OK;
	}
}
