package ca.bcit.comp2522.termproject;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

enum QuestionType
{
    CAPITAL, COUNTRY, FACT
}

interface Question
{
    String getPrompt();

    boolean checkAnswer(final String userAnswer);

    String getAnswer();
}

/**
 * Utility for removing accents from strings.
 */
final class AccentRemover
{
    private AccentRemover()
    {
        // Utility class; prevent instantiation.
    }

    public static String removeAccents(final String originalString)
    {
        final String normalizedString;
        normalizedString = Normalizer.normalize(originalString, Normalizer.Form.NFKD);

        final String resultWithoutAccents;
        resultWithoutAccents =
            normalizedString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return resultWithoutAccents;
    }
}

abstract class AbstractCountryQuestion implements Question
{
    protected final Country country;
    protected final String  normalizedCountryName;

    protected AbstractCountryQuestion(final Country country)
    {
        this.country = country;

        final String normalizedCountryNameValue;
        normalizedCountryNameValue = normalize(country.getCountryName());

        normalizedCountryName = normalizedCountryNameValue;
    }

    protected String normalize(final String word)
    {
        final String trimmedLowerCaseWord;
        trimmedLowerCaseWord = word.trim().toLowerCase();

        return AccentRemover.removeAccents(trimmedLowerCaseWord);
    }

    @Override
    public String getAnswer()
    {
        return country.getCountryName();
    }
}

/**
 * Handles the console-based word game loop that quizzes the player about
 * countries, capitals, and facts.
 */
public final class WordGame
{
    // -------------------- Game Constants (no magic numbers) --------------------

    private static final int FIRST_QUESTION_NUMBER     = 1;
    private static final int TOTAL_QUESTIONS_PER_ROUND = 10;

    private static final int INITIAL_GUESS_COUNT             = 0;
    private static final int MAXIMUM_GUESS_COUNT_EXCLUSIVE   = 2;
    private static final int FIRST_ATTEMPT_GUESS_COUNT_VALUE = 1;

    // -------------------- Game State --------------------

    private final World           world;
    private final QuestionFactory questionFactory;

    private int     firstCorrectGuesses;
    private int     secondCorrectGuesses;
    private int     incorrectGuesses;
    private int     gamesPlayed;
    private boolean playAgain = true;

    private final Scanner input;

    /**
     * Constructs a new {@code WordGame} that reads all user input from the provided scanner.
     *
     * @param input scanner used to read player responses from the console
     */
    public WordGame(final Scanner input)
    {
        this.input      = input;
        world           = new World();
        questionFactory = new QuestionFactory(world);
    }

    /**
     * Runs the main gameplay loop for the word game.
     * Continues until the user decides not to play again.
     */
    public void playWordGame()
    {
        while (playAgain)
        {
            gamesPlayed++;

            for (int questionNumber = FIRST_QUESTION_NUMBER;
                 questionNumber <= TOTAL_QUESTIONS_PER_ROUND;
                 questionNumber++)
            {
                int guessCount;
                guessCount = INITIAL_GUESS_COUNT;

                boolean correct;
                correct = false;

                final Question question;
                question = questionFactory.generateRandomQuestion();

                while (guessCount < MAXIMUM_GUESS_COUNT_EXCLUSIVE && !correct)
                {
                    if (guessCount == INITIAL_GUESS_COUNT)
                    {
                        System.out.println("Question " + questionNumber + ": " + question.getPrompt());
                    }

                    final String userInput;
                    userInput = input.nextLine().trim().toLowerCase();

                    correct = question.checkAnswer(userInput);
                    guessCount++;

                    if (correct)
                    {
                        System.out.println("CORRECT!\n");

                        if (guessCount == FIRST_ATTEMPT_GUESS_COUNT_VALUE)
                        {
                            firstCorrectGuesses++;
                        }
                        else
                        {
                            secondCorrectGuesses++;
                        }
                    }
                    else if (guessCount < MAXIMUM_GUESS_COUNT_EXCLUSIVE)
                    {
                        System.out.print("INCORRECT!\n");
                    }
                    else
                    {
                        System.out.println("The correct answer was " + question.getAnswer());
                        incorrectGuesses++;
                    }
                }
            }

            final String summary;
            summary =
                gamesPlayed + " word games played\n"
                + firstCorrectGuesses + " correct answers on first attempt\n"
                + secondCorrectGuesses + " correct answers on second attempt\n"
                + incorrectGuesses + " incorrect answers on the two attempts each";

            System.out.println(summary);

            String playerAnswer;

            while (true)
            {
                try
                {
                    System.out.println("Would you like to play again? (Yes or No)");

                    playerAnswer = input.nextLine().toLowerCase();

                    if (playerAnswer.equals("yes"))
                    {
                        break;
                    }

                    if (playerAnswer.equals("no"))
                    {
                        playAgain = false;

                        final Score newScore;
                        newScore = new Score(LocalDateTime.now(),
                                             gamesPlayed,
                                             firstCorrectGuesses,
                                             secondCorrectGuesses,
                                             incorrectGuesses);

                        final float scoreAverageCurrentRound;
                        scoreAverageCurrentRound = newScore.calculateTotalScore();

                        try
                        {
                            final float highScore;
                            final String highScoreTime;
                            final String highScoreDate;

                            // Use the CSV-backed high score methods
                            highScore     = Score.getHighScore();
                            highScoreTime = Score.getHighScoreTime();
                            highScoreDate = Score.getHighScoreDate();

                            // Use the CSV-backed save method (reuses appendScoreToFile)
                            newScore.saveToDefaultScoreFile();

                            if (scoreAverageCurrentRound > highScore)
                            {
                                System.out.println(
                                    "CONGRATULATIONS! You are the new high score with an average of "
                                    + scoreAverageCurrentRound + " points per game; the previous record was "
                                    + highScore + " points per game on " + highScoreDate + " at "
                                    + highScoreTime
                                                  );
                            }
                            else
                            {
                                System.out.println(
                                    "You did not beat the high score of " + highScore + " points per game from "
                                    + highScoreDate + " at " + highScoreTime
                                                  );
                            }
                        }
                        catch (final IOException exception)
                        {
                            throw new RuntimeException(exception);
                        }


                        break;
                    }

                    throw new IllegalArgumentException("Please enter either \"Yes\" or \"No\"");
                }
                catch (final IllegalArgumentException exception)
                {
                    System.out.println(exception.getMessage());
                }
            }
        }
    }
}

