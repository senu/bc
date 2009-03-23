package batman;

import batman.unit.Archon;
import batman.unit.Soldier;
import batman.unit.Unit;
import batman.unit.Worker;
import battlecode.common.*;
import java.util.Random;

public class RobotPlayer implements Runnable
{
	private Random rand = new Random();
	private final RobotController rc;
	private RobotType robotType;

	public RobotPlayer(RobotController rc)
	{
		this.rc = rc;
		this.rand.setSeed(rc.getRobot().getID());
	}

	private void debug_print(String format, Object... args)
	{
		System.out.printf(rc.getRobot().getID() + " " + format + "\n", args);
	}

	private final void init()
	{
		robotType = rc.getRobotType();
	}

	public void run()
	{
		init();
		while (true) {
			try {

				Unit unit;
				if (robotType == RobotType.WORKER) {
					unit = new Worker(rc);
				} else if (robotType == RobotType.SOLDIER) {
					unit = new Soldier(rc);
				} else {
					unit = new Archon(rc);
				}

				unit.beYourself();

			} catch (Exception e) {
				debug_print("caught exception:");
				e.printStackTrace();
			}
		}
	}
}
