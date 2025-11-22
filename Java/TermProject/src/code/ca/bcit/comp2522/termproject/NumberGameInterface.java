package ca.bcit.comp2522.termproject;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NumberGameInterface extends Application
{
    Button[] cellButtons;
    Label    currentNumberLabel;
    Label    currentNumberDisplay;

    @Override
    public void start(final Stage s)
    {
        HBox hbox = new HBox(15);
        currentNumberLabel   = new Label("Next Number:");
        currentNumberDisplay = new Label("No Number Yet");

        hbox.getChildren().addAll(currentNumberLabel, currentNumberDisplay);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        cellButtons = new Button[20];

        for (int i = 0; i < 20; i++)
        {
            Button btn = new Button("-");
            btn.setMinSize(60, 60);

            final int capturedIndex = i;
            btn.setOnAction(e -> gameLogic.handleCellClick(capturedIndex));

            int row = i / 5;
            int col = i % 5;

            grid.add(btn, col, row);
            cellButtons[i] = btn;
        }

        VBox layout = new VBox(10);
        layout.getChildren().addAll(hbox, grid);

        Scene scene = new Scene(layout, 500, 450);
        s.setScene(scene);
        s.setTitle("Number Game");

        s.show();

        gameLogic.startNewGame();
    }

    public void launchNumberGame(final String[] args)
    {
        Application.launch(NumberGameInterface.class, args);
    }

    private final NumberGame gameLogic = new NumberGame()
    {
        @Override
        protected void onStateChanged()
        {
            int[] snapshot = currentGrid();

            for (int i = 0; i < currentGrid().length; i++)
            {
                int valueInCell = snapshot[i];

                if (valueInCell == 0)
                {
                    cellButtons[i].setText("-");
                }
                else
                {
                    cellButtons[i].setText(String.valueOf(valueInCell));
                }
            }

            if (isGameOver())
            {
                currentNumberDisplay.setText("Game Over");
            }
            else
            {
                currentNumberDisplay.setText(String.valueOf(getCurrentNumber()));
            }
        }

        @Override
        public void endCurrentGame(final boolean isLoss,
                                   final int numberOfPlacements,
                                   final int totalGamesLost,
                                   final int totalGamesPlayed,
                                   final double averagePlacementsPerGame)
        {
            String resultMessage = isLoss ? "You lost!" : "You won!";
            String statsMessage = String.format(
                "Games played: %d, won: %d, lost: %d, total placements: %d, avg per game: %.2f",
                totalGamesPlayed,
                totalGamesLost,
                totalGamesPlayed - totalGamesLost,
                numberOfPlacements,
                averagePlacementsPerGame);

            System.out.println(resultMessage);
            System.out.println(statsMessage);


        }

        @Override
        public void showGuidanceMessage(final String message)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hint");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    };

}
