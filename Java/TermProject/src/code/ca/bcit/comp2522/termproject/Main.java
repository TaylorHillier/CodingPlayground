package ca.bcit.comp2522.termproject;

import javafx.application.Platform;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Entry point for running multiple games through a text-based menu.
 *
 * <p>This class presents a simple console menu that lets the user choose
 * between different games, including a JavaFX-based golf game and number game.
 * It keeps the JavaFX runtime alive across multiple game launches.</p>
 *
 * @author Taylor
 * @version 1.0
 */
public final class Main
{
    // -------------------- Constants --------------------

    /**
     * Menu option character for the word game.
     */
    private static final char WORD_GAME_OPTION_CHAR = 'W';

    /**
     * Menu option character for the number game.
     */
    private static final char NUMBER_GAME_OPTION_CHAR = 'N';

    /**
     * Menu option character for the golf game.
     */
    private static final char CUSTOM_GAME_OPTION_CHAR = 'G';

    /**
     * Menu option character to quit the program.
     */
    private static final char QUIT_OPTION_CHAR = 'Q';

    /**
     * Text prompt for the word game menu option.
     */
    private static final String MENU_TEXT_WORD_GAME =
        "Press W to play the Word game.";

    /**
     * Text prompt for the number game menu option.
     */
    private static final String MENU_TEXT_NUMBER_GAME =
        "Press N to play the Number game.";

    /**
     * Text prompt for the golf game menu option.
     */
    private static final String MENU_TEXT_CUSTOM_GAME =
        "Press G to play the Golf game.";

    /**
     * Text prompt for the quit menu option.
     */
    private static final String MENU_TEXT_QUIT =
        "Press Q to quit.";

    /**
     * Initial count used for the latch that tracks when a game finishes.
     */
    private static final int INITIAL_GAME_FINISHED_LATCH_COUNT = 1;

    // -------------------- JavaFX State --------------------

    /**
     * Tracks whether the JavaFX platform has already been started.
     */
    private static boolean javafxStarted = false;

    /**
     * Private constructor to prevent instantiation.
     */
    private Main()
    {
        // Prevent instantiation.
    }

    /**
     * Drives the game selector loop.
     *
     * @param args command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        final Scanner userInputScanner;
        userInputScanner = new Scanner(System.in);

        while (true)
        {
            System.out.println(MENU_TEXT_WORD_GAME);
            System.out.println(MENU_TEXT_NUMBER_GAME);
            System.out.println(MENU_TEXT_CUSTOM_GAME);
            System.out.println(MENU_TEXT_QUIT);

            final String rawInput;
            rawInput = userInputScanner.nextLine().trim().toUpperCase();

            if (rawInput.isEmpty())
            {
                continue;
            }

            final char menuSelectionCharacter;
            menuSelectionCharacter = rawInput.charAt(0);

            if (menuSelectionCharacter == WORD_GAME_OPTION_CHAR)
            {
                final WordGame wordGame;
                wordGame = new WordGame(userInputScanner);
                wordGame.playWordGame();
            }
            else if (menuSelectionCharacter == NUMBER_GAME_OPTION_CHAR)
            {
                final CountDownLatch gameFinishedLatch;
                gameFinishedLatch = new CountDownLatch(INITIAL_GAME_FINISHED_LATCH_COUNT);

                openJavaFxGameWindow(() ->
                                     {
                                         final NumberGameInterface numberGameInterface;
                                         numberGameInterface = new NumberGameInterface(gameFinishedLatch);
                                         numberGameInterface.openInNewStage();
                                     });

                awaitLatch(gameFinishedLatch);
            }
            else if (menuSelectionCharacter == CUSTOM_GAME_OPTION_CHAR)
            {
                final CountDownLatch gameFinishedLatch;
                gameFinishedLatch = new CountDownLatch(INITIAL_GAME_FINISHED_LATCH_COUNT);

                openJavaFxGameWindow(() ->
                                     {
                                         final GolfGameInterface golfGameInterface;
                                         golfGameInterface = new GolfGameInterface(gameFinishedLatch);
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
     * Uses a lambda to open any JavaFX game window.
     * <p>
     * This method ensures that the JavaFX platform is started only once and
     * then reused for subsequent game windows.
     * </p>
     *
     * @param gameWindowCreator runnable that creates and opens the game window
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

    /**
     * Blocks the current thread until the provided latch reaches zero.
     *
     * @param gameFinishedLatch latch that is counted down when the game finishes
     */
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
