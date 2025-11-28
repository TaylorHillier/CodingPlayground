/* 
* Course: COMP 2510 
* Assignment: Assignment 3 
* Name: Taylor Hillier
* Student ID: A01171951
* Date: 2025-11-24
* Description: This program runs a generic swap on two ints, floats or strings,
*              then runs an insertion/selection sort on a user-defined array.
*/ 

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void insertionSort(int numElements, int *elements);
void selectionSort(int numElements, int *elements);
int sortingAlgorithm();
void genericSwap();
void swap(void *data1ptr, void *data2ptr, size_t nbytes);

int main()
{
    genericSwap();
    sortingAlgorithm();
    return 0;
}

void genericSwap()
{
    printf("Select a data type: \n");
    printf("1. int \n");
    printf("2. float \n");
    printf("3. string \n");

    int choice;
    scanf("%d", &choice);

    printf("Provide 2 values to swap: \n");

    switch(choice)
    {
        case 1:
        {
            int firstItem;
            int secondItem;
            scanf("%d %d", &firstItem, &secondItem);

            printf("Before the swap: %d %d\n", firstItem, secondItem);

            // swap uses the raw memory block of the integer values
            swap(&firstItem, &secondItem, sizeof(int));

            printf("After the swap: %d %d\n", firstItem, secondItem);
            break;
        }

        case 2:
        {
            float firstItemFloat;
            float secondItemFloat;
            scanf("%f %f", &firstItemFloat, &secondItemFloat);

            printf("Before the swap: %f %f\n", firstItemFloat, secondItemFloat);

            // same logic but using float-sized memory
            swap(&firstItemFloat, &secondItemFloat, sizeof(float));

            printf("After the swap: %f %f\n", firstItemFloat, secondItemFloat);
            break;
        }

        case 3:
        default:
        {
            char firstItemString[100];
            char secondItemString[100];

            scanf("%99s %99s", firstItemString, secondItemString);

            printf("Before the swap: %s %s\n", firstItemString, secondItemString);

            // arrays decay to pointer-to-first-char; sizeof gives full array size (100 bytes)
            swap(firstItemString, secondItemString, sizeof(firstItemString));

            printf("After the swap: %s %s\n", firstItemString, secondItemString);
            break;
        }
    }
}

void swap(void *data1ptr, void *data2ptr, size_t nbytes)
{
    // allocate a temporary buffer exactly the size of the data being swapped
    char *temp = malloc(nbytes);

    // copy A to temp
    memcpy(temp, data1ptr, nbytes);

    // copy B to A
    memcpy(data1ptr, data2ptr, nbytes);

    // copy original A from temp to B
    memcpy(data2ptr, temp, nbytes);

    free(temp);
}

int sortingAlgorithm()
{
    int numberOfElements;

    printf("Enter the number of elements: \n");
    if (scanf("%d", &numberOfElements) != 1)
    {
        printf("Invalid number input.\n");
        return 1;
    }

    // allocate dynamic array based on user input
    int *elements = malloc(sizeof(int) * numberOfElements);
    if (elements == NULL)
    {
        printf("Memory allocation failed.\n");
        return 1;
    }

    printf("Enter %d integer values separated by spaces:\n", numberOfElements);

    for (int i = 0; i < numberOfElements; i++)
    {
        if (scanf("%d", &elements[i]) != 1)
        {
            printf("Invalid element input.\n");
            free(elements);
            return 1;
        }
    }

    printf("Unsorted array: ");
    for (int currentIndex = 0; currentIndex < numberOfElements; currentIndex++)
    {
        printf("%d ", elements[currentIndex]);
    }
    printf("\n");

    int userChoice;
    printf("Choose with sorting algorithm to use: \n");
    printf("1. Insertion sort \n");
    printf("2. Selection sort \n");
    scanf("%d", &userChoice);

    switch(userChoice)
    {
        case 1:
            // insertion sort builds sorted order from left side
            insertionSort(numberOfElements, elements);
            printf("Insertion Sort Result: ");
            break;

        case 2:
            // selection sort repeatedly finds the smallest remaining element
            selectionSort(numberOfElements, elements);
            printf("Selection Sort Result: ");
            break;
    }

    for (int i = 0; i < numberOfElements; i++)
    {
        printf("%d ", elements[i]);
    }
    printf("\n");

    free(elements);
    return 0;
}

void selectionSort(int numElements, int *elements)
{
    int y;
    int min;

    for (int i = 0; i < numElements; i++)
    {
        min = i; // assume current index is minimum

        // search right side for smaller value
        for (y = i + 1; y < numElements; y++)
        {
            if (elements[y] < elements[min])
            {
                min = y;
            }
        }
        
        // place smallest value into current index
        int temp = elements[min];
        elements[min] = elements[i];
        elements[i] = temp;
    }
}

void insertionSort(int numElements, int *elements)
{
    int key;
    int y;

    for (int i = 0; i < numElements; i++)
    {
        key = elements[i];
        y = i - 1;

        // shift larger elements to the right
        while (y >= 0 && elements[y] > key)
        {
            elements[y + 1] = elements[y];
            y = y - 1;
        }

        // insert key into correct sorted position
        elements[y + 1] = key;
    }
}
