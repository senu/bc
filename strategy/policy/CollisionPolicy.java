package batman.strategy.policy;

/**
 * Jak się zachowuje robot, gdy ktos z jego druzyny stoi mu na drodze.
 * @author senu
 */
public enum CollisionPolicy
{
	GoRound,
	AlwaysWait, /** Czeka aż sie przesunie */
	WaitALitte
}
