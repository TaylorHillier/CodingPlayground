package ca.bcit.comp2522.termproject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents scoring data for a gameplay session, and provides utilities
 * to persist scores, read them back, and compute high-score statistics.
 *
 * <p>A {@code Score} instance is immutable: once created with its counts and timestamp,
 * it calculates and stores the total score. Static methods handle reading and writing
 * score data to disk in both human-readable and CSV-like formats.</p>
 *
 * @author Taylor
 * @version 1.0
 */
public final class Score
{
    // -------------------------------------------------------
    // Directory & File Constants
    // -------------------------------------------------------

    private static final String SCORES_DIRECTORY_NAME = "scores";
    private static final String SCORES_FILE_NAME      = "scores.txt";

    // -------------------------------------------------------
    // Time & Formatting Constants
    // -------------------------------------------------------

    private static final String            DATE_TIME_PATTERN   = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    // Index constants for "yyyy-MM-dd HH:mm:ss".split(" ")
    private static final int DATE_TIME_COMPONENT_INDEX_DATE = 0;
    private static final int DATE_TIME_COMPONENT_INDEX_TIME = 1;

    private static final int CSV_INDEX_DATE_TIME      = 0;
    private static final int CSV_INDEX_GAMES_PLAYED   = 1;
    private static final int CSV_INDEX_CORRECT_FIRST  = 2;
    private static final int CSV_INDEX_CORRECT_SECOND = 3;
    private static final int CSV_INDEX_INCORRECT      = 4;

    // -------------------------------------------------------
    // Human-readable text prefixes
    // -------------------------------------------------------

    private static final String PREFIX_DATE_TIME      = "Date and Time: ";
    private static final String PREFIX_GAMES_PLAYED   = "Games played: ";
    private static final String PREFIX_CORRECT_FIRST  = "Correct First Attempts: ";
    private static final String PREFIX_CORRECT_SECOND = "Correct second attempts: ";
    private static final String PREFIX_INCORRECT      = "Incorrect attempts: ";
    private static final String PREFIX_TOTAL_SCORE    = "Total score: ";

    // -------------------------------------------------------
    // Score Calculation / Parsing Constants
    // -------------------------------------------------------

    private static final int SCORE_MULTIPLIER_FIRST_ATTEMPT = 2;
    private static final int MINIMUM_FIELDS_REQUIRED_CSV    = 5;

    private static final float DEFAULT_HIGHSCORE_INITIAL_VALUE = -1.0f;
    private static final int   ZERO_INT                        = 0;

    // -------------------------------------------------------
    // Instance Fields
    // -------------------------------------------------------

    private final String formattedDateTimePlayed;

    private final int numGamesPlayed;
    private final int numCorrectFirstAttempt;
    private final int numCorrectSecondAttempt;
    private final int numIncorrectTwoAttempts;

    private final int totalScore;

    /**
     * Constructs a score record for a particular gameplay session.
     *
     * @param dateTimePlayedParameter          date and time of the gameplay session
     * @param numGamesPlayedParameter          number of games played in the session
     * @param numCorrectFirstAttemptParameter  number of correct first attempts
     * @param numCorrectSecondAttemptParameter number of correct second attempts
     * @param numIncorrectTwoAttemptsParameter number of incorrect attempts
     */
    public Score(final LocalDateTime dateTimePlayedParameter,
                 final int numGamesPlayedParameter,
                 final int numCorrectFirstAttemptParameter,
                 final int numCorrectSecondAttemptParameter,
                 final int numIncorrectTwoAttemptsParameter)
    {
        numGamesPlayed          = numGamesPlayedParameter;
        numCorrectFirstAttempt  = numCorrectFirstAttemptParameter;
        numCorrectSecondAttempt = numCorrectSecondAttemptParameter;
        numIncorrectTwoAttempts = numIncorrectTwoAttemptsParameter;

        formattedDateTimePlayed = getCurrentTime(dateTimePlayedParameter);
        totalScore              = calculateTotalScore();
    }

    /**
     * Formats the specified time using the configured {@link DateTimeFormatter}.
     *
     * @param currentTime time to format
     * @return formatted date and time string
     */
    public static String getCurrentTime(final LocalDateTime currentTime)
    {
        return currentTime.format(DATE_TIME_FORMATTER);
    }

