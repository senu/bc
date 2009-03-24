package batman.messaging.message;

import batman.messaging.Recipient;
import batman.messaging.serialization.ISerializable;
import battlecode.common.Message;

/**
 *
 * @author senu
 */
public interface IMessage extends ISerializable
{
	/** Runda w której zostala wyslana */
	public int getRound();

	public int getPriority();

	public int getMessageType();

	public Recipient getRecipient();

	public Message finalSerialize();

	public void finalDeserialize(Message msg);
}
