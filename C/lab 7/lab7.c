#include <stdio.h>

int main(int argc, char *argv[])
{
    if(argc != 3)
    {
        return 1;
    }

    char *inputFileName = argv[1];
    char *outputFileName = argv[2];
    char bitWidth = 0;
    long long operand1 = 0;
    long long operand2 = 0;

    FILE *inputFile = fopen(inputFileName, "r");
    if(inputFile == NULL)
    {
        perror("error opening file");
        return 1;
    }

    FILE *out = fopen(outputFileName, "w");

    char token[64];
    int tokensRead = 0;
    while(fscanf(inputFile, "%63s", token) == 1)
    {
        if(token[0] == 'E' && token[1] == '\0')
        {
            break;
        }

        long long tempValue;
        sscanf(token, "%lld", &tempValue);

        if(tokensRead == 0)
        {
            bitWidth = tempValue;
        }

        if(tokensRead == 1)
        {
            operand1 = tempValue;
        }

        if(tokensRead == 2)
        {
            operand2 = tempValue;
        }

        tokensRead++;
    }

}