final class CapitalQuestion extends AbstractCountryQuestion
{
    private final String normalizedCapitalName;

    public CapitalQuestion(final Country country)
    {
        super(country);
        normalizedCapitalName = normalize(country.getCapitalCityName());
    }

    @Override
    public String getPrompt()
    {
        return "What is the capital city of " + country.getCountryName() + "?";
    }

    @Override
    public boolean checkAnswer(final String userAnswer)
    {
        final String normalizedUserAnswer;
        normalizedUserAnswer = normalize(userAnswer);

        return normalizedUserAnswer.equals(normalizedCapitalName);
    }

    @Override
    public String getAnswer()
    {
        return country.getCapitalCityName();
    }
}

final class CountryQuestion extends AbstractCountryQuestion
{
    private final String normalizedCountryNameLocal;

    public CountryQuestion(final Country country)
    {
        super(country);
        normalizedCountryNameLocal = normalize(country.getCountryName());
    }

    @Override
    public String getPrompt()
    {
        return "What country is " + country.getCapitalCityName() + " in?";
    }

    @Override
    public boolean checkAnswer(final String userAnswer)
    {
        final String normalizedUserAnswer;
        normalizedUserAnswer = normalize(userAnswer);

        return normalizedUserAnswer.equals(normalizedCountryNameLocal);
    }
}

final class FactQuestion extends AbstractCountryQuestion
{
    private final String randomFact;

    public FactQuestion(final Country country)
    {
        super(country);
        randomFact = country.getRandomFactAtIndex();
    }

    @Override
    public String getPrompt()
    {
        return "What country does this quote describe: " + randomFact;
    }

    @Override
    public boolean checkAnswer(final String userAnswer)
    {
        final String normalizedUserAnswer;
        normalizedUserAnswer = normalize(userAnswer);

        return normalizedUserAnswer.equals(normalizedCountryName);
    }
}

final class QuestionFactory
{
    private final World  world;
    private final Random random;

    public QuestionFactory(final World world)
    {
        this.world = world;
        random     = new Random();
    }

    Question generateRandomQuestion()
    {
        final Country randomCountry;
        final QuestionType[] questionTypes;
        final int randomTypeIndex;
        final QuestionType selectedQuestionType;

        randomCountry        = world.getRandomCountry();
        questionTypes        = QuestionType.values();
        randomTypeIndex      = random.nextInt(questionTypes.length);
        selectedQuestionType = questionTypes[randomTypeIndex];

        return switch (selectedQuestionType)
        {
            case CAPITAL -> new CapitalQuestion(randomCountry);
            case COUNTRY -> new CountryQuestion(randomCountry);
            case FACT -> new FactQuestion(randomCountry);
        };
    }
}
