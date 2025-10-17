package ca.bcit.comp2522.termproject;

import java.io.InputStream;
import java.util.*;

public class World
{
    final                Map<String, Country> countries;
    private final static int                  COUNTRY_NAME_INDEX    = 0;
    private final static int                  COUNTRY_CAPITAL_INDEX = 1;

    public World()
    {
        countries = new HashMap<>();
        generateWorld();
    }

    private void generateWorld()
    {
        for (char c = 'a'; c <= 'z'; c++)
        {
            String fileName;
            fileName = c + ".txt";

            InputStream inputStream;
            inputStream = getClass().getResourceAsStream("/" + fileName);

            if (inputStream == null)
            {
                System.out.println("File not found");
                continue;
            }

            try (Scanner scan = new Scanner(inputStream))
            {
                String[] countryProperties;
                String key = null;
                String city = null;
                List<String> facts;
                Country country;
                boolean firstLine = false;

                facts = new ArrayList<>();

                while (scan.hasNextLine())
                {
                    String line;
                    line = scan.nextLine().trim();

                    if (line.isEmpty())
                    {
                        firstLine = true;
                    }
                    if (line.contains(":") && firstLine)
                    {
                        countryProperties = line.split(":", 2);
                        key               = countryProperties[COUNTRY_NAME_INDEX].trim();
                        city              = countryProperties[COUNTRY_CAPITAL_INDEX].trim();
                        firstLine         = false;
                    }
                    else
                    {
                        facts.add(line);
                    }

                    String[] array;
                    array = facts.toArray(new String[0]);

                    if (line.isEmpty() &&
                        key != null &&
                        city != null)
                    {
                        country = new Country(key, city, array);
                        countries.put(key, country);
                        System.out.println(country);
                        key   = null;
                        city  = null;
                        facts = new ArrayList<>();
                    }
                }
            }
        }
    }
}
