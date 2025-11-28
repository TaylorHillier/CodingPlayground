package ca.bcit.comp2522.termproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Represents the core logic for a numeric grid-based game.
 * This class manages the board state, random number generation, valid move checking,
 * statistics tracking (wins, losses, placements), and game lifecycle events.
 * Concrete subclasses must provide UI-specific behavior for state changes and messages.
 *
 * @author Taylor
 * @version 1.0
 */
public abstract class NumberGame implements NumberGridGame
{
    // -------------------- Game Configuration Constants --------------------

    private static final int BOARD_SIZE_CELLS = 20;

    private static final int EMPTY_CELL_VALUE = 0;

    private static final int RANDOM_NUMBER_MIN_INCLUSIVE = 1;

    private static final int RANDOM_NUMBER_MAX_INCLUSIVE = 1000;

    private static final int RANDOM_NUMBER_UPPER_BOUND_EXCLUSIVE =
        RANDOM_NUMBER_MAX_INCLUSIVE;

    private static final int LEFT_BOUND_SENTINEL_VALUE = -1;

    private static final int RIGHT_BOUND_SENTINEL_VALUE =
        RANDOM_NUMBER_MAX_INCLUSIVE + 1;

    private static final int EMPTY_INT_ARRAY_LENGTH_ELEMENTS = 0;

    private static final int DEFAULT_MIN_VALID_CELL_INDEX = 0;

    private static final int LAST_CELL_INDEX_OFFSET = 1;

    private static final int NO_GAMES_PLAYED_COUNT = 0;

    private static final double NO_AVERAGE_PLACEMENTS_PER_GAME = 0.0;

    // -------------------- Instance Fields --------------------

    private int[]   board;
    private int     numberOfPlacements;
    private Random  randomNumberGenerator;
    private int     currentNumber;
    private boolean gameOver;
    private boolean gameLost;
    private int     totalGamesLost;
    private int     totalGamesWon;
    private int     totalGamesPlayed;
    private int     totalSuccessfulPlacements;
    private int[]   validCells;

    /**
     * Starts a new game by resetting the board, statistics related to the current
     * game session, and picking a new random number. Also triggers a state change
     * callback so subclasses can update their UI or other observers.
     */
    @Override
    public void startNewGame()
    {
        numberOfPlacements    = 0;
        board                 = new int[BOARD_SIZE_CELLS];
        randomNumberGenerator = new Random();
        currentNumber         = getRandomNumber();

        Arrays.fill(board, EMPTY_CELL_VALUE);

        validCells = new int[EMPTY_INT_ARRAY_LENGTH_ELEMENTS];

        gameOver = false;
        gameLost = false;

        onStateChanged();
    }

    /**
     * Gets the current board state.
     *
     * @return an array representing the current board
     */
    public int[] getBoard()
    {
        return board;
    }

    /**
     * Indicates whether the game has ended.
     *
     * @return {@code true} if the game is over, {@code false} otherwise
     */
    public boolean isGameOver()
    {
        return gameOver;
    }

    /**
     * Indicates whether the game was lost.
     *
     * @return {@code true} if the game ended in a loss, {@code false} otherwise
     */
    public boolean isGameLost()
    {
        return gameLost;
    }

    /**
     * Gets a defensive copy of the currently valid cells for placing the next number.
     *
     * @return an array of valid cell indices; never {@code null}
     */
    public int[] getValidCells()
    {
        if (validCells == null)
        {
            return new int[EMPTY_INT_ARRAY_LENGTH_ELEMENTS];
        }
        else
        {
            return validCells.clone();
        }
    }

    /**
     * Sets the cells that are valid for placing the current number this turn.
     *
     * @param newCells array of valid cell indices
     */
    public void setValidCells(final int[] newCells)
    {
        validCells = newCells;
    }

    /**
     * Gets the number that should be placed on the board next.
     *
     * @return the current number to place
     */
    public int getCurrentNumber()
    {
        return currentNumber;
    }

