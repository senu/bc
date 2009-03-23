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
	int rl;
	int howMuch;
	MapLocation where;

	@Override
	public final int getMessageType()
	{
		return 1;
	}

	public HungerMessage(RobotController who)
	{
		rl = (who.getRobot().getRobotLevel() == RobotLevel.IN_AIR) ? 0 : 1;
		howMuch = (int) Math.round(who.getMaxEnergonLevel() - who.getEventualEnergonLevel());
		where = who.getLocation();
	}

	public Message serialize()
	{
		MutableMessage m = serializeStart();

		m.ints.add(rl);
		m.ints.add(howMuch);

		m.locations.add(where);

		return m.serialize();
	}

	public void deserialize(Message m)
	{
		SerializationIterator it = deserializeStart(m);
		rl = it.getInt();
		howMuch = it.getInt();
		where = it.getLoc();
	}
}
