package batman.unit;

import battlecode.common.RobotController;
import java.util.Random;

/**
 *
 * @author senu
 */
public class Unit
{
	protected RobotController rc;
	protected Random rand = new Random();

	public Unit(RobotController rc)
	{
		this.rc = rc;
		this.rand.setSeed(rc.getRobot().getID());
	}

	/** Czekaj, az ruch sie nie skonczy. */
	protected final void yieldMv()
	{
		while (rc.isMovementActive()) {
			rc.yield();
		}
	}

	public void beYourself()
	{
	}
}
