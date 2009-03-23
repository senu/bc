package batman.messaging.serialization;

/**
 * Daje sie wyslać i odebrać poprzez rc.broadcast() i odczytać.
 * @author senu
 */
public interface ISerializable
{
	/** Ma wczytać swój stan z it. */
	public void deserialize(SerializationIterator it);

	public void serialize(MutableMessage m);
}