    /**
     * Generates a new random number in the configured range.
     *
     * @return a random number between {@link #RANDOM_NUMBER_MIN_INCLUSIVE}
     * and {@link #RANDOM_NUMBER_MAX_INCLUSIVE}, inclusive
     */
    public int getRandomNumber()
    {
        return randomNumberGenerator.nextInt(RANDOM_NUMBER_UPPER_BOUND_EXCLUSIVE)
               + RANDOM_NUMBER_MIN_INCLUSIVE;
    }

    /**
     * Gets the total number of games played so far.
     *
     * @return total games played
     */
    public int getTotalGamesPlayed()
    {
        return totalGamesPlayed;
    }

    /**
     * Gets the total number of games lost so far.
     *
     * @return total games lost
     */
    public int getTotalGamesLost()
    {
        return totalGamesLost;
    }

    /**
     * Gets the total number of games won so far.
     *
     * @return total games won
     */
    public int getTotalGamesWon()
    {
        return totalGamesWon;
    }

    /**
     * Gets the total number of successful placements made across all games.
     *
     * @return total successful placements
     */
    public int getTotalSuccessfulPlacements()
    {
        return totalSuccessfulPlacements;
    }

    /**
     * Gets the average number of placements per game.
     *
     * @return average placements per game, or {@code 0.0} if no games have been played
     */
    public double getAveragePlacementsPerGame()
    {
        if (totalGamesPlayed == NO_GAMES_PLAYED_COUNT)
        {
            return NO_AVERAGE_PLACEMENTS_PER_GAME;
        }

        return (double) totalSuccessfulPlacements / totalGamesPlayed;
    }

