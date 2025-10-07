function getPrimesToN(n) {
    const primes = [];

    for (let i = 2; i <= n; i++) {
        let isPrime = true;
        for (let j = 2; j * j <= i; j++) {
            if (i % j === 0) {
                isPrime=false;
                break;
            }
        }
        if (isPrime) primes.push(i);
    }

    return primes;
}

console.log(getPrimesToN(37)); // [2, 3, 5, 7, 11, 13, 17, 19, 23, 29]