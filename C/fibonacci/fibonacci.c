#include <stdio.h>

int fibonacci(int n)
{
    if(n <= 1)
    {
        return n;
    }

    printf("Current fib value: %d\n", n);

    return fibonacci(n-1) + fibonacci(n-2);
}

int main()
{
    int number = 0;

    printf("Enter a number");
    scanf("%d", &number);

    printf("Final fib value: %d", fibonacci(number));

}