    /**
     * Calculates the valid cell indices for placing the provided number on the current board.
     * A cell is considered valid if:
     * <ul>
     *     <li>It is empty.</li>
     *     <li>The number is between the closest non-empty value on the left and the closest non-empty
     *         value on the right, using sentinel values when no such number exists.</li>
     * </ul>
     *
     * @param currentNumberValue the number to place this turn
     * @return an array of valid cell indices for the provided number
     */
    public int[] getValidSpotsForTurn(final int currentNumberValue)
    {
        final int[] currentBoard;
        final List<Integer> validSpots;

        validSpots   = new ArrayList<>();
        currentBoard = getBoard();

        for (int currentCellIndex = 0; currentCellIndex < currentBoard.length; currentCellIndex++)
        {
            if (currentBoard[currentCellIndex] != EMPTY_CELL_VALUE)
            {
                continue;
            }

            int leftValue;
            leftValue = LEFT_BOUND_SENTINEL_VALUE;

            for (int leftScanIndex = currentCellIndex - 1; leftScanIndex >= 0; leftScanIndex--)
            {
                if (currentBoard[leftScanIndex] != EMPTY_CELL_VALUE)
                {
                    leftValue = currentBoard[leftScanIndex];
                    break;
                }
            }

            int rightValue;
            rightValue = RIGHT_BOUND_SENTINEL_VALUE;

            for (int rightScanIndex = currentCellIndex + 1; rightScanIndex < currentBoard.length;
                 rightScanIndex++)
            {
                if (currentBoard[rightScanIndex] != EMPTY_CELL_VALUE)
                {
                    rightValue = currentBoard[rightScanIndex];
                    break;
                }
            }

            if (leftValue <= currentNumberValue && currentNumberValue <= rightValue)
            {
                validSpots.add(currentCellIndex);
            }
        }

        return validSpots.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Checks whether placing a number in the specified cell index is valid for this turn.
     * This method also handles the case where no valid cells remain (triggering a loss).
     *
     * @param cellIndex index of the cell the player attempted to use
     * @return {@code true} if the move is valid, {@code false} otherwise
     */
    public boolean checkValidMove(final int cellIndex)
    {
        if (validCells == null || validCells.length == EMPTY_INT_ARRAY_LENGTH_ELEMENTS)
        {
            gameOver = true;
            gameLost = true;

            finishGame(true);

            return false;
        }

        final int minimumValidIndex;
        minimumValidIndex = Arrays.stream(validCells)
                                  .min()
                                  .orElse(DEFAULT_MIN_VALID_CELL_INDEX);

        final int maximumValidIndex;
        maximumValidIndex = Arrays.stream(validCells)
                                  .max()
                                  .orElse(board.length - LAST_CELL_INDEX_OFFSET);

        if (cellIndex < minimumValidIndex)
        {
            showGuidanceMessage("You cannot place this number before the lowest number.\n");
            return false;
        }

        if (cellIndex > maximumValidIndex)
        {
            showGuidanceMessage("You cannot place this number after the highest number.\n");
            return false;
        }

        final boolean isValidCell;
        isValidCell = Arrays.stream(validCells).anyMatch(validIndex -> validIndex == cellIndex);

        if (!isValidCell)
        {
            showGuidanceMessage("You must place the number in a valid spot.\n");
            return false;
        }

        return true;
    }

    /**
     * Handles a cell click by validating the move, updating the board and statistics,
     * and advancing the game state or ending the game if the board is full.
     *
     * @param cellIndex the index of the cell that was clicked
     */
    public void handleCellClick(final int cellIndex)
    {
        if (isGameOver())
        {
            return;
        }

        final boolean isMoveValid;
        isMoveValid = checkValidMove(cellIndex);

        if (!isMoveValid)
        {
            onStateChanged();
            return;
        }

        board[cellIndex] = currentNumber;

        numberOfPlacements++;
        totalSuccessfulPlacements++;

        if (numberOfPlacements == board.length)
        {
            gameLost = false;
            finishGame(false);
        }
        else
        {
            currentNumber = getRandomNumber();
        }

        onStateChanged();
    }

    /**
     * Finishes the game, updates summary statistics, and calls
     * {@link #endCurrentGame(boolean, int, int, int, double)} with the resulting values.
     *
     * @param isLoss {@code true} if the game has been lost, {@code false} if it has been won
     */
    public void finishGame(final boolean isLoss)
    {
        gameOver = true;
        gameLost = isLoss;

        if (gameLost)
        {
            totalGamesLost++;
        }
        else
        {
            totalGamesWon++;
        }

        totalGamesPlayed++;

        final double averagePlacementsPerGame;
        averagePlacementsPerGame =
            (double) totalSuccessfulPlacements / totalGamesPlayed;

        endCurrentGame(isLoss,
                       numberOfPlacements,
                       totalGamesLost,
                       totalGamesPlayed,
                       averagePlacementsPerGame);
    }

    /**
     * Called whenever the underlying game state changes.
     * Subclasses must implement this to react (for example, by updating a UI).
     */
    protected abstract void onStateChanged();

    /**
     * Called when the current game has ended to allow subclasses to react to the final state.
     * This may include printing results, updating UI components, or logging.
     *
     * @param isLoss                   {@code true} if the game ended in a loss; {@code false} if it was a win
     * @param numberOfPlacements       number of placements made in this game
     * @param totalGamesLost           total games lost across all sessions
     * @param totalGamesPlayed         total games played across all sessions
     * @param averagePlacementsPerGame average placements per game across all sessions
     */
    public abstract void endCurrentGame(final boolean isLoss,
                                        final int numberOfPlacements,
                                        final int totalGamesLost,
                                        final int totalGamesPlayed,
                                        final double averagePlacementsPerGame);

    /**
     * Requests that a guidance or hint message be shown to the player.
     * Subclasses decide how this is displayed (e.g., dialog, console, label).
     *
     * @param message the hint text to show
     */
    public abstract void showGuidanceMessage(final String message);

    /**
     * Requests that a game-over message be shown to the player.
     * Subclasses decide how this is displayed and how any follow-up actions are handled.
     */
    public abstract void showGameOverMessage();
}
