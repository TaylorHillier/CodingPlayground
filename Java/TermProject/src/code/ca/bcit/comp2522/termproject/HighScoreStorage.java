package ca.bcit.comp2522.termproject;

import javafx.scene.control.Label;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles loading and saving the best golf round relative to par
 * as a simple text file on disk.
 *
 * @author Taylor
 * @version 1.0
 */
public final class HighScoreStorage
{
    /**
     * File name used to store the best round relative to par.
     */
    private static final String BEST_ROUND_FILE_NAME = "golf_best_round.txt";

    /**
     * Absolute file system path to the best round file, resolved
     * against the current working directory.
     */
    private static final Path BEST_ROUND_FILE_PATH =
        Path.of("").toAbsolutePath().resolve(BEST_ROUND_FILE_NAME);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private HighScoreStorage()
    {
        // Utility class; prevent instantiation.
    }

    /**
     * Loads the best round relative to par from the storage file.
     * <p>
     * The method returns {@code null} if the file does not exist,
     * is empty, or contains a value that cannot be parsed as an integer.
     *
     * @return the best round relative to par, or {@code null} if unavailable
     */
    public static Integer loadBestRoundRelativeToPar()
    {
        try
        {
            if (!Files.exists(BEST_ROUND_FILE_PATH))
            {
                System.out.println("Best round file not found at: " + BEST_ROUND_FILE_PATH);
                return null;
            }

            final String fileContents;
            fileContents = Files.readString(BEST_ROUND_FILE_PATH).trim();

            if (fileContents.isEmpty())
            {
                System.out.println("Best round file is empty at: " + BEST_ROUND_FILE_PATH);
                return null;
            }

            final int parsedValue;
            parsedValue = Integer.parseInt(fileContents);
            
            return parsedValue;
        }
        catch (final IOException | NumberFormatException exception)
        {
            System.err.println("Failed to load best round from "
                               + BEST_ROUND_FILE_PATH + ": " + exception.getMessage());
            return null;
        }
    }

    /**
     * Saves the provided best round relative to par into the storage file.
     * <p>
     * If saving fails, an error is logged to standard error and the
     * status label is updated with a failure message if it is not {@code null}.
     *
     * @param relativeToPar the best round relative to par to persist
     * @param statusLabel   the label used to display a failure status message, or {@code null}
     */
    public static void saveBestRoundRelativeToPar(final int relativeToPar,
                                                  final Label statusLabel)
    {
        try
        {
            final String scoreAsString;
            scoreAsString = Integer.toString(relativeToPar);

            if (BEST_ROUND_FILE_PATH.getParent() != null)
            {
                Files.createDirectories(BEST_ROUND_FILE_PATH.getParent());
            }

            Files.writeString(BEST_ROUND_FILE_PATH, scoreAsString);

            System.out.println("Saved best round " + relativeToPar
                               + " to " + BEST_ROUND_FILE_PATH);
        }
        catch (final IOException ioException)
        {
            System.err.println("Failed to save best round to "
                               + BEST_ROUND_FILE_PATH + ": " + ioException.getMessage());

            if (statusLabel != null)
            {
                statusLabel.setText("Round complete, but failed to save best round.");
            }
        }
    }
}
