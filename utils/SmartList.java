package batman.utils;

import batman.messaging.serialization.ISerializable;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import java.util.ArrayList;

/**
 *
 * @author senu
 */
public class SmartList<T> extends ArrayList<T> implements ISerializable
{

	public void deserialize(SerializationIterator it)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void serialize(MutableMessage m)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
