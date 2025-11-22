package ca.bcit.comp2522.termproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class NumberGame implements NumberGridGame
{
    private int[] board;
    int numberOfPlacements;
    private Random randomNumberGenerator;
    int currentNumber;
    public boolean gameOver;
    public boolean gameLost;
    public int     totalGamesLost;
    public int     totalGamesWon;
    public int     totalGamesPlayed;
    public int     totalSuccessfulPlacements;

    @Override
    public void startNewGame()
    {
        numberOfPlacements    = 0;
        board                 = new int[20];
        randomNumberGenerator = new Random();
        currentNumber         = getRandomNumber();

        Arrays.fill(board, 0);

        gameOver = false;
        gameLost = false;

        onStateChanged();
    }

    public int[] currentGrid()
    {
        return board.clone();
    }

    public boolean isGameOver()
    {
        return gameOver;
    }

    public boolean isGameLost()
    {
        return gameLost;
    }

    public int getCurrentNumber()
    {
        return currentNumber;
    }

    public int getRandomNumber()
    {
        return randomNumberGenerator.nextInt(1000) + 1;
    }

    public int[] validSpotsForTurn(final int currentNumber)
    {
        final List<Integer> validSpots;

        validSpots = new ArrayList<>();

        for (int i = 0; i < board.length; i++)
        {
            if (board[i] != 0)
            {
                continue;
            }

            int leftVal = -1;

            for (int j = i - 1; j >= 0; j--)
            {
                if (board[j] != 0)
                {
                    leftVal = board[j];
                    break;
                }
            }

            int rightVal = 1001;

            for (int j = i + 1; j < board.length; j++)
            {
                if (board[j] != 0)
                {
                    rightVal = board[j];
                    break;
                }
            }

            if (leftVal <= currentNumber && currentNumber <= rightVal)
            {
                validSpots.add(i);
                System.out.println(i);
            }

        }

        return validSpots.stream().mapToInt(Integer::intValue).toArray();

    }

    public void handleCellClick(final int cellIndex)
    {
        if (isGameOver())
        {
            return;
        }

        int[] validCells = validSpotsForTurn(currentNumber);

        int minValidIndex = Arrays.stream(validCells).min().orElse(0);
        int maxValidIndex = Arrays.stream(validCells).max().orElse(board.length - 1);

        if (cellIndex < minValidIndex)
        {
            showGuidanceMessage("You cannot place this number before the lowest number.\n");
            return;
        }

        if (cellIndex > maxValidIndex)
        {
            showGuidanceMessage("You cannot place this number after the highest number.\n");
            return;
        }

        final boolean isValidCell = Arrays.stream(validCells).anyMatch(index -> index == cellIndex);

        if (!isValidCell)
        {
            gameLost = true;
            finishGame(true);
            onStateChanged();
            return;
        }

        board[cellIndex] = currentNumber;
        numberOfPlacements++;

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

    public double getAveragePlacementsPerGame()
    {
        return (double) numberOfPlacements / totalGamesPlayed;
    }

    public void finishGame(final boolean isLoss)
    {
        gameOver = true;

        if (isLoss)
        {
            totalGamesLost++;
        }
        else
        {
            totalGamesWon++;
        }

        totalGamesPlayed++;

        double averagePlacementsPerGame = (double) totalSuccessfulPlacements / totalGamesPlayed;

        endCurrentGame(isLoss, numberOfPlacements, totalGamesLost, totalGamesPlayed, averagePlacementsPerGame);
    }

    protected abstract void onStateChanged();

    public abstract void endCurrentGame(final boolean isLoss,
                                        final int numberOfPlacements,
                                        final int totalGamesLost,
                                        final int totalGamesPlayed,
                                        final double averagePlacementsPerGame);

    public abstract void showGuidanceMessage(final String message);
}



