package ca.bcit.comp2522.termproject;

import javafx.application.Platform;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Entry point for running multiple games through a text-based menu.
 */
public final class Main
{
    // -------------------- Constants --------------------

    private static final char WORD_GAME_OPTION_CHAR   = 'W';
    private static final char NUMBER_GAME_OPTION_CHAR = 'N';
    private static final char CUSTOM_GAME_OPTION_CHAR = 'G'; // G for Golf
    private static final char QUIT_OPTION_CHAR        = 'Q';

    private static final String MENU_TEXT_WORD_GAME   =
        "Press W to play the Word game.";
    private static final String MENU_TEXT_NUMBER_GAME =
        "Press N to play the Number game.";
    private static final String MENU_TEXT_CUSTOM_GAME =
        "Press G to play the Golf game.";
    private static final String MENU_TEXT_QUIT        =
        "Press Q to quit.";

    // -------------------- JavaFX State --------------------

    private static boolean javafxStarted = false;

    private Main()
    {
        // Prevent instantiation.
    }

    /**
     * Drives the game selector loop.
     */
    public static void main(final String[] args)
    {
        final Scanner userInputScanner = new Scanner(System.in);

        while (true)
        {
            System.out.println(MENU_TEXT_WORD_GAME);
            System.out.println(MENU_TEXT_NUMBER_GAME);
            System.out.println(MENU_TEXT_CUSTOM_GAME);
            System.out.println(MENU_TEXT_QUIT);

            final String rawInput = userInputScanner.nextLine().trim().toUpperCase();

            if (rawInput.isEmpty())
            {
                continue;
            }

            final char menuSelectionCharacter = rawInput.charAt(0);

            if (menuSelectionCharacter == WORD_GAME_OPTION_CHAR)
            {
                final WordGame wordGame = new WordGame(userInputScanner);
                wordGame.playWordGame();
            }
            else if (menuSelectionCharacter == NUMBER_GAME_OPTION_CHAR)
            {
                final CountDownLatch gameFinishedLatch = new CountDownLatch(1);

                openJavaFxGameWindow(() ->
                                     {
                                         final NumberGameInterface numberGameInterface =
                                             new NumberGameInterface(gameFinishedLatch);
                                         numberGameInterface.openInNewStage();
                                     });

                awaitLatch(gameFinishedLatch);
            }
            else if (menuSelectionCharacter == CUSTOM_GAME_OPTION_CHAR)
            {
                final CountDownLatch gameFinishedLatch = new CountDownLatch(1);

                openJavaFxGameWindow(() ->
                                     {
                                         final GolfGameInterface golfGameInterface =
                                             new GolfGameInterface(gameFinishedLatch);
                                         golfGameInterface.openInNewStage();
                                     });

                awaitLatch(gameFinishedLatch);
            }
            else if (menuSelectionCharacter == QUIT_OPTION_CHAR)
            {
                break;
            }
            else
            {
                // Invalid option ignored.
            }
        }

        userInputScanner.close();
    }

    /**
     * Uses a lambda (week 6) to open any JavaFX game window.
     * Reuses a single JavaFX runtime (Platform.startup only once).
     */
    private static void openJavaFxGameWindow(final Runnable gameWindowCreator)
    {
        if (!javafxStarted)
        {
            javafxStarted = true;

            Platform.startup(() ->
                             {
                                 Platform.setImplicitExit(false);
                                 gameWindowCreator.run();
                             });
        }
        else
        {
            Platform.runLater(gameWindowCreator);
        }
    }

    private static void awaitLatch(final CountDownLatch gameFinishedLatch)
    {
        try
        {
            gameFinishedLatch.await();
        }
        catch (final InterruptedException interruption)
        {
            Thread.currentThread().interrupt();
        }
    }
}
