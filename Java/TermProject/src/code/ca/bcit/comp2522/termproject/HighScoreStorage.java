package ca.bcit.comp2522.termproject;

import javafx.scene.control.Label;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles loading and saving the best score for the golf game.
 *
 * @author Taylor
 * @version 1.0
 */
public final class HighScoreStorage
{
    private static final String HIGH_SCORE_FILE_NAME = "golf_best_score.txt";

    private static final Path HIGH_SCORE_FILE_PATH =
        Path.of(System.getProperty("user.home"), HIGH_SCORE_FILE_NAME);

    private HighScoreStorage()
    {
        // Utility class; prevent instantiation.
    }

    /**
     * Loads the best score from disk, if available.
     *
     * @return best score, or null if none exists or file is invalid
     */
    public static Integer loadBestScore()
    {
        try
        {
            if (Files.exists(HIGH_SCORE_FILE_PATH))
            {
                final List<String> fileLines =
                    Files.readAllLines(HIGH_SCORE_FILE_PATH);

                final List<Integer> parsedScores =
                    fileLines.stream()
                             .map(String::trim)
                             .filter(line -> !line.isEmpty())
                             .map(Integer::parseInt)
                             .collect(Collectors.toList());

                if (!parsedScores.isEmpty())
                {
                    return parsedScores.get(0);
                }
            }
        }
        catch (final IOException | NumberFormatException exception)
        {
            return null;
        }

        return null;
    }

    /**
     * Saves the best score to disk.
     *
     * @param bestScoreStrokes best score to save
     * @param statusLabel      label for reporting save failures (may be null)
     */
    public static void saveBestScore(final int bestScoreStrokes,
                                     final Label statusLabel)
    {
        try
        {
            final String scoreAsString = Integer.toString(bestScoreStrokes);
            Files.writeString(HIGH_SCORE_FILE_PATH, scoreAsString);
        }
        catch (final IOException ioException)
        {
            if (statusLabel != null)
            {
                statusLabel.setText("Hole complete, but failed to save best score.");
            }
        }
    }
}
