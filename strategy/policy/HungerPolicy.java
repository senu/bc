/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.strategy.policy;

/**
 *
 * @author pw248348
 */
public enum HungerPolicy {

	DieStarving(-1.0),
	HungryAt35(0.35),
	HungryAt60(0.60);

	HungerPolicy(double level) {
		this.hungerLevel = level;
	}
	public double hungerLevel;
}
