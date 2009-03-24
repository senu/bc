package batman.messaging.serialization;

import batman.messaging.message.IMessage;

/**
 *
 * @author senu
 */
public class MessageDispatcher
{
	public static IMessage getMessageByRepresentation(String name)
	{

		//TODO

		try {
			return (IMessage) (Class.forName(name).newInstance());

		} catch (Exception e) {
			System.out.println("Message serialization error:");
			e.printStackTrace();
			return null;
		}


	}
}
