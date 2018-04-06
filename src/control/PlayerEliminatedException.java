package control;

/**
 * Error thrown when an eliminated player attempts to make a suggestion or accusation after being eliminated
 *
 */
public class PlayerEliminatedException extends Exception {
	public PlayerEliminatedException(String string){
		super(string);
	}
}
