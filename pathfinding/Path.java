package batman.pathfinding;

import battlecode.common.MapLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Wyliczona ścieżka dla robota.
 * @author senu
 */
public class Path
{
	private List<MapLocation> path;
	private int currentIdx;

	public Path(List<MapLocation> path)
	{
		this.path = path;
		currentIdx = 0;
	}

	public Path()
	{
		this.path = new ArrayList<MapLocation>();
		currentIdx = 0;
	}

	/** Przesun sie na sciezce */
	public void next()
	{
		currentIdx++;
	}

	public boolean hasNext()
	{
		return currentIdx == path.size() - 1;
	}

	public MapLocation getNext()
	{
		return path.get(currentIdx + 1);
	}

	public MapLocation getCur()
	{
		return path.get(currentIdx);
	}

	public MapLocation getPrev()
	{
		return path.get(currentIdx - 1);
	}

	public int getCurrentIdx()
	{
		return currentIdx;
	}

	public void setCurrentIdx(int currentIdx)
	{
		this.currentIdx = currentIdx;
	}

	public List<MapLocation> getPath()
	{
		return path;
	}

	public void setPath(List<MapLocation> path)
	{
		this.path = path;
	}
}
