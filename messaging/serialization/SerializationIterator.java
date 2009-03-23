package batman.messaging.serialization;

import battlecode.common.MapLocation;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public class SerializationIterator
{
	public int ii;
	public int li;
	public int si;
	public Message msg;

	public SerializationIterator(Message msg, int ii, int li, int si)
	{
		this.ii = ii;
		this.li = li;
		this.si = si;
		this.msg = msg;
	}

	public SerializationIterator(Message msg)
	{
		ii = 0;
		li = 0;
		si = 0;
		this.msg = msg;
	}

	public int getInt()
	{
		return msg.ints[ii++];
	}

	public MapLocation getLoc()
	{
		return msg.locations[li++];
	}

	public String getString()
	{
		return msg.strings[si++];
	}
}
