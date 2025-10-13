package ca.bcit.comp2522.lambdas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

public class Main
{

    public static final int MIN_AGE      = 20;
    public static final int MIN_GOALS    = 15;
    public static final int CURRENT_YEAR = 2025;

    private static HockeyTeam team()
    {

        final List<HockeyPlayer> players;
        final HockeyPlayer player1;
        final HockeyPlayer player2;
        final HockeyPlayer player3;
        final HockeyPlayer player4;
        final HockeyPlayer player5;
        final HockeyPlayer player6;

        players = new ArrayList<>();

        player1 = new HockeyPlayer("Bobby Orr", "D", 1948, 24);
        player2 = new HockeyPlayer("Wayne Gretzky", "F", 1961, 60);
        player3 = new HockeyPlayer("Connor Mcdavid", "F", 1997, 19);
        player4 = new HockeyPlayer("Mario Lemieux", "F", 1965, 5);
        player5 = new HockeyPlayer("Quinn Hughes", "D", 1999, 22);
        player6 = new HockeyPlayer("Roberto Luongo", "G", 1979, 0);

        players.add(player1);
        players.add(player2);
        players.add(player3);
        players.add(player4);
        players.add(player5);
        players.add(player6);

        return new HockeyTeam("Canucks", players);
    }

    public static void main(final String[] args)
    {

        final HockeyTeam team = team();
        final List<HockeyPlayer> roster = team.getRoster();
        final HockeyPlayer demoPlayer = team.getRoster().getFirst();
        final HockeyPlayer demoPlayer2 = team.getRoster().get(1);

        System.out.println("===== 1) CALL-UP SUPPLIER =====");
        final Supplier<HockeyPlayer> callUp = () -> new HockeyPlayer("Elias Pettersson", "F", 1998, 22);
        team.addPlayer(callUp.get());
        System.out.println(roster.getLast());

        System.out.println("\n===== 2) PREDICATES (Forwards with 20+ Goals) =====");
        final Predicate<HockeyPlayer> isForward = player -> player.getPosition().equals("F");
        final Predicate<HockeyPlayer> has20Plus = player -> player.getGoals() >= 20;

        for (final HockeyPlayer player : team.getRoster())
        {
            if (isForward.test(player) && has20Plus.test(player))
            {
                System.out.println(player.toString());
            }
        }

        System.out.println("\n===== 3) FUNCTION (Player to String) =====");
        final Function<HockeyPlayer, String> playerToString =
            (player) -> player.getName() + " - Position: " + player.getPosition();
        System.out.println(playerToString.apply(team.getRoster().getFirst()));

        System.out.println("\n===== 4) CONSUMER (Print Names) =====");
        final Consumer<HockeyPlayer> printNames = (player) -> System.out.println(player.getName());
        printNames.accept(demoPlayer);

        System.out.println("\n===== 5) UNARY OPERATOR (Uppercase Names) =====");
        final UnaryOperator<String> upperCaseNames = x -> x.toUpperCase();
        System.out.println(upperCaseNames.apply(demoPlayer.getName()));

        System.out.println("\n===== 6) COMPARATOR (Sort by Goals DESC) =====");
        final Comparator<HockeyPlayer> sortByGoalsDesc = (playerOne, playerTwo) ->
            Integer.compare(playerTwo.getGoals(), playerOne.getGoals());
        roster.sort(sortByGoalsDesc);
        for (final HockeyPlayer p : roster)
        {
            System.out.println(p.getName() + " - " + p.getGoals() + " goals");
        }

        System.out.println("\n===== 7) AGGREGATION (Total Goals) =====");
        int goalSum = 0;
        for (final HockeyPlayer player : roster)
        {
            goalSum += player.getGoals();
        }
        System.out.println("Total goals: " + goalSum);

        System.out.println("\n===== 8) CUSTOM FUNCTIONAL INTERFACE (Eligibility Rule) =====");
        final EligibilityRule eligible = (player, minAge, minGoals, currentYear) ->
        {
            final int age = currentYear - player.getYearOfBirth();
            return age > minAge && player.getGoals() > minGoals;
        };

        for (final HockeyPlayer player : roster)
        {
            if (eligible.test(player, MIN_AGE, MIN_GOALS, CURRENT_YEAR))
            {
                System.out.println(player);
            }
        }

    }

    @FunctionalInterface
    interface EligibilityRule
    {
        boolean test(HockeyPlayer player,
                     int minAge,
                     int minGoals,
                     int currentYear);

    }
}
