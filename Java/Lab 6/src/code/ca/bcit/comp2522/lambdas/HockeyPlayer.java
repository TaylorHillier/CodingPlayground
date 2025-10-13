package ca.bcit.comp2522.lambdas;

public class HockeyPlayer
{
    private final String name;
    private final String position;
    private final int    yearOfBirth;
    private final int    goals;

    public HockeyPlayer(final String name,
                        final String position,
                        final int yearOfBirth,
                        final int goals)
    {
        this.name        = name;
        this.position    = position;
        this.yearOfBirth = yearOfBirth;
        this.goals       = goals;
    }

    public String getPosition()
    {
        return this.position;
    }

    public int getGoals()
    {
        return this.goals;
    }

    public String getName()
    {
        return this.name;
    }

    public int getYearOfBirth()
    {
        return this.yearOfBirth;
    }

    @Override
    public String toString()
    {
        return "Player name: " +
               name +
               "\n Player Position: " +
               position +
               "\n Player Birthdate: " +
               yearOfBirth +
               "\n Player goals: "
               + goals;
    }
}
