package ca.bcit.comp2522.termproject;

import java.util.Scanner;

public class Main
{
    public static void main(final String[] args)
    {
        char input;
        final Scanner scan = new Scanner(System.in);

        while (true)
        {
            System.out.println("Press W to play the Word game.");
            System.out.println("Press N to play the Number game.");
            System.out.println("Press M to play the <your game's name> game.");
            System.out.println("Press Q to quit.");

            String rawInput = scan.nextLine().trim().toUpperCase();
            input = rawInput.charAt(0);

            if (input == 'W')
            {
                WordGame wordGame = new WordGame(scan);
                wordGame.playWordGame();
            }
            else if (input == 'N')
            {

            }
            else if (input == 'M')
            {

            }
            else if (input == 'Q')
            {
                break;
            }
            else
            {

            }

        }

        scan.close();
    }

}
