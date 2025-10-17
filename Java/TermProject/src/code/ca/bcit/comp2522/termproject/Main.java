package ca.bcit.comp2522.termproject;

public class Main
{
    public static void main(final String[] args)
    {
//        char input;
//        final Scanner scan = new Scanner(System.in);
//
//        System.out.println("Press W to play the Word game.");
//        System.out.println("Press N to play the Number game.");
//        System.out.println("Press M to play the <your game's name> game.");
//        System.out.println("Press Q to quit.");
//
//        input = scan.next().toUpperCase().charAt(0);

//        if (input == 'W')
//        {
        WordGame wordGame = new WordGame();
        wordGame.playWordGame();
//        }
    }

}
