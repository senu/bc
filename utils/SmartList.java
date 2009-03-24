package batman.utils;

import batman.messaging.serialization.ISerializable;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import java.util.ArrayList;

/**
 * Czesciowo serializowalna lista.
 * @author senu
 */
public abstract class SmartList<T extends ISerializable>
		extends ArrayList<T>
		implements ISerializable
{
	public SmartList()
	{
	}

	public SmartList(int arg0)
	{
		super(arg0);
	}

	public int prepareDeserialize(SerializationIterator it)
	{
		this.clear();

		int len = it.getInt();
		this.ensureCapacity(len);

		return len;
	}

	public void serialize(MutableMessage m)
	{
		m.ints.add(size());
		for (ISerializable obj : this) {
			obj.serialize(m);
		}
	}
}
