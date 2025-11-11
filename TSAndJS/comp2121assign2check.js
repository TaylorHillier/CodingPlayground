function loop(n) {
    let time = 100; 

    for (let i = 1; i <= n; i++) {
        time = time + 2;

        for (let k = 1; k <= i + 8; k++) { 
            for (let j = k; j <= 3 * k; j++) { 
                time = time + 12; 
            }
        }
    }

    return time; 
}

console.log(loop(50));