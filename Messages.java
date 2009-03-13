/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman;

import battlecode.common.*;

/**
 *
 * @author pw248348
 */
class Messages
{
	public static final int MSG_HUNGRY = 1;
	public static final int MSG_FIND_BLOCK = 2;
	public static final int MSG_PING = 3;

	public static Message newMessage(int type, RobotController who)
	{
		Message m = new Message();
		RobotLevel rl = who.getRobot().getRobotLevel();
		int rli = 0; //TODO

		for (RobotLevel x : RobotLevel.values()) {
			if (x == rl) {
				break;
			}
			rli++;
		}
//(int)Math.round(who.getMaxEnergonLevel() - who.getEventualEnergonLevel()
		m.ints = new int[]{type, rli, 10};
		m.locations = new MapLocation[]{who.getLocation()};

		return m;
	}

	public static Message newSimpleMessage(int type)
	{
		Message m = new Message();
		m.ints = new int[]{type};
		return m;
	}

	public static Message newRequestBlockMessage(MapLocation loc)
	{
		Message m = new Message();
		m.ints = new int[]{MSG_FIND_BLOCK};
		m.locations = new MapLocation[]{loc};
		return m;

	}
}