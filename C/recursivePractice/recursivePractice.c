#include <stdio.h>

void printOneToN(int currentMax)
{

    if(currentMax == 0)
    {
        return;
    }

    printOneToN(currentMax - 1);

    printf("%d ", currentMax);
}

int sumOneToN(int currentMax)
{
    if(currentMax == 0)
    {
        return 0;
    }

    return currentMax + sumOneToN(currentMax - 1);
}

int factorial(int n)
{
    if(n <= 1)
    {
        return n;
    }

    return factorial(n-1) + factorial(n-2);
}

int sumOfDigits(unsigned int n)
{
    if(n < 10)
    {
        return n;
    }

    return (n % 10) + sumOfDigits(n / 10);
}

int sumArrayFromIndex(int *numbers, int totalLength, int currentIndex)
{
    if(currentIndex == totalLength)
    {
        return 0;
    }

    return numbers[currentIndex] + sumArrayFromIndex(numbers, totalLength, currentIndex + 1);
}




int main()
{
    printOneToN(5);
    int arr[5] = {5,7,3,2,1};

    printf("\n%d", sumOneToN(4));
    printf("\n%d", factorial(4));
    printf("\n%d", sumOfDigits(4096));
    printf("\n%d", sumArrayFromIndex(arr, 5, 0));
    return 0;
}