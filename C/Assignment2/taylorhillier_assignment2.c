/*
* Course: COMP 2510
* Assignment: Assignment 2
* Name: Taylor Hillier
* Student ID: A01171951
* Date: 2025-11-02
* Description: Checks if the 2nd and 4th bits of an integer are ON, 
* and calculates the sum of digits of another integer.
*/

#include <stdio.h>

void printBinary(int num) {
    printf("Binary: ");
    for (int i = 8 - 1; i >= 0; i--) {
        printf("%d", (num >> i) & 1);
    }
    printf("\n");
}

// Checks if both 2nd and 4th bits of n are ON
int areSecondAndFourthBitsOn(int n)
{
    int bit2Mask = 0b00000100;
    int bit4Mask = 0b00010000;

    printBinary(n);

    if ((n & bit2Mask) && (n & bit4Mask)) {
        printf("Second and fourth bits are ON.\n");
    } else {
        printf("Second and fourth bits are NOT both ON.\n");
    }

    return (n & bit2Mask) && (n & bit4Mask);
}

// Recursively sums the digits of n
int sumOfDigits(int n)
{
    if (n == 0)
        return 0;

    int lastDigit = n % 10;
    int remainingNumber = n / 10;

    return lastDigit + sumOfDigits(remainingNumber);
}

int main()
{
    int userInput = 0;
    printf("Enter an integer: ");
    scanf("%d", &userInput);

    areSecondAndFourthBitsOn(userInput);

    int number = 0;
    printf("Enter a number: ");
    scanf("%d", &number);

    int sum = sumOfDigits(number);
    printf("Sum of digits = %d\n", sum);

    return 0;
}
