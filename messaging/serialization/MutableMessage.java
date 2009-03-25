package batman.messaging.serialization;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author senu
 */
public class MutableMessage
{
	public MutableMessage()
	{
		locations = new ArrayList<MapLocation>();
		ints = new ArrayList<Integer>();
		strings = new ArrayList<String>();
	}
	public ArrayList<MapLocation> locations;
	public ArrayList<Integer> ints;
	public ArrayList<String> strings;

	public Message serialize()
	{
		Message m = new Message();

		m.ints = new int[ints.size()];
		int pos = 0;
		for (Integer i : ints) {
			m.ints[pos++] = i;
		}

		m.locations = locations.toArray(new MapLocation[0]);
		m.strings = strings.toArray(new String[0]);

		return m;


	}
}
