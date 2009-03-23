package batman.unit;

import batman.constants.ByteCodeConstants;
import batman.messaging.Messages;
import batman.constants.StrategyConstants;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 *
 * @author senu
 */
public class Soldier extends Unit
{
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

	private final void handleInts() throws GameActionException
	{
		if (health() <= StrategyConstants.SOLDIER_HUNGER_LEVEL) {

			for (;;) {
				refreshLocation();

				yieldIf(ByteCodeConstants.Medium);
				MapLocation loc = nearestArchon();

				if (loc != null) {
					if (inTransferRange(loc)) {
						rc.broadcast(Messages.hungryMessage(rc));
						while (health() <= StrategyConstants.SOLDIER_HUNGER_LEVEL) {
							rc.yield();
						}
						return;
					} else {
//						debug_print("hungry");
//							goTo(loc);
					}
				}
			}
		}
	}

}
