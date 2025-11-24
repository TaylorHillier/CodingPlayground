package ca.bcit.comp2522.termproject;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Represents the JavaFX user interface for the Number Game.
 * This class creates and manages a separate window that displays the game board
 * and the current number, and delegates user interactions to the underlying game logic.
 *
 * @author Taylor Hillier
 * @version 1.0
 */
public final class NumberGameInterface
{
    // -------------------- UI / Layout Constants --------------------

    private static final int HBOX_SPACING_PIXELS        = 15;
    private static final int GRID_HORIZONTAL_GAP_PIXELS = 10;
    private static final int GRID_VERTICAL_GAP_PIXELS   = 10;
    private static final int VBOX_SPACING_PIXELS        = 10;

    private static final int CELL_BUTTON_MIN_WIDTH_PIXELS  = 60;
    private static final int CELL_BUTTON_MIN_HEIGHT_PIXELS = 60;

    private static final int SCENE_WIDTH_PIXELS  = 500;
    private static final int SCENE_HEIGHT_PIXELS = 450;

    // -------------------- Game / Board Constants --------------------

    private static final int NUMBER_GAME_TOTAL_CELLS     = 20;
    private static final int NUMBER_GAME_COLUMNS_PER_ROW = 5;

    private static final int    EMPTY_CELL_VALUE        = 0;
    private static final String EMPTY_CELL_DISPLAY_TEXT = "-";

    private static final String CURRENT_NUMBER_LABEL_TEXT   = "Next Number:";
    private static final String INITIAL_CURRENT_NUMBER_TEXT = "No Number Yet";
    private static final String GAME_OVER_LABEL_TEXT        = "Game Over";

    private static final String GAME_WINDOW_TITLE_TEXT = "Number Game";

    private static final String GAME_OVER_DIALOG_TITLE_TEXT       = "Game Over";
    private static final String GAME_OVER_DIALOG_WIN_TEXT         = "Game Over! You Win!";
    private static final String GAME_OVER_DIALOG_LOSS_PREFIX_TEXT =
        "Game Over! Impossible to place the next number: ";

    private static final String GAME_OVER_DIALOG_TRY_AGAIN_TEXT = "Try Again";
    private static final String GAME_OVER_DIALOG_QUIT_TEXT      = "Quit";

    // -------------------- Instance Fields --------------------

    private final Button[] cellButtons;
    private final Label    currentNumberDisplay;

    private final CountDownLatch gameFinishedLatch;

    /**
     * Creates a new {@code NumberGameInterface} that will count down the provided latch
     * when the game window is closed.
     *
     * @param gameFinishedLatch latch to count down when the game window closes; may be {@code null}
     */
    public NumberGameInterface(final CountDownLatch gameFinishedLatch)
    {
        this.gameFinishedLatch = gameFinishedLatch;

        cellButtons          = new Button[NUMBER_GAME_TOTAL_CELLS];
        currentNumberDisplay = new Label(INITIAL_CURRENT_NUMBER_TEXT);
    }

    /**
     * Opens the Number Game in a new window (stage), installs a close handler that
     * decrements the latch, builds the UI, and starts a new game.
     */
    public void openInNewStage()
    {
        final Stage gameStage = new Stage();

        gameStage.setOnCloseRequest(event ->
                                    {
                                        if (gameFinishedLatch != null)
                                        {
                                            gameFinishedLatch.countDown();
                                        }
                                    });

        buildUI(gameStage);
    }