    // inside Score
    private static Path getDefaultScoreFilePath() throws IOException
    {
        final Path directoryPath;
        directoryPath = Paths.get(SCORES_DIRECTORY_NAME);

        if (Files.notExists(directoryPath))
        {
            Files.createDirectories(directoryPath);
        }

        final Path filePath;
        filePath = directoryPath.resolve(SCORES_FILE_NAME);

        return filePath;
    }


    /**
     * Saves this score to the default game score file (scores/scores.txt)
     * using the same CSV format as the test helper methods.
     *
     * @throws IOException if writing to the file fails
     */
    public void saveToDefaultScoreFile() throws IOException
    {
        final Path defaultScoreFilePath;
        defaultScoreFilePath = getDefaultScoreFilePath();

        // Reuse the tested static helper
        appendScoreToFile(this, defaultScoreFilePath.toString());
    }


    /**
     * Gets the current high score value from disk.
     *
     * @return the high score as a float
     * @throws IOException if reading from the score file fails
     */
    public static float getHighScore() throws IOException
    {
        final Path defaultScoreFilePath;
        defaultScoreFilePath =
            Paths.get(SCORES_DIRECTORY_NAME).resolve(SCORES_FILE_NAME);

        if (Files.notExists(defaultScoreFilePath))
        {
            return DEFAULT_HIGHSCORE_INITIAL_VALUE;
        }

        final List<Score> scores;
        scores = readScoresFromFile(defaultScoreFilePath.toString());

        if (scores.isEmpty())
        {
            return DEFAULT_HIGHSCORE_INITIAL_VALUE;
        }

        int highestScoreValue;
        highestScoreValue = ZERO_INT;

        for (final Score score : scores)
        {
            if (score.totalScore > highestScoreValue)
            {
                highestScoreValue = score.totalScore;
            }
        }

        return highestScoreValue;
    }


    public static String getHighScoreTime() throws IOException
    {
        final Path defaultScoreFilePath;
        defaultScoreFilePath =
            Paths.get(SCORES_DIRECTORY_NAME).resolve(SCORES_FILE_NAME);

        if (Files.notExists(defaultScoreFilePath))
        {
            final String fallbackCurrentTimeValue;
            fallbackCurrentTimeValue = getCurrentTime(LocalDateTime.now());

            final String[] components;
            components = fallbackCurrentTimeValue.split(" ");

            return components[DATE_TIME_COMPONENT_INDEX_TIME];
        }

        final List<Score> scores;
        scores = readScoresFromFile(defaultScoreFilePath.toString());

        if (scores.isEmpty())
        {
            final String fallbackCurrentTimeValue;
            fallbackCurrentTimeValue = getCurrentTime(LocalDateTime.now());

            final String[] components;
            components = fallbackCurrentTimeValue.split(" ");

            return components[DATE_TIME_COMPONENT_INDEX_TIME];
        }

        Score bestScore;
        bestScore = null;

        int highestScoreValue;
        highestScoreValue = ZERO_INT;

        for (final Score score : scores)
        {
            if (bestScore == null || score.totalScore > highestScoreValue)
            {
                highestScoreValue = score.totalScore;
                bestScore         = score;
            }
        }

        final String[] timeComponents;
        timeComponents = bestScore.formattedDateTimePlayed.split(" ");

        return timeComponents[DATE_TIME_COMPONENT_INDEX_TIME];
    }


    public static String getHighScoreDate() throws IOException
    {
        final Path defaultScoreFilePath;
        defaultScoreFilePath =
            Paths.get(SCORES_DIRECTORY_NAME).resolve(SCORES_FILE_NAME);

        if (Files.notExists(defaultScoreFilePath))
        {
            final String fallbackCurrentTimeValue;
            fallbackCurrentTimeValue = getCurrentTime(LocalDateTime.now());

            final String[] components;
            components = fallbackCurrentTimeValue.split(" ");

            return components[DATE_TIME_COMPONENT_INDEX_DATE];
        }

        final List<Score> scores;
        scores = readScoresFromFile(defaultScoreFilePath.toString());

        if (scores.isEmpty())
        {
            final String fallbackCurrentTimeValue;
            fallbackCurrentTimeValue = getCurrentTime(LocalDateTime.now());

            final String[] components;
            components = fallbackCurrentTimeValue.split(" ");

            return components[DATE_TIME_COMPONENT_INDEX_DATE];
        }

        Score bestScore;
        bestScore = null;

        int highestScoreValue;
        highestScoreValue = ZERO_INT;

        for (final Score score : scores)
        {
            if (bestScore == null || score.totalScore > highestScoreValue)
            {
                highestScoreValue = score.totalScore;
                bestScore         = score;
            }
        }

        final String[] dateComponents;
        dateComponents = bestScore.formattedDateTimePlayed.split(" ");

        return dateComponents[DATE_TIME_COMPONENT_INDEX_DATE];
    }


