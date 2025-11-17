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

public class Score
{
    private final String formattedDateTimePlayed;

    private final LocalDateTime dateTimePlayed;
    private final int           numGamesPlayed;
    private final int           numCorrectFirstAttempt;
    private final int           numCorrectSecondAttempt;
    private final int           numIncorrectTwoAttempts;

    private final int totalScore;

    public Score(final LocalDateTime dateTimePlayed,
                 final int numGamesPlayed,
                 final int numCorrectFirstAttempt,
                 final int numCorrectSecondAttempt,
                 final int numIncorrectTwoAttempts)
    {
        this.dateTimePlayed          = dateTimePlayed;
        this.numGamesPlayed          = numGamesPlayed;
        this.numCorrectFirstAttempt  = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;
        this.formattedDateTimePlayed = getCurrentTime(dateTimePlayed);
        totalScore                   = calculateTotalScore();
    }

    public static String getCurrentTime(final LocalDateTime currentTime)
    {
        final DateTimeFormatter formatter;
        final String formattedDateTime;

        formatter         = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        formattedDateTime = currentTime.format(formatter);

        return formattedDateTime;
    }

    private static class HighScoreData
    {
        private final String timeOfHighScore;
        private final float  highScore;

        public HighScoreData(final String timeOfHighScore,
                             final float highScore)
        {
            this.timeOfHighScore = timeOfHighScore;
            this.highScore       = highScore;
        }

    }

    public static float getHighScore() throws IOException
    {
        HighScoreData data = getHighScoreData();
        return data.highScore;
    }

    public static String getHighScoreTime() throws IOException
    {
        HighScoreData data = getHighScoreData();

        String[] time = data.timeOfHighScore != null ? data.timeOfHighScore.split(" ") :
            getCurrentTime(LocalDateTime.now()).split(" ");

        return time[1];
    }

    public static String getHighScoreDate() throws IOException
    {
        HighScoreData data = getHighScoreData();

        String[] date = data.timeOfHighScore != null ? data.timeOfHighScore.split(" ") :
            getCurrentTime(LocalDateTime.now()).split(" ");

        return date[0];
    }

    public int calculateTotalScore()
    {
        return (numCorrectFirstAttempt * 2) + numCorrectSecondAttempt;
    }

    public int getScore()
    {
        return totalScore;
    }

    public void appendScoreToFile()
    {
        try
        {
            Path directory = Paths.get("scores");

            if (Files.notExists(directory))
            {
                Files.createDirectories(directory);
            }

            Path filePath = directory.resolve("scores.txt");

            if (Files.notExists(filePath))
            {
                Files.createFile(filePath);
            }

            StringBuilder entry = new StringBuilder();

            entry.append("Date and Time: ").append(formattedDateTimePlayed).append("\n")
                 .append("Games played: ").append(numGamesPlayed).append("\n")
                 .append("Correct First Attempts: ").append(numCorrectFirstAttempt).append("\n")
                 .append("Correct second attempts: ").append(numCorrectSecondAttempt).append("\n")
                 .append("Incorrect attempts: ").append(numIncorrectTwoAttempts).append("\n")
                 .append("Total score: ").append(totalScore).append("\n\n");

            Files.write(filePath, entry.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void appendScoreToFile(final Score score,
                                         final String scoreFileName) throws IOException
    {
        try (FileWriter fileWriter = new FileWriter(scoreFileName, true))
        {
            String line = String.format("%s,%d,%d,%d,%d%n",
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
        List<Score> scores = new ArrayList<>();
        Path path = Paths.get(scoreFileName);

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

                String[] parts = line.split(",");

                if (parts.length < 5)
                {
                    continue;
                }

                LocalDateTime dateTime = LocalDateTime.parse(parts[0],
                                                             DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                int gamesPlayed = Integer.parseInt(parts[1]);
                int correctFirst = Integer.parseInt(parts[2]);
                int correctSecond = Integer.parseInt(parts[3]);
                int incorrect = Integer.parseInt(parts[4]);

                Score score = new Score(dateTime, gamesPlayed, correctFirst, correctSecond, incorrect);
                scores.add(score);
            }
        }

        return scores;
    }

    public static HighScoreData getHighScoreData() throws IOException
    {
        Path directory = Paths.get("scores");
        Path file = directory.resolve("scores.txt");

        if (Files.notExists(file))
        {
            throw new FileNotFoundException("File does not exist. Please play a game first");
        }

        float highestScore = -1;
        String timeOfHighScore = null;
        String currentTime = null;

        float currentScore = 0;
        float currentGamesPlayed = 0;

        try (BufferedReader reader = Files.newBufferedReader(file))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                String trimmedLine = line.trim();

                if (trimmedLine.startsWith("Date and Time: "))
                {
                    currentTime = trimmedLine.substring("Date and Time: ".length()).trim();
                }
                else if (trimmedLine.startsWith("Games played: "))
                {
                    String value = trimmedLine.substring("Games played: ".length()).trim();
                    currentGamesPlayed = Integer.parseInt(value);
                }
                else if (trimmedLine.startsWith("Total score: "))
                {
                    String value = trimmedLine.substring("Total score: ".length()).trim();
                    currentScore = Integer.parseInt(value);
                }

                if (currentGamesPlayed > 0)
                {
                    if (currentScore > highestScore)
                    {
                        if (highestScore == -1)
                        {
                            highestScore = 0;
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
        catch (final IOException e)
        {
            e.printStackTrace();
        }


        return new HighScoreData(timeOfHighScore, highestScore);
    }

    @Override
    public String toString()
    {
        return String.format(
            "Date and Time: %s\n" +
            "Games Played: %d\n" +
            "Correct First Attempts: %d\n" +
            "Correct Second Attempts: %d\n" +
            "Incorrect Attempts: %d\n" +
            "Score: %d points\n",
            formattedDateTimePlayed,
            numGamesPlayed,
            numCorrectFirstAttempt,
            numCorrectSecondAttempt,
            numIncorrectTwoAttempts,
            totalScore
                            );
    }
}

