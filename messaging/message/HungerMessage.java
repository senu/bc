package batman.messaging.message;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

/**
 *
 * @author senu
 */
public class HungerMessage extends MessageImpl
{
	public RobotLevel rl;
	public int howMuch;
	public MapLocation where;

	@Override
	public final int getMessageType()
	{
		return 1;
	}

	public HungerMessage()
	{
	}

	public HungerMessage(RobotController who)
	{
		rl = who.getRobot().getRobotLevel();
		howMuch = (int) Math.round(who.getMaxEnergonLevel() - who.getEventualEnergonLevel());
		where = who.getLocation();
	}

	public void serialize(MutableMessage m)
	{
		m.ints.add(rl.ordinal());
		m.ints.add(howMuch);

		m.locations.add(where);
	}

	public void deserialize(SerializationIterator it)
	{
		rl = RobotLevel.values()[it.getInt()];
		howMuch = it.getInt();
		where = it.getLoc();
	}
}
