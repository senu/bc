package batman.utils;

/**
 *
 * @author senu
 */
public class Pair<T1, T2>
{
	public Pair(T1 x, T2 y)
	{
		this.x = x;
		this.y = y;
	}
	public T1 x;
	public T2 y;

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Pair) || obj == null) {
			return false;
		}

		Pair pair = (Pair) obj;

		return (null == x ? null == pair.x : x.equals(pair.x)) && (null == y ? null == pair.y : y.equals(pair.y));

	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 19 * hash + (this.x != null ? this.x.hashCode() : 0);
		hash = 19 * hash + (this.y != null ? this.y.hashCode() : 0);
		return hash;
	}
}