package ca.bcit.comp2522.termproject;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

enum questionType
{CAPITAL, COUNTRY, FACT};

interface Question
{
    String getPrompt();

    boolean checkAnswer(String userAnswer);

    String getAnswer();
}

class AccentRemover
{
    public static String removeAccents(final String str)
    {
        String normalizedString = Normalizer.normalize(str, Normalizer.Form.NFKD);
        return normalizedString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}

abstract class AbstractCountryQuestion implements Question
{
    protected final Country country;
    protected final String  normalizedCountryName;

    protected AbstractCountryQuestion(final Country country)
    {
        this.country = country;

        this.normalizedCountryName = AccentRemover.removeAccents(country.getCountryName());
    }

    protected String normalize(final String word)
    {
        return AccentRemover.removeAccents(word.trim().toLowerCase());
    }

    @Override
    public String getAnswer()
    {
        return country.getCountryName();
    }
}

public class WordGame
{
    private final World           world                = new World();
    private final QuestionFactory questionFactory      = new QuestionFactory(world);
    private       int             firstCorrectGuesses  = 0;
    private       int             secondCorrectGuesses = 0;
    private       int             incorrectGuesses     = 0;
    private       int             gamesPlayed          = 0;
    private       boolean         playAgain            = true;

    private final Scanner input;

    public WordGame(final Scanner input)
    {
        this.input = input;
    }

    public void playWordGame()
    {
        while (playAgain)
        {
            gamesPlayed++;

            for (int i = 1; i <= 3; i++)
            {

                int guessCount = 0;
                boolean correct = false;
                final Question question = questionFactory.generateRandomQuestion();

                while (guessCount < 2 && !correct)
                {
                    if (guessCount == 0)
                    {
                        System.out.println("Question " + i + ": " + question.getPrompt());
                    }

                    final String userInput = input.nextLine().trim().toLowerCase();
                    System.out.println(userInput);
                    correct = question.checkAnswer(userInput);

                    guessCount++;

                    if (correct)
                    {
                        System.out.println("CORRECT!\n");

                        if (guessCount == 1)
                        {
                            firstCorrectGuesses++;
                        }
                        else
                        {
                            secondCorrectGuesses++;
                        }
                    }
                    else if (guessCount < 2)
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

            String summary = gamesPlayed + " word games played\n" +
                             firstCorrectGuesses + " correct answers on first attempt\n" +
                             secondCorrectGuesses + " correct answers on second attempt\n" +
                             incorrectGuesses + " incorrect answers on the two attempts each";
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

                        final Score newScore = new Score(LocalDateTime.now(),
                                                         gamesPlayed,
                                                         firstCorrectGuesses,
                                                         secondCorrectGuesses,
                                                         incorrectGuesses);

                        final float scoreAvgCurRound = newScore.calculateTotalScore();

                        try
                        {
                            final float highScore = Score.getHighScore();
                            final String highScoreTime = Score.getHighScoreTime();
                            final String highScoreDate = Score.getHighScoreDate();
                            
                            newScore.appendScoreToFile();

                            if (scoreAvgCurRound > highScore)
                            {
                                System.out.println("CONGRATULATIONS! You are the new high score with an average of " +
                                                   scoreAvgCurRound + " points per game; the previous record was " +
                                                   highScore + " points per game on " + highScoreDate + " at "
                                                   + highScoreTime);
                            }
                            else
                            {
                                System.out.println("You did not beat the high score of " + highScore + " points per " +
                                                   "game from " +
                                                   highScoreDate + " at " + highScoreTime);
                            }
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }

                        break;

                    }

                    throw new IllegalArgumentException("Please enter either \"Yes\" or \"No\"");
                }
                catch (IllegalArgumentException e)
                {
                    System.out.println(e.getMessage());
                }
            }

        }
    }
}

class CapitalQuestion extends AbstractCountryQuestion
{
    private final String normalizedCapitalName;

    public CapitalQuestion(final Country country)
    {
        super(country);

        this.normalizedCapitalName = normalize(country.getCapitalCityName());
    }


    @Override
    public String getPrompt()
    {
        return "What is the capital city of " + country.getCountryName() + "?";
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return normalize(userAnswer).equals(normalizedCapitalName);
    }

    @Override
    public String getAnswer()
    {
        return country.getCapitalCityName();
    }
}

class CountryQuestion extends AbstractCountryQuestion
{
    private final String normalizedCountryName;

    public CountryQuestion(final Country country)
    {
        super(country);

        this.normalizedCountryName = normalize(country.getCountryName());
    }

    @Override
    public String getPrompt()
    {
        return "What country is " + country.getCapitalCityName() + " in?";
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return normalize(userAnswer).equals(normalizedCountryName);
    }
}

class FactQuestion extends AbstractCountryQuestion
{
    private final String randomFact;

    public FactQuestion(final Country country)
    {
        super(country);

        this.randomFact = country.getRandomFactAtIndex();
    }

    @Override
    public String getPrompt()
    {
        return "What country does this quote describe: " + randomFact;
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return normalize(userAnswer).equals(normalizedCountryName);
    }
}

class QuestionFactory
{
    final World  world;
    final Random random;

    public QuestionFactory(final World world)
    {
        this.world  = world;
        this.random = new Random();
    }

    final Question generateRandomQuestion()
    {
        final Country randomCountry = world.getRandomCountry();
        questionType type = questionType.values()[random.nextInt(questionType.values().length)];

        return switch (type)
        {
            case CAPITAL -> new CapitalQuestion(randomCountry);
            case COUNTRY -> new CountryQuestion(randomCountry);
            case FACT -> new FactQuestion(randomCountry);
        };
    }
}