    private void buildUI(final Stage primaryStage)
    {
        final HBox currentNumberContainer = new HBox(HBOX_SPACING_PIXELS);
        final Label currentNumberLabel = new Label(CURRENT_NUMBER_LABEL_TEXT);

        currentNumberDisplay.setText(INITIAL_CURRENT_NUMBER_TEXT);

        currentNumberContainer.getChildren().addAll(currentNumberLabel, currentNumberDisplay);

        final GridPane gameGrid = new GridPane();
        gameGrid.setHgap(GRID_HORIZONTAL_GAP_PIXELS);
        gameGrid.setVgap(GRID_VERTICAL_GAP_PIXELS);

        for (int currentCellIndex = 0; currentCellIndex < NUMBER_GAME_TOTAL_CELLS; currentCellIndex++)
        {
            final Button cellButton = new Button(EMPTY_CELL_DISPLAY_TEXT);
            cellButton.setMinSize(CELL_BUTTON_MIN_WIDTH_PIXELS, CELL_BUTTON_MIN_HEIGHT_PIXELS);

            final int capturedCellIndex = currentCellIndex;
            cellButton.setOnAction(actionEvent -> gameLogic.handleCellClick(capturedCellIndex));

            final int rowIndex = currentCellIndex / NUMBER_GAME_COLUMNS_PER_ROW;
            final int columnIndex = currentCellIndex % NUMBER_GAME_COLUMNS_PER_ROW;

            gameGrid.add(cellButton, columnIndex, rowIndex);
            cellButtons[currentCellIndex] = cellButton;
        }

        final VBox rootLayout = new VBox(VBOX_SPACING_PIXELS);
        rootLayout.getChildren().addAll(currentNumberContainer, gameGrid);

        final Scene scene = new Scene(rootLayout, SCENE_WIDTH_PIXELS, SCENE_HEIGHT_PIXELS);
        primaryStage.setScene(scene);
        primaryStage.setTitle(GAME_WINDOW_TITLE_TEXT);

        primaryStage.show();

        gameLogic.startNewGame();
    }

