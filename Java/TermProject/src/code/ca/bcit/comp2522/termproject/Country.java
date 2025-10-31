package ca.bcit.comp2522.termproject;

import java.util.Random;

public class Country
{
    private final String   name;
    private final String   capitalCityName;
    private final String[] facts;

    public Country(final String name,
                   final String capitalCityName,
                   final String[] facts)
    {
        ValidateInputs(name, capitalCityName, facts);

        this.name            = name;
        this.capitalCityName = capitalCityName;
        this.facts           = facts;
    }

    public String getCountryName()
    {
        return name;
    }

    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    public String[] getFacts()
    {
        return facts;
    }

    public String getRandomFactAtIndex()
    {
        if (facts == null)
        {
            throw new NullPointerException("Facts is not instated yet");
        }

        final Random randomIndex;
        randomIndex = new Random();

        return facts[randomIndex.nextInt(3)];
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Country name: ").append(name).append("\n");
        sb.append("Country capital: ").append(capitalCityName).append("\n");

        for (String fact : facts)
        {
            sb.append(fact).append("\n");
        }

        return sb.toString();
    }

    private static void ValidateInputs(final String name,
                                       final String capitalCityName,
                                       final String[] facts)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Please add a name");
        }

        if (capitalCityName == null || capitalCityName.isBlank())
        {
            throw new IllegalArgumentException("Please add a capital city");
        }

        if (facts == null || facts.length < 1)
        {
            throw new IllegalArgumentException("Please Add a fact about the country");
        }
    }
}
