/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.strategy.policy;

/**
 *
 * @author pw248348
 */
public enum HungerPolicy
{
	DieStarving(-1.0),
	HungryAt10(0.10),
	HungryAt35(0.35),
	HungryAt42(0.42),
	HungryAt50(0.50),
	HungryAt60(0.60);
	public double hungerLevel;

	HungerPolicy(double level)
	{
		this.hungerLevel = level;
	}
}
