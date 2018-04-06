package control;

/**
 * Error throw after the player has not performed a move
 *
 */
public class TurnNotFinishedException extends Exception {
	public TurnNotFinishedException(String string){
		super(string);
	}
}
