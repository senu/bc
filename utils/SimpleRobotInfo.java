package batman.utils;

import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 *
 * @author senu
 */
public class SimpleRobotInfo
{
	public SimpleRobotInfo(RobotInfo ri)
	{
		this.type = ri.type;
		this.team = ri.team;
	}
	public RobotType type;
	public Team team;
}
