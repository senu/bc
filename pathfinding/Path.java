package batman.pathfinding;

import battlecode.common.MapLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Wyliczona ścieżka dla robota.
 *
 * Dla pustych ścieżek należy używać emptyPath.
 *
 * @author senu
 */
public class Path
{
	private List<MapLocation> path;
	private int currentIdx;
	public static Path emptyPath = new Path();

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

	public void debug_print(GameMap map)
	{
		MapLocation lu = map.luCorner();

		for (MapLocation loc : path) {
			loc = new MapLocation(loc.getX() - lu.getX(), loc.getY() - lu.getY());
			System.out.print(String.format("[%d,%d] -> ", loc.getX(), loc.getY()));
		}
		System.out.println();
	}
}