    private final NumberGame gameLogic = new NumberGame()
    {
        private int    statsTotalGamesPlayed;
        private int    statsTotalGamesLost;
        private int    statsTotalGamesWon;
        private int    statsTotalSuccessfulPlacements;
        private double statsAveragePlacementsPerGame;

        /**
         * Updates the UI in response to a change in the game state.
         * This includes re-rendering the board, updating valid cells,
         * checking for end-of-game conditions, and updating the current number label.
         */
        @Override
        protected void onStateChanged()
        {
            final int[] boardSnapshot = getBoard();

            reRenderGrid(boardSnapshot);

            final int[] validCellsForCurrentTurn = getValidSpotsForTurn(getCurrentNumber());
            setValidCells(validCellsForCurrentTurn);

            if (validCellsForCurrentTurn.length == 0 && !isGameOver())
            {
                finishGame(true);
            }

            if (isGameOver())
            {
                currentNumberDisplay.setText(GAME_OVER_LABEL_TEXT);
                showGameOverMessage();
            }
            else
            {
                currentNumberDisplay.setText(String.valueOf(getCurrentNumber()));
            }
        }

        /**
         * Ends the current game, updates statistics, and prints a summary to standard output.
         *
         * @param isLoss                   {@code true} if the game ended in a loss; {@code false} if it was a win
         * @param numberOfPlacements       number of placements in this game
         * @param totalGamesLost           total games lost so far
         * @param totalGamesPlayed         total games played so far
         * @param averagePlacementsPerGame average placements per game so far
         */
        @Override
        public void endCurrentGame(final boolean isLoss,
                                   final int numberOfPlacements,
                                   final int totalGamesLost,
                                   final int totalGamesPlayed,
                                   final double averagePlacementsPerGame)
        {
            statsTotalGamesPlayed          = getTotalGamesPlayed();
            statsTotalGamesLost            = getTotalGamesLost();
            statsTotalGamesWon             = getTotalGamesWon();
            statsTotalSuccessfulPlacements = getTotalSuccessfulPlacements();
            statsAveragePlacementsPerGame  = getAveragePlacementsPerGame();

            final String resultMessage = isLoss ? "You lost!" : "You won!";
            final String scoreSummary = buildScoreSummary();

            System.out.println(resultMessage);
            System.out.println(scoreSummary);
        }

        private String buildScoreSummary()
        {
            if (statsTotalGamesPlayed == 0)
            {
                return "No games played yet.";
            }

            final String gameWord = (statsTotalGamesPlayed == 1) ? "game" : "games";

            if (statsTotalGamesLost == statsTotalGamesPlayed)
            {
                return String.format(
                    "You lost %d out of %d %s, with %d successful placements, an average of %.2f per game",
                    statsTotalGamesLost,
                    statsTotalGamesPlayed,
                    gameWord,
                    statsTotalSuccessfulPlacements,
                    statsAveragePlacementsPerGame
                                    );
            }

            if (statsTotalGamesWon == statsTotalGamesPlayed)
            {
                return String.format(
                    "You won %d out of %d %s, with %d successful placements, an average of %.2f per game",
                    statsTotalGamesWon,
                    statsTotalGamesPlayed,
                    gameWord,
                    statsTotalSuccessfulPlacements,
                    statsAveragePlacementsPerGame
                                    );
            }

            return String.format(
                "You won %d out of %d %s and you lost %d out of %d %s, with %d successful placements, an average of "
                + "%.2f per game",
                statsTotalGamesWon,
                statsTotalGamesPlayed,
                gameWord,
                statsTotalGamesLost,
                statsTotalGamesPlayed,
                gameWord,
                statsTotalSuccessfulPlacements,
                statsAveragePlacementsPerGame
                                );
        }

        /**
         * Shows an informational guidance message to the player using a JavaFX alert.
         *
         * @param message the text of the guidance message to display
         */
        @Override
        public void showGuidanceMessage(final String message)
        {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hint");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        /**
         * Shows a game-over dialog that allows the user to either try again or quit.
         * On quit, the window is closed and the latch is decremented (if present).
         */
        @Override
        public void showGameOverMessage()
        {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(GAME_OVER_DIALOG_TITLE_TEXT);
            alert.setHeaderText(null);

            if (gameLogic.isGameLost())
            {
                alert.setContentText(
                    GAME_OVER_DIALOG_LOSS_PREFIX_TEXT + gameLogic.getCurrentNumber()
                                    );
            }
            else
            {
                alert.setContentText(GAME_OVER_DIALOG_WIN_TEXT);
            }

            alert.getButtonTypes().clear();

            final ButtonType tryAgainButtonType = new ButtonType(GAME_OVER_DIALOG_TRY_AGAIN_TEXT);
            final ButtonType quitButtonType = new ButtonType(GAME_OVER_DIALOG_QUIT_TEXT);

            alert.getButtonTypes().addAll(tryAgainButtonType, quitButtonType);

            final Optional<ButtonType> selection = alert.showAndWait();

            if (selection.isPresent() && selection.get() == tryAgainButtonType)
            {
                gameLogic.startNewGame();
            }
            else if (selection.isPresent() && selection.get() == quitButtonType)
            {
                final Stage stage = (Stage) currentNumberDisplay.getScene().getWindow();
                stage.close();

                if (gameFinishedLatch != null)
                {
                    gameFinishedLatch.countDown();
                }
            }
        }

        /**
         * Re-renders the grid of buttons so that each button's text matches the value
         * stored at the corresponding index in the board array.
         *
         * @param currentBoard the current state of the game board
         */
        public void reRenderGrid(final int[] currentBoard)
        {
            for (int currentCellIndex = 0; currentCellIndex < currentBoard.length; currentCellIndex++)
            {
                final int valueInCell = currentBoard[currentCellIndex];

                if (valueInCell == EMPTY_CELL_VALUE)
                {
                    cellButtons[currentCellIndex].setText(EMPTY_CELL_DISPLAY_TEXT);
                }
                else
                {
                    cellButtons[currentCellIndex].setText(String.valueOf(valueInCell));
                }
            }
        }
    };
}
