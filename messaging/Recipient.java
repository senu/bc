package batman.messaging;

import batman.messaging.serialization.ISerializable;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import java.security.AllPermission;

/**
 *
 * @author senu
 */
public class Recipient implements ISerializable
{
	public int groupOrId;
	public RecipientType toWhom;

	public Recipient(RecipientType toWhom)
	{
		this.toWhom = toWhom;
	}

	public Recipient()
	{
		this.toWhom = RecipientType.All;
	}

	public enum RecipientType
	{
		Soldiers(2),
		Medics(4),
		Archons(32),
		All(1 | Soldiers.flag | Medics.flag | Archons.flag),
		Group(8),
		Single(16);
		public int flag;

		private RecipientType(int flag)
		{
			this.flag = flag;
		}
	}

	public void deserialize(SerializationIterator it)
	{
		toWhom = RecipientType.values()[it.getInt()];
		groupOrId = it.getInt();
	}

	public void serialize(MutableMessage m)
	{
		m.ints.add(toWhom.ordinal());
		m.ints.add(groupOrId);
	}
}
