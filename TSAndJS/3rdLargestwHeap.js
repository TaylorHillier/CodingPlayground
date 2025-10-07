function xLargest_heap(arr, x) {
    if (arr.length < x) throw new Error("Array must have at least " + x + " elements");
    //new mini array
    const minHeap = [];

    for (const n of arr) {
        //if not already in array
        if (!minHeap.includes(n)) {
            //add to array
            minHeap.push(n);
            //sort array with biggest first
            minHeap.sort((a, b) => b - a);         
            if (minHeap.length > x) minHeap.pop();
        }
    }

    return minHeap[x - 1];
}

console.log(xLargest_heap([10, 4, 3, 50, 23, 90], 4)); // Output: 50