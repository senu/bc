package batman.pathfinding;

import battlecode.common.MapLocation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Mapa do A*, dla naziemnych.
 * @author senu
 */
public class GameMap
{
	private HashMap<MapLocation, MapTile> map = new HashMap<MapLocation, MapTile>();
	public int mx = Integer.MAX_VALUE,  my = Integer.MAX_VALUE; //shift
	public int Mx = Integer.MIN_VALUE,  My = Integer.MIN_VALUE;

	public final void setTile(MapLocation loc, MapTile state)
	{
		if (loc.getX() < mx) {
			mx = loc.getX();
		}
		if (loc.getX() > Mx) {
			Mx = loc.getX();
		}
		if (loc.getY() < my) {
			my = loc.getY();
		}
		if (loc.getY() > My) {
			My = loc.getY();
		}

		map.put(loc, state);
	}

	public final MapTile getTile(MapLocation loc)
	{
		return map.get(loc);
	}

	public final boolean hasLoc(MapLocation loc)
	{
		return map.containsKey(loc);
	}

	public void debug_print()
	{
		debug_print(Path.emptyPath);
	}

	public void debug_print(Path path)
	{
//		MapLocation min =
		Set<MapLocation> ks = new HashSet<MapLocation>(map.keySet());

		for (MapLocation loc : path.getPath()) {
			ks.add(loc);
		}
		for (MapLocation loc : ks) { //shift
			if (loc.getX() < mx) {
				mx = loc.getX();
			}
			if (loc.getX() > Mx) {
				Mx = loc.getX();
			}
			if (loc.getY() < my) {
				my = loc.getY();
			}
			if (loc.getY() > My) {
				My = loc.getY();
			}
		}

		if (mx > Mx) {
			System.out.println("empty map");
			return;
		} else {
			System.out.println(String.format("%d | %d %d %d %d", map.size(), mx, my, Mx, My));
		}

		char[][] repr = new char[My - my + 1][Mx - mx + 1];
		for (Entry<MapLocation, MapTile> e : map.entrySet()) {
			int x = e.getKey().getX() - mx;
			int y = e.getKey().getY() - my;

			char c = 0;
			if (e.getValue().state == MapTile.LocState.Ground) {
				c = Integer.toString(e.getValue().blockCount).charAt(0);
				if (c == '0') {
					c = ' ';
				}
			} else if (e.getValue().state == MapTile.LocState.Air) {
				c = 'A';
			} else if (e.getValue().state == MapTile.LocState.Bad) {
				c = 'B';
			} else if (e.getValue().state == MapTile.LocState.Unknown) {
				c = '_';
			}

			repr[y][x] = c;
		}

		for (MapLocation loc : path.getPath()) {
			if (repr[loc.getY() - my][loc.getX() - mx] == 'A') {
				repr[loc.getY() - my][loc.getX() - mx] = 'p';
			} else {
				repr[loc.getY() - my][loc.getX() - mx] = 'P';
			}
		}


		System.out.println();
		for (int i = 0; i < repr.length; i++) {
			char[] cs = repr[i];
			for (int j = 0; j < cs.length; j++) {
				char c = cs[j];
				if (c == 0) {
					c = '.';
				}

				System.out.print(c);
			}
			System.out.println();
		}
	}

	public MapLocation luCorner()
	{
		int mx = Integer.MAX_VALUE, my = Integer.MAX_VALUE;
		for (MapLocation loc : map.keySet()) { //shift
			if (loc.getX() < mx) {
				mx = loc.getX();
			}
			if (loc.getY() < my) {
				my = loc.getY();
			}
		}

		return new MapLocation(mx, my);
	}

	public Set<Entry<MapLocation, MapTile>> getTileSet()
	{
		return map.entrySet();
	}
}
