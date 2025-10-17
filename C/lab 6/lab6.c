#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

// Update this with your A number
char a_num[] = "01171951";

typedef struct numAtFirstIndex
{
    int row;
    char value;
} numAtFirst;

void zoomArray(char ***arr, 
               float n, 
               int *rows, 
               int *cols, 
               char* outputFileName)
{

    FILE *outputFile = fopen(outputFileName, "w");
    if (outputFile == NULL)
    {
        perror("Error opening input file");
        
    }

    int newRows = *rows * n;
    int newCols = *cols * n;
    numAtFirst *numArr = NULL;

    numArr = malloc(*rows * sizeof(*numArr));

    const int START_INDEX = 0;

    char lastFirstColRead = (*arr)[START_INDEX][START_INDEX];
    int mappedRows = START_INDEX;

    numArr[START_INDEX].row = mappedRows;
    numArr[START_INDEX].value = lastFirstColRead;
    mappedRows++;

    for (int i = 1; i < *rows; ++i)
    {
        char currentFirstCol = (*arr)[i][0];

        if (currentFirstCol == lastFirstColRead)
            continue;

        numArr[mappedRows].row = i * n;
        numArr[mappedRows].value = (*arr)[i][0];

        lastFirstColRead = currentFirstCol;

        mappedRows++;
    }

    int runIndex = 0;
    for (int i = 0; i < newRows; i++)
    {
        int nextBreakRow = 0;

        if (runIndex + 1 < mappedRows)
        {
            nextBreakRow = numArr[runIndex + 1].row;
        }
        else
        {
            nextBreakRow = newRows;
        }

        if (i >= nextBreakRow && runIndex + 1 < mappedRows)
        {
            runIndex++;
        }

        char valueForRow = numArr[runIndex].value;

        for (int j = 0; j < newCols; j++)
        {
            fprintf(outputFile, "%c", valueForRow);
        }

        if (i < newRows - 1)
        {
            fprintf(outputFile, "\n");
        }
    }

    fclose(outputFile);
}

int main(int argc, char *argv[])
{
    if (argc != 3)
    {
        printf("Usage: %s <input_file> <output_file\n", argv[0]);
        return 1;
    }

    char *inputFileName = argv[1];
    int dimensions = 0;
    char **arr = NULL;
    int rows = 0, cols = 0;
    float zoomFactor = 0;

    // Read the input array from the specified file
    FILE *file = fopen(inputFileName, "r");
    if (file == NULL)
    {
        perror("Error opening input file");
        return 1;
    }

    // Read the row value
    fscanf(file, "%d", &rows);
    // Read the column value
    fscanf(file, "%d", &cols);
    // Read the zoom factor
    fscanf(file, "%f", &zoomFactor);

    // Allocate memory for the 2D array
    arr = (char **)malloc(rows * sizeof(char *));
    if (arr == NULL)
    {
        printf("Memory allocation failed.\n");
        return 1;
    }
    for (int i = 0; i < rows; i++)
    {
        arr[i] = (char *)malloc(cols * sizeof(char));
        if (arr[i] == NULL)
        {
            printf("Memory allocation failed.\n");
            return 1;
        }
    }

    // Read the elements of the array until 'E' is encountered
    char inputChar;
    int i = 0, j = 0;
    while ((inputChar = fgetc(file)) != 'E')
    {
        if (inputChar >= '0' && inputChar <= '9' && i < rows && j < cols)
        {
            arr[i][j++] = inputChar;
            if (j == cols)
            {
                j = 0;
                i++;
            }
        }
    }

    fclose(file);
    printf("A%s\n", a_num);

    // Output the input array
    printf("Input Array:\n");
    for (int i = 0; i < rows; i++)
    {
        for (int j = 0; j < cols; j++)
        {
            printf("%c", arr[i][j]);
        }
        printf("\n");
    }
    printf("\n");

    // Call the zoomArray function
    zoomArray(&arr, zoomFactor, &rows, &cols, argv[2]);

    // Free the memory allocated for the 2D array
    for (int i = 0; i < rows; i++)
    {
        free(arr[i]);
    }
    free(arr);

    return 0;
}
