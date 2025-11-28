package ca.bcit.comp2522.termproject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * Represents a collection of {@link Country} objects loaded from resource files.
 * Each file (a.txt, b.txt, ..., z.txt) may contain multiple countries and facts.
 * This class provides a way to randomly select a country for use in games.
 *
 * <p>The format of each file is expected to be:</p>
 * <pre>
 * CountryName:CapitalCity
 * fact line 1
 * fact line 2
 * ...
 *
 * NextCountry:NextCapital
 * ...
 * </pre>
 *
 * @author Taylor
 * @version 1.0
 */
public final class World
{
    // -------------------- File / Parsing Constants --------------------

    private static final char FIRST_COUNTRY_FILE_LETTER = 'a';
    private static final char LAST_COUNTRY_FILE_LETTER  = 'z';

    private static final String COUNTRY_FILE_EXTENSION   = ".txt";
    private static final String COUNTRY_FILE_PATH_PREFIX = "/";

    private static final String COUNTRY_LINE_SEPARATOR   = ":";
    private static final int    COUNTRY_LINE_SPLIT_LIMIT = 2;

    private static final int COUNTRY_NAME_INDEX    = 0;
    private static final int COUNTRY_CAPITAL_INDEX = 1;

    private static final int EMPTY_ARRAY_LENGTH = 0;

    // -------------------- Fields --------------------

    private final Map<String, Country> countries;

    /**
     * Constructs a {@code World} and populates it by reading all country
     * data from resource files named 'a.txt' through 'z.txt'.
     */
    public World()
    {
        countries = new HashMap<>();
        generateWorld();
    }

    /**
     * Loads country data from resource files (a.txt through z.txt) and
     * populates the internal map.
     */
    private void generateWorld()
    {
        for (char currentFileLetter = FIRST_COUNTRY_FILE_LETTER; currentFileLetter <= LAST_COUNTRY_FILE_LETTER; currentFileLetter++)
        {
            final String fileName;
            fileName = currentFileLetter + COUNTRY_FILE_EXTENSION;

            final InputStream inputStream;
            inputStream = getClass().getResourceAsStream(COUNTRY_FILE_PATH_PREFIX + fileName);

            if (inputStream == null)
            {
                continue;
            }

            try (Scanner scanner = new Scanner(inputStream))
            {
                String[] countryProperties;
                String countryNameKey;
                String capitalCityName;
                List<String> facts;
                Country country;
                boolean firstLine;

                countryNameKey  = null;
                capitalCityName = null;
                facts           = new ArrayList<>();
                country         = null;
                firstLine       = false;

                while (scanner.hasNextLine())
                {
                    final String line;
                    line = scanner.nextLine().trim();

                    if (line.isEmpty())
                    {
                        firstLine = true;
                    }

                    if (line.contains(COUNTRY_LINE_SEPARATOR) && firstLine)
                    {
                        countryProperties = line.split(COUNTRY_LINE_SEPARATOR, COUNTRY_LINE_SPLIT_LIMIT);

                        countryNameKey  = countryProperties[COUNTRY_NAME_INDEX].trim();
                        capitalCityName = countryProperties[COUNTRY_CAPITAL_INDEX].trim();
                        firstLine       = false;
                    }
                    else
                    {
                        facts.add(line);
                    }

                    final String[] factArray;
                    factArray = facts.toArray(new String[EMPTY_ARRAY_LENGTH]);

                    if (line.isEmpty()
                        && countryNameKey != null
                        && capitalCityName != null)
                    {
                        country = new Country(countryNameKey, capitalCityName, factArray);

                        countries.put(countryNameKey, country);

                        countryNameKey  = null;
                        capitalCityName = null;
                        facts           = new ArrayList<>();
                    }
                }
            }
        }
    }

    /**
     * Returns a randomly selected {@link Country} from the world.
     *
     * @return a random country from the internal map
     */
    public Country getRandomCountry()
    {
        final Set<String> keySet;
        final Object[] keys;
        final Random randomNumberGenerator;
        final int randomIndex;
        final String randomKey;

        keySet                = countries.keySet();
        keys                  = keySet.toArray();
        randomNumberGenerator = new Random();
        randomIndex           = randomNumberGenerator.nextInt(keys.length);
        randomKey             = (String) keys[randomIndex];

        return countries.get(randomKey);
    }
}
