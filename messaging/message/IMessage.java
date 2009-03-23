package batman.messaging.message;

import battlecode.common.Message;

/**
 *
 * @author senu
 */
public interface IMessage
{
	/** Runda w której zostala wyslana */
	public int getRound();

	public int getPriority();

	public Message serialize();

	public void deserialize(Message msg);
}