    /**
     * Calculates the total score for this instance based on correct attempts.
     *
     * @return total score
     */
    public int calculateTotalScore()
    {
        return (numCorrectFirstAttempt * SCORE_MULTIPLIER_FIRST_ATTEMPT)
               + numCorrectSecondAttempt;
    }

    /**
     * Gets the total score value for this instance.
     *
     * @return total score
     */
    public int getScore()
    {
        return totalScore;
    }

    /**
     * Appends the specified score as a CSV-like line to the given file.
     *
     * @param score         score instance to append
     * @param scoreFileName file name to write to
     * @throws IOException if writing to the file fails
     */
    public static void appendScoreToFile(final Score score,
                                         final String scoreFileName) throws IOException
    {
        try (FileWriter fileWriter = new FileWriter(scoreFileName, true))
        {
            final String line;
            line = String.format("%s,%d,%d,%d,%d%n",
                                 score.formattedDateTimePlayed,
                                 score.numGamesPlayed,
                                 score.numCorrectFirstAttempt,
                                 score.numCorrectSecondAttempt,
                                 score.numIncorrectTwoAttempts);

            fileWriter.write(line);
        }
    }

    /**
     * Reads all scores from the specified CSV-like score file.
     *
     * @param scoreFileName file name to read from
     * @return list of {@code Score} objects parsed from the file
     * @throws IOException if reading from the file fails
     */
    public static List<Score> readScoresFromFile(final String scoreFileName) throws IOException
    {
        final List<Score> scores;
        scores = new ArrayList<>();

        final Path path;
        path = Paths.get(scoreFileName);

        if (Files.notExists(path))
        {
            return scores;
        }

        try (BufferedReader reader = Files.newBufferedReader(path))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.trim().isEmpty())
                {
                    continue;
                }

                final String[] parts;
                parts = line.split(",");

                if (parts.length < MINIMUM_FIELDS_REQUIRED_CSV)
                {
                    continue;
                }

                final LocalDateTime dateTime;
                dateTime = LocalDateTime.parse(parts[CSV_INDEX_DATE_TIME], DATE_TIME_FORMATTER);

                final int gamesPlayed;
                gamesPlayed = Integer.parseInt(parts[CSV_INDEX_GAMES_PLAYED]);

                final int correctFirst;
                correctFirst = Integer.parseInt(parts[CSV_INDEX_CORRECT_FIRST]);

                final int correctSecond;
                correctSecond = Integer.parseInt(parts[CSV_INDEX_CORRECT_SECOND]);

                final int incorrect;
                incorrect = Integer.parseInt(parts[CSV_INDEX_INCORRECT]);

                final Score score;
                score = new Score(dateTime,
                                  gamesPlayed,
                                  correctFirst,
                                  correctSecond,
                                  incorrect);

                scores.add(score);
            }
        }

        return scores;
    }

    /**
     * Builds a human-readable representation of this {@code Score}.
     *
     * @return formatted multi-line string describing the score
     */
    @Override
    public String toString()
    {
        final StringBuilder builder;
        builder = new StringBuilder();

        builder.append("Date and Time: ")
               .append(formattedDateTimePlayed).append("\n")
               .append("Games Played: ").append(numGamesPlayed).append("\n")
               .append("Correct First Attempts: ").append(numCorrectFirstAttempt).append("\n")
               .append("Correct Second Attempts: ").append(numCorrectSecondAttempt).append("\n")
               .append("Incorrect Attempts: ").append(numIncorrectTwoAttempts).append("\n")
               .append("Score: ").append(totalScore).append(" points\n");

        return builder.toString();
    }

}
