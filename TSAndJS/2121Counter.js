function counter(n) {
    var counter = 250;

    for (let i = 5; i <= n; i++) 
    {

        for(let j = i + 1; j <= 4 * n; j++)
        {
            counter += 7;

            for(let k = j + 1; k <= 3 * n; k++)
            {
                counter += 9;
            }

        }
    }

    return counter;
}

console.log(counter(50));