package batman.utils;

import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import java.io.Serializable;

/**
 *
 * @author senu
 */
public class SimpleRobotInfo implements Serializable
{
	public static final long serialVersionUID = 1L;

	public SimpleRobotInfo(RobotInfo ri)
	{
		this.type = ri.type;
		this.team = ri.team;
	}
	public RobotType type;
	public Team team;
}
