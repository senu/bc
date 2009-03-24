/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.management.order;

import batman.messaging.serialization.OrderDispatcher;
import batman.messaging.serialization.SerializationIterator;
import batman.utils.SmartList;

/**
 *
 * @author pw248348
 */
public class OrderList extends SmartList<Order>
{

	public OrderList(int arg0)
	{
		super(arg0);
	}

	public OrderList()
	{
	}


	public void deserialize(SerializationIterator it)
	{
		int len = prepareDeserialize(it);

		for (int i = 0; i < len; i++) {
			Order order = OrderDispatcher.getOrderByRepresentation(it.getString());
			order.deserialize(it);
			add(order);
		}

	}
}
