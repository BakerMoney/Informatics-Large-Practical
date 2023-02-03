package uk.ac.ed.inf;

/**
 * A class for helping with throwing exceptions
 */
public class InvalidPizzaCombinationException  extends Exception{
    /**
     * A method for handling an invalid pizza combination exception
     *
     * @param message Message to be shown if exception occurs
     */
    public InvalidPizzaCombinationException (String message) {
        super(message);
    }
}
