package ca.bcit.comp2522.termproject;

import javafx.application.Platform;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Entry point for running multiple games through a text-based menu.
 * This class manages the loop that reads user selections, launches the
 * appropriate game, and waits for UI-based games (JavaFX) to finish
 * using a {@link CountDownLatch}. It also safely initializes JavaFX
 * exactly once and reuses the runtime for subsequent Number Game launches.
 * <p>
 * The program continues prompting until the user chooses to quit.
 *
 * @author Taylor Hillier
 * @version 1.0
 */
public final class Main
{
    // -------------------- Constants --------------------

    private static final char WORD_GAME_OPTION_CHAR   = 'W';
    private static final char NUMBER_GAME_OPTION_CHAR = 'N';
    private static final char CUSTOM_GAME_OPTION_CHAR = 'M';
    private static final char QUIT_OPTION_CHAR        = 'Q';

    private static final String MENU_TEXT_WORD_GAME   =
        "Press W to play the Word game.";
    private static final String MENU_TEXT_NUMBER_GAME =
        "Press N to play the Number game.";
    private static final String MENU_TEXT_CUSTOM_GAME =
        "Press M to play the <your game's name> game.";
    private static final String MENU_TEXT_QUIT        =
        "Press Q to quit.";

    // -------------------- JavaFX State --------------------

    private static boolean javafxStarted = false;

    /**
     * Drives the game selector loop. Prompts the user for input, and depending
     * on the selected option, launches either a console-based game or a JavaFX-based one.
     * The loop continues until the quit option is selected.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(final String[] args)
    {
        final Scanner scanner = new Scanner(System.in);

        while (true)
        {
            System.out.println(MENU_TEXT_WORD_GAME);
            System.out.println(MENU_TEXT_NUMBER_GAME);
            System.out.println(MENU_TEXT_CUSTOM_GAME);
            System.out.println(MENU_TEXT_QUIT);

            final String rawInput = scanner.nextLine().trim().toUpperCase();

            if (rawInput.isEmpty())
            {
                continue;
            }

            final char firstCharacter = rawInput.charAt(0);

            if (firstCharacter == WORD_GAME_OPTION_CHAR)
            {
                final WordGame wordGame = new WordGame(scanner);
                wordGame.playWordGame();
            }
            else if (firstCharacter == NUMBER_GAME_OPTION_CHAR)
            {
                final CountDownLatch gameFinishedLatch = new CountDownLatch(1);

                openNumberGameWindow(gameFinishedLatch);

                try
                {
                    gameFinishedLatch.await();
                }
                catch (final InterruptedException interruption)
                {
                    Thread.currentThread().interrupt();
                }
            }
            else if (firstCharacter == CUSTOM_GAME_OPTION_CHAR)
            {
                // Reserved for user-defined future game.
            }
            else if (firstCharacter == QUIT_OPTION_CHAR)
            {
                break;
            }
            else
            {
                // Invalid option ignored.
            }
        }

        scanner.close();
    }

    private static void openNumberGameWindow(final CountDownLatch gameFinishedLatch)
    {
        if (!javafxStarted)
        {
            javafxStarted = true;

            Platform.startup(() ->
                             {
                                 Platform.setImplicitExit(false);

                                 final NumberGameInterface numberGame =
                                     new NumberGameInterface(gameFinishedLatch);

                                 numberGame.openInNewStage();
                             });
        }
        else
        {
            Platform.runLater(() ->
                              {
                                  final NumberGameInterface numberGame =
                                      new NumberGameInterface(gameFinishedLatch);

                                  numberGame.openInNewStage();
                              });
        }
    }
}
