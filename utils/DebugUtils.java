package batman.utils;

/**
 *
 * @author senu
 */
public class DebugUtils
{
	public static final void debug_print(String format, Object... args)
	{
		System.out.printf(format + "\n", args);
	}
}
