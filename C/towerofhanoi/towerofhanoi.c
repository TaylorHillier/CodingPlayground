#include <stdio.h>

int moveDisks(int numberOfDisks, char source, char destination, char auxiliary)
{
    if(numberOfDisks == 1)
    {
        printf("Move disk 1 from %c -> %c\n", source, destination);
        return 1;
    }

    int movesInFirstHalf = moveDisks(numberOfDisks - 1, source, auxiliary, destination);

    printf("Move disk %d from %c -> %c\n", numberOfDisks, source, destination);

    int movesInSecondHalf = moveDisks(numberOfDisks - 1, auxiliary, destination, source);

    return movesInFirstHalf + 1 + movesInSecondHalf;
}

int main()
{
    int totalDisks = 3;
    int totalMoves = moveDisks(totalDisks, 'A', 'C', 'B');
    printf("\nTotal moves: %d\n", totalMoves);
    return 0;
}