package ca.bcit.comp2522.termproject;

public interface NumberGridGame
{
    void startNewGame();

    void handleCellClick(final int cellIndex);

    boolean isGameOver();

    boolean isGameLost();

    int[] currentGrid();

    int getCurrentNumber();
}
