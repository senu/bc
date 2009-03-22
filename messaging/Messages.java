/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.messaging;

import battlecode.common.*;

/**
 *
 * @author pw248348
 */
public class Messages
{
	public static final int MSG_HUNGRY = 1;
	public static final int MSG_FIND_BLOCK = 2;
	public static final int MSG_PING = 3;

	public static Message hungryMessage(RobotController who)
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
		m.ints = new int[]{MSG_HUNGRY, rli, (int) Math.round(who.getMaxEnergonLevel() - who.getEventualEnergonLevel())};
		m.locations = new MapLocation[]{who.getLocation()};

		return m;
	}

	public static Message newSimpleMessage(int type)
	{
		Message m = new Message();
		m.ints = new int[]{type};
		return m;
	}

	public static Message newRequestBlockMessage(MapLocation loc, int howFar)
	{
		Message m = new Message();
		m.ints = new int[]{MSG_FIND_BLOCK, howFar};
		m.locations = new MapLocation[]{loc};
		return m;

	}
}