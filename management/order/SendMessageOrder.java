package batman.management.order;

import batman.management.executor.Executor;
import batman.management.result.ExecutionResult;
import batman.messaging.message.IMessage;
import batman.messaging.serialization.MessageDispatcher;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import battlecode.common.GameActionException;

/**
 * TODO nie dziala!
 * @author senu
 */
public class SendMessageOrder implements Order
{
	IMessage message;

	public SendMessageOrder(IMessage message)
	{
		this.message = message;
	}

	public SendMessageOrder()
	{
	}

	public ExecutionResult execute(Executor executor) throws GameActionException
	{
		return executor.executeSendMessageOrder(this);
	}

	public String getOrderName()
	{
		return this.getClass().getSimpleName();
	}

	public IMessage getMessage()
	{
		return message;
	}

	public void setMessage(IMessage message)
	{
		this.message = message;
	}

	public void deserialize(SerializationIterator it)
	{
		message = MessageDispatcher.getMessageByRepresentation(it.getString());
		message.deserialize(it);
	}

	public void serialize(MutableMessage m)
	{
		m.strings.add(getOrderName());
		message.serialize(m);
	}
}
