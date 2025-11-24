package ca.bcit.comp2522.termproject;

import java.util.Random;

/**
 * Represents a country with a name, capital city, and a collection of facts.
 * A {@code Country} object guarantees valid non-empty inputs and provides
 * methods for retrieving its data as well as a randomly selected fact.
 * <p>
 * The class is immutable: once created, the name, capital, and facts cannot
 * be changed.
 *
 * @author
 * @version 1.0
 */
public final class Country
{
    // -------------------- Constants --------------------

    /**
     * Required number of facts the game expects. Logic must NOT change; this constant only removes a magic number.
     */
    private static final int RANDOM_FACT_UPPER_BOUND_EXCLUSIVE = 3;

    // -------------------- Instance Fields --------------------

    private final String   name;
    private final String   capitalCityName;
    private final String[] facts;

    /**
     * Creates a new immutable {@code Country} instance.
     *
     * @param name            the name of the country
     * @param capitalCityName the capital city of the country
     * @param facts           an array of facts about the country
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Country(final String name,
                   final String capitalCityName,
                   final String[] facts)
    {
        validateInputs(name, capitalCityName, facts);

        this.name            = name;
        this.capitalCityName = capitalCityName;
        this.facts           = facts;
    }

    /**
     * Gets the name of this country.
     *
     * @return the country name
     */
    public String getCountryName()
    {
        return name;
    }

    /**
     * Gets the capital city name of this country.
     *
     * @return the capital city name
     */
    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    /**
     * Gets the array of facts about this country.
     *
     * @return the country facts array
     */
    public String[] getFacts()
    {
        return facts;
    }

    /**
     * Returns one fact selected at random based on the game's expected fact count.
     * Logic must stay unchanged, so this method uses a fixed bound of 3.
     *
     * @return a random fact from the facts array
     * @throws NullPointerException if the facts array is not instantiated
     */
    public String getRandomFactAtIndex()
    {
        if (facts == null)
        {
            throw new NullPointerException("Facts is not instated yet");
        }

        final Random randomIndex = new Random();

        return facts[randomIndex.nextInt(RANDOM_FACT_UPPER_BOUND_EXCLUSIVE)];
    }

    @Override
    public String toString()
    {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Country name: ").append(name).append("\n");
        stringBuilder.append("Country capital: ").append(capitalCityName).append("\n");

        for (final String fact : facts)
        {
            stringBuilder.append(fact).append("\n");
        }

        return stringBuilder.toString();
    }

    // -------------------- Private Validation --------------------

    private static void validateInputs(final String name,
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
