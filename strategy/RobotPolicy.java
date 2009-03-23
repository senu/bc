/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.strategy;

import batman.messaging.serialization.ISerializable;
import batman.messaging.serialization.MutableMessage;
import batman.messaging.serialization.SerializationIterator;
import batman.strategy.policy.AttackTargetingPolicy;
import batman.strategy.policy.EnemySpottedPolicy;
import batman.strategy.policy.HungerPolicy;
import batman.strategy.policy.MapRefreshPolicy;

/**
 *
 * @author pw248348
 */
public class RobotPolicy implements ISerializable
{
	public HungerPolicy hungerPolicy;
	public EnemySpottedPolicy enemySpottedPolicy;
	public MapRefreshPolicy mapRefreshPolicy;
	public AttackTargetingPolicy attackTargetingPolicy;

	public RobotPolicy()
	{
		hungerPolicy = HungerPolicy.HungryAt35;
		enemySpottedPolicy = EnemySpottedPolicy.AttackIfWeaker;
		mapRefreshPolicy = MapRefreshPolicy.FullScanModerately;
		attackTargetingPolicy = AttackTargetingPolicy.AttackCloser;
	}

	public void deserialize(SerializationIterator it)
	{
		hungerPolicy = HungerPolicy.values()[it.getInt()];
		enemySpottedPolicy = EnemySpottedPolicy.values()[it.getInt()];
		mapRefreshPolicy = MapRefreshPolicy.values()[it.getInt()];
		attackTargetingPolicy = attackTargetingPolicy.values()[it.getInt()];
	}

	public void serialize(MutableMessage m)
	{
		m.ints.add(hungerPolicy.ordinal());
		m.ints.add(enemySpottedPolicy.ordinal());
		m.ints.add(mapRefreshPolicy.ordinal());
		m.ints.add(attackTargetingPolicy.ordinal());
	}
}
