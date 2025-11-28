package ca.bcit.comp2522.termproject;

import java.util.Random;

/**
 * Represents a country with a name, capital city, and a collection of facts.
 * This class is immutable: values cannot be changed once constructed.
 * <p>
 * All coding standards applied:
 * - final parameters
 * - private final fields
 * - local variables declared at top of scope
 * - no magic numbers (constant for fact count)
 * - JavaDoc for all public members
 * - no logic changed
 * <p>
 * NOTE: getFacts() returns the original array because logic must remain unchanged.
 * getRandomFactAtIndex() still uses fixed bound 3 for same reason.
 *
 * @author Taylor
 * @version 1.0
 */
public final class Country
{
    // -------------------- Constants --------------------

    private static final int RANDOM_FACT_UPPER_BOUND_EXCLUSIVE = 3;

    // -------------------- Instance Fields --------------------

    private final String   countryName;
    private final String   capitalCityName;
    private final String[] facts;

    // -------------------- Constructor --------------------

    /**
     * Constructs a new Country instance.
     *
     * @param name            the country name
     * @param capitalCityName the capital city name
     * @param facts           array of fact strings
     */
    public Country(final String name,
                   final String capitalCityName,
                   final String[] facts)
    {
        validateInputs(name, capitalCityName, facts);

        this.countryName     = name;
        this.capitalCityName = capitalCityName;
        this.facts           = facts;
    }

    // -------------------- Accessors --------------------

    /**
     * Returns the country's name.
     *
     * @return name string
     */
    public String getCountryName()
    {
        return countryName;
    }

    /**
     * Returns the capital city name.
     *
     * @return capital city string
     */
    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    /**
     * Returns the facts array.
     * (Logic unchanged—returns the original reference.)
     *
     * @return facts array reference
     */
    public String[] getFacts()
    {
        return facts;
    }

    /**
     * Returns a random fact using a fixed index range.
     * Logic intentionally unchanged—still assumes 3 facts.
     *
     * @return randomly selected fact string
     */
    public String getRandomFactAtIndex()
    {
        final Random randomIndexGenerator;
        final int randomIndex;
        final String randomFact;

        if (facts == null)
        {
            throw new NullPointerException("Facts is not instated yet");
        }

        randomIndexGenerator = new Random();
        randomIndex          = randomIndexGenerator.nextInt(RANDOM_FACT_UPPER_BOUND_EXCLUSIVE);
        randomFact           = facts[randomIndex];

        return randomFact;
    }

    /**
     * Builds a text representation of the country.
     *
     * @return formatted string
     */
    @Override
    public String toString()
    {
        final StringBuilder builder;
        final int factsLength;

        builder     = new StringBuilder();
        factsLength = facts.length;

        builder.append("Country name: ").append(countryName).append("\n");
        builder.append("Country capital: ").append(capitalCityName).append("\n");

        for (int factIndex = 0; factIndex < factsLength; factIndex++)
        {
            builder.append(facts[factIndex]).append("\n");
        }

        return builder.toString();
    }

    // -------------------- Input Validation --------------------

    /**
     * Validates constructor inputs.
     *
     * @param name            input country name
     * @param capitalCityName input capital city name
     * @param facts           input facts array
     */
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
