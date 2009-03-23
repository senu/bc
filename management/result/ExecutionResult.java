package batman.management.result;

/**
 *
 * @author senu
 */
public enum ExecutionResult
{
	OK(1),
	Failed(2),
	Interrupted(4 | Failed.flag);

	private ExecutionResult(int flag)
	{
		this.flag = flag;
	}

	public int flag;
}
