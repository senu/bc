package batman.management.order;

import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;

/**
 * Rozkaz bez pól.
 * @author senu
 */
public abstract class SimpleOrder implements Order
{
	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}

	public void deserialize(SerializationIterator it)
	{
	}

	public void serialize(MutableMessage m)
	{
		m.strings.add(getOrderName());
	}
}
