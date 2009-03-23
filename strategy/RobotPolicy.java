/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.strategy;

import batman.strategy.policy.AttackTargetingPolicy;
import batman.strategy.policy.EnemySpottedPolicy;
import batman.strategy.policy.HungerPolicy;
import batman.strategy.policy.MapRefreshPolicy;

/**
 *
 * @author pw248348
 */
public class RobotPolicy
{
	public HungerPolicy hungerPolicy;
	public EnemySpottedPolicy enemySpottedPolicy;
	public MapRefreshPolicy mapRefreshPolicy;
	public AttackTargetingPolicy attackTargetingPolicy;
}
