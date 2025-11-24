package ca.bcit.comp2522.termproject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents scoring data for a gameplay session, and provides utilities
 * to persist scores, read them back, and compute high-score statistics.
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

    public Score(final LocalDateTime dateTimePlayed,
                 final int numGamesPlayed,
                 final int numCorrectFirstAttempt,
                 final int numCorrectSecondAttempt,
                 final int numIncorrectTwoAttempts)
    {
        this.numGamesPlayed          = numGamesPlayed;
        this.numCorrectFirstAttempt  = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;

        formattedDateTimePlayed = getCurrentTime(dateTimePlayed);
        totalScore              = calculateTotalScore();
    }

    public static String getCurrentTime(final LocalDateTime currentTime)
    {
        return currentTime.format(DATE_TIME_FORMATTER);
    }

    private static class HighScoreData
    {
        private final String timeOfHighScore;
        private final float  highScore;

        HighScoreData(final String timeOfHighScore,
                      final float highScore)
        {
            this.timeOfHighScore = timeOfHighScore;
            this.highScore       = highScore;
        }
    }

    public static float getHighScore() throws IOException
    {
        final HighScoreData highScoreData = getHighScoreData();
        return highScoreData.highScore;
    }

    public static String getHighScoreTime() throws IOException
    {
        final HighScoreData highScoreData = getHighScoreData();

        final String[] timeComponents =
            (highScoreData.timeOfHighScore != null)
                ? highScoreData.timeOfHighScore.split(" ")
                : getCurrentTime(LocalDateTime.now()).split(" ");

        return timeComponents[DATE_TIME_COMPONENT_INDEX_TIME];
    }

    public static String getHighScoreDate() throws IOException
    {
        final HighScoreData highScoreData = getHighScoreData();

        final String[] dateComponents =
            (highScoreData.timeOfHighScore != null)
                ? highScoreData.timeOfHighScore.split(" ")
                : getCurrentTime(LocalDateTime.now()).split(" ");

        return dateComponents[DATE_TIME_COMPONENT_INDEX_DATE];
    }

    public int calculateTotalScore()
    {
        return (numCorrectFirstAttempt * SCORE_MULTIPLIER_FIRST_ATTEMPT)
               + numCorrectSecondAttempt;
    }

    public int getScore()
    {
        return totalScore;
    }

    public void appendScoreToFile()
    {
        try
        {
            final Path directoryPath = Paths.get(SCORES_DIRECTORY_NAME);

            if (Files.notExists(directoryPath))
            {
                Files.createDirectories(directoryPath);
            }

            final Path filePath = directoryPath.resolve(SCORES_FILE_NAME);

            if (Files.notExists(filePath))
            {
                Files.createFile(filePath);
            }

            final StringBuilder entryBuilder = new StringBuilder();

            entryBuilder.append(PREFIX_DATE_TIME).append(formattedDateTimePlayed).append("\n")
                        .append(PREFIX_GAMES_PLAYED).append(numGamesPlayed).append("\n")
                        .append(PREFIX_CORRECT_FIRST).append(numCorrectFirstAttempt).append("\n")
                        .append(PREFIX_CORRECT_SECOND).append(numCorrectSecondAttempt).append("\n")
                        .append(PREFIX_INCORRECT).append(numIncorrectTwoAttempts).append("\n")
                        .append(PREFIX_TOTAL_SCORE).append(totalScore).append("\n\n");

            Files.write(filePath,
                        entryBuilder.toString().getBytes(),
                        StandardOpenOption.APPEND);
        }
        catch (final IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    public static void appendScoreToFile(final Score score,
                                         final String scoreFileName) throws IOException
    {
        try (FileWriter fileWriter = new FileWriter(scoreFileName, true))
        {
            final String line = String.format("%s,%d,%d,%d,%d%n",
                                              score.formattedDateTimePlayed,
                                              score.numGamesPlayed,
                                              score.numCorrectFirstAttempt,
                                              score.numCorrectSecondAttempt,
                                              score.numIncorrectTwoAttempts);
            fileWriter.write(line);
        }
    }

    public static List<Score> readScoresFromFile(final String scoreFileName) throws IOException
    {
        final List<Score> scores = new ArrayList<>();
        final Path path = Paths.get(scoreFileName);

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

                final String[] parts = line.split(",");

                if (parts.length < MINIMUM_FIELDS_REQUIRED_CSV)
                {
                    continue;
                }

                final LocalDateTime dateTime =
                    LocalDateTime.parse(parts[CSV_INDEX_DATE_TIME], DATE_TIME_FORMATTER);

                final int gamesPlayed =
                    Integer.parseInt(parts[CSV_INDEX_GAMES_PLAYED]);

                final int correctFirst =
                    Integer.parseInt(parts[CSV_INDEX_CORRECT_FIRST]);

                final int correctSecond =
                    Integer.parseInt(parts[CSV_INDEX_CORRECT_SECOND]);

                final int incorrect =
                    Integer.parseInt(parts[CSV_INDEX_INCORRECT]);


                final Score score = new Score(dateTime,
                                              gamesPlayed,
                                              correctFirst,
                                              correctSecond,
                                              incorrect);

                scores.add(score);
            }
        }

        return scores;
    }

    public static HighScoreData getHighScoreData() throws IOException
    {
        final Path directoryPath = Paths.get(SCORES_DIRECTORY_NAME);
        final Path filePath = directoryPath.resolve(SCORES_FILE_NAME);

        if (Files.notExists(filePath))
        {
            throw new FileNotFoundException("File does not exist. Please play a game first");
        }

        float highestScore = DEFAULT_HIGHSCORE_INITIAL_VALUE;
        String timeOfHighScore = null;

        float currentScore = ZERO_INT;
        float currentGamesPlayed = ZERO_INT;
        String currentTime = null;

        try (BufferedReader reader = Files.newBufferedReader(filePath))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                final String trimmedLine = line.trim();

                if (trimmedLine.startsWith(PREFIX_DATE_TIME))
                {
                    currentTime = trimmedLine.substring(PREFIX_DATE_TIME.length()).trim();
                }
                else if (trimmedLine.startsWith(PREFIX_GAMES_PLAYED))
                {
                    final String value =
                        trimmedLine.substring(PREFIX_GAMES_PLAYED.length()).trim();
                    currentGamesPlayed = Integer.parseInt(value);
                }
                else if (trimmedLine.startsWith(PREFIX_TOTAL_SCORE))
                {
                    final String value =
                        trimmedLine.substring(PREFIX_TOTAL_SCORE.length()).trim();
                    currentScore = Integer.parseInt(value);
                }

                if (currentGamesPlayed > ZERO_INT)
                {
                    if (currentScore > highestScore)
                    {
                        if (highestScore == DEFAULT_HIGHSCORE_INITIAL_VALUE)
                        {
                            highestScore = ZERO_INT;
                        }
                        else
                        {
                            highestScore = currentScore / currentGamesPlayed;
                        }

                        timeOfHighScore = currentTime;
                    }
                }
            }
        }
        catch (final IOException ioException)
        {
            ioException.printStackTrace();
        }

        return new HighScoreData(timeOfHighScore, highestScore);
    }

    @Override
    public String toString()
    {
        return String.format(
            "%s%s\n" +
            "Games Played: %d\n" +
            "Correct First Attempts: %d\n" +
            "Correct Second Attempts: %d\n" +
            "Incorrect Attempts: %d\n" +
            "Score: %d points\n",
            PREFIX_DATE_TIME,
            formattedDateTimePlayed,
            numGamesPlayed,
            numCorrectFirstAttempt,
            numCorrectSecondAttempt,
            numIncorrectTwoAttempts,
            totalScore
                            );
    }

}
