package batman.messaging;

import battlecode.common.Message;

/**
 *
 * @author senu
 */
public interface IMessage
{
	public Message serialize();

	public void deserialize(Message msg);
}
