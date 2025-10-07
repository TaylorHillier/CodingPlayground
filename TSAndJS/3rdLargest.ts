function thirdLargestTS(arr: number[]): number {
    if( arr.length < 3 ) throw new Error("Array must have at least three elements");
    for(let i = 0; i < 2; i++) {
        let max = Math.max(...arr);
        arr.splice(arr.indexOf(max),1);
    }
    return Math.max(...arr);
}

console.log(thirdLargestTS([10, 4, 3, 50, 23, 90])); // Output: 50