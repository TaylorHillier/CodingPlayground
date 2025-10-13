package ca.bcit.comp2522.lambdas;

import java.util.List;

public class HockeyTeam
{
    private final String             name;
    private       List<HockeyPlayer> roster;

    public HockeyTeam(final String name,
                      final List<HockeyPlayer> roster)
    {
        this.name   = name;
        this.roster = roster;
    }

    public void addPlayer(HockeyPlayer player)
    {
        roster.add(player);
    }

    public List<HockeyPlayer> getRoster()
    {
        return roster;
    }

}
