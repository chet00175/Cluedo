package control;

/**
 * Error throw after a player attemped to make another suggestion OR accusation
 *
 */
public class AlreadySuggestedException extends Exception {
	public AlreadySuggestedException(String string){
		super(string);
	}
}
