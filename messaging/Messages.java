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
	public static final int MSG_MAP_TRANSFER_REQUEST = 4;
	public static final int MSG_MAP_TRANSFER_RESPONSE = 5;

	public static Message hungryMessage(RobotController who)
	{
		return new HungerMessage(who).serialize();
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