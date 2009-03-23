package batman.messaging;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

/**
 *
 * @author senu
 */
public class HungerMessage implements IMessage
{
	int rl;
	int howMuch;
	MapLocation where;

	public HungerMessage(RobotController who)
	{
		rl = (who.getRobot().getRobotLevel() == RobotLevel.IN_AIR) ? 0 : 1;
		howMuch = (int) Math.round(who.getMaxEnergonLevel() - who.getEventualEnergonLevel());
		where = who.getLocation();
	}

	public Message serialize()
	{
		Message m = new Message();

		m.ints = new int[]{Messages.MSG_HUNGRY, rl, howMuch};
		m.locations = new MapLocation[]{where};

		return m;
	}

	public void deserialize(Message m)
	{
		rl = m.ints[1];
		howMuch = m.ints[2];
		where = m.locations[0];
	}
}
