package ca.bcit.comp2522.termproject;

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

public class WordGame
{
    private final World           world           = new World();
    private final QuestionFactory questionFactory = new QuestionFactory(world);

    public void playWordGame()
    {
        final Scanner input;
        input = new Scanner(System.in);

        for (int i = 0; i < 10; i++)
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

                final String userInput = input.next().trim().toLowerCase();
                correct = question.checkAnswer(userInput);

                guessCount++;

                if (correct)
                {
                    System.out.println("CORRECT!\n");
                }
                else if (guessCount < 2)
                {
                    System.out.print("INCORRECT!\n");
                }
                else
                {
                    System.out.println("The correct answer was " + question.getAnswer());
                }

            }

        }
    }
}

class CapitalQuestion implements Question
{
    private final Country country;

    public CapitalQuestion(final Country country)
    {
        this.country = country;
    }

    @Override
    public String getPrompt()
    {
        return "What is the capital city of " + country.getCountryName() + "?";
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return userAnswer.trim().equalsIgnoreCase(country.getCapitalCityName().trim());
    }

    @Override
    public String getAnswer()
    {
        return country.getCapitalCityName();
    }
}

class CountryQuestion implements Question
{
    private final Country country;

    public CountryQuestion(final Country country)
    {
        this.country = country;
    }

    @Override
    public String getPrompt()
    {
        return "What country is " + country.getCapitalCityName() + " in?";
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return userAnswer.trim().equalsIgnoreCase(country.getCountryName().trim());
    }

    @Override
    public String getAnswer()
    {
        return country.getCountryName();
    }
}

class FactQuestion implements Question
{
    private final Country country;
    private       String  randomFact = null;

    public FactQuestion(final Country country)
    {
        this.country = country;
        randomFact   = country.getRandomFactAtIndex();
    }

    @Override
    public String getPrompt()
    {
        return "What country does this quote describe: " + randomFact + "?";
    }

    @Override
    public boolean checkAnswer(String userAnswer)
    {
        return userAnswer.trim().equalsIgnoreCase(country.getCountryName().trim());
    }

    @Override
    public String getAnswer()
    {
        return country.getCountryName();
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

    Question generateRandomQuestion()
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

