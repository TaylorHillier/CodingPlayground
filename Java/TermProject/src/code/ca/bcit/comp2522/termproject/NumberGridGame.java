package ca.bcit.comp2522.termproject;

/**
 * Defines the required operations for any grid-based number game.
 * Implementations must manage a numeric board, track whether the game
 * has ended, and handle user interactions with individual cells.
 * <p>
 * This interface represents the minimum contract needed by the UI layer
 * to display the game state and relay user actions back to the logic layer.
 * Any class implementing this interface must provide complete game behavior
 * such that the UI can fully drive a playable session.
 *
 * @author Taylor Hillier
 * @version 1.0
 */
public interface NumberGridGame
{
    /**
     * Resets all necessary fields and begins a completely new game session.
     * Implementations must ensure the board starts empty, the first number is
     * ready to place, and all game state flags return to their default values.
     */
    void startNewGame();

    /**
     * Handles a request to place the current number in the specified board index.
     * Implementations must validate the move, update any game statistics,
     * determine whether the game should end, and update the current number.
     *
     * @param cellIndex the board index that was clicked
     */
    void handleCellClick(final int cellIndex);

    /**
     * Indicates whether the game has ended (either by winning or losing).
     *
     * @return {@code true} if the game is over, otherwise {@code false}
     */
    boolean isGameOver();

    /**
     * Indicates whether the last completed game ended in a loss.
     *
     * @return {@code true} if the game was lost, otherwise {@code false}
     */
    boolean isGameLost();

    /**
     * Retrieves the current board state. Implementations should return the
     * live internal board or a defensive copy depending on design needs.
     *
     * @return an integer array representing the current board
     */
    int[] getBoard();

    /**
     * Retrieves the next number that must be placed by the player.
     *
     * @return the current number to place
     */
    int getCurrentNumber();
}
