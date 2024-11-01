import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SortingAlgorithms {

    public static void mergeSort(int[] arr, int length) {
        if (length < 2)
            return;

        int middle = length / 2;
        int[] leftHalf = new int[middle];
        int[] rightHalf = new int[length - middle];

        System.arraycopy(arr, 0, leftHalf, 0, middle);
        System.arraycopy(arr, middle, rightHalf, 0, length - middle);

        mergeSort(leftHalf, middle);
        mergeSort(rightHalf, length - middle);

        combine(arr, leftHalf, rightHalf, middle, length - middle);
    }

    private static void combine(int[] arr, int[] leftHalf, int[] rightHalf, int leftSize, int rightSize) {
        int i = 0, j = 0, k = 0;
        while (i < leftSize && j < rightSize) {
            arr[k++] = (leftHalf[i] <= rightHalf[j]) ? leftHalf[i++] : rightHalf[j++];
        }
        while (i < leftSize)
            arr[k++] = leftHalf[i++];
        while (j < rightSize)
            arr[k++] = rightHalf[j++];
    }

    public static void bubbleSort(int[] arr) {
        int length = arr.length;
        for (int i = 0; i < length; i++) {
            boolean isSwapped = false;
            for (int j = 0; j < length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    isSwapped = true;
                }
            }
            if (!isSwapped)
                break; // Stop if no elements were swapped
        }
    }

    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivotValue = arr[high];
        int partitionIndex = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivotValue) {
                partitionIndex++;
                swap(arr, partitionIndex, j);
            }
        }
        swap(arr, partitionIndex + 1, high);
        return partitionIndex + 1;
    }

    public static void selectionSort(int[] arr) {
        int length = arr.length;
        for (int i = 0; i < length; i++) {
            int minimumIndex = i;
            for (int j = i + 1; j < length; j++) {
                if (arr[j] < arr[minimumIndex]) {
                    minimumIndex = j;
                }
            }
            swap(arr, minimumIndex, i);
        }
    }

    // Test and measure the execution time of each sorting algorithm
    public static String[] evaluateSortingAlgorithms(int[] array) {
        int[] mergeSortCopy = array.clone();
        int[] bubbleSortCopy = array.clone();
        int[] quickSortCopy = array.clone();
        int[] selectionSortCopy = array.clone();

        long startTime, timeMerge, timeBubble, timeSelection, timeQuick;

        // Timing Merge Sort
        startTime = System.nanoTime();
        mergeSort(mergeSortCopy, mergeSortCopy.length);
        timeMerge = System.nanoTime() - startTime;
        System.out.println("Merge Sort execution time (" + array.length + " elements): " + timeMerge + " ns");

        // Timing Bubble Sort
        startTime = System.nanoTime();
        bubbleSort(bubbleSortCopy);
        timeBubble = System.nanoTime() - startTime;
        System.out.println("Bubble Sort execution time (" + array.length + " elements): " + timeBubble + " ns");

        // Timing Quick Sort
        startTime = System.nanoTime();
        quickSort(quickSortCopy, 0, quickSortCopy.length - 1);
        timeQuick = System.nanoTime() - startTime;
        System.out.println("Quick Sort execution time (" + array.length + " elements): " + timeQuick + " ns");

        // Timing Selection Sort
        startTime = System.nanoTime();
        selectionSort(selectionSortCopy);
        timeSelection = System.nanoTime() - startTime;
        System.out.println("Selection Sort execution time (" + array.length + " elements): " + timeSelection + " ns");
        System.out.println("--------------------------------------------------\n");

        return new String[] {
                String.format("%d", array.length),
                String.format("%d", timeMerge),
                String.format("%d", timeBubble),
                String.format("%d", timeQuick),
                String.format("%d", timeSelection),
        };
    }

    private static void swap(int[] array, int firstIndex, int secondIndex) {
        int temp = array[firstIndex];
        array[firstIndex] = array[secondIndex];
        array[secondIndex] = temp;
    }

    // Main method to initialize arrays of different sizes and test sorting
    // algorithms
    public static void main(String[] args) {
        // Create random arrays of various sizes
        int[] tenElements = new Random().ints(10, 0, 100).toArray();
        int[] hundredElements = new Random().ints(100, 0, 1000).toArray();
        int[] thousandElements = new Random().ints(1000, 0, 10000).toArray();
        int[] tenThousandElements = new Random().ints(10000, 0, 10000).toArray();

        String[][] results = new String[4][];

        String filePath = "serial_sorting_results.csv";

        System.out.println("Comparing Serial Sorting Algorithms with Different Array Sizes\n");

        // Testing each array with all sorting algorithms
        System.out.println("Testing with array of 10 elements:");
        results[0] = evaluateSortingAlgorithms(tenElements);

        System.out.println("\nTesting with array of 100 elements:");
        results[1] = evaluateSortingAlgorithms(hundredElements);

        System.out.println("\nTesting with array of 1000 elements:");
        results[2] = evaluateSortingAlgorithms(thousandElements);

        System.out.println("\nTesting with array of 10000 elements:");
        results[3] = evaluateSortingAlgorithms(tenThousandElements);

        try (FileWriter writer = new FileWriter(filePath)) {
            // Write results to CSV
            for (String[] row : results) {
                writeToCSV(writer, row);
            }
        } catch (IOException e) {
            System.out.println("Error while writing CSV: " + e.getMessage());
        }
    }

    private static void writeToCSV(FileWriter writer, String[] values) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            line.append(values[i]);
            if (i < values.length - 1) {
                line.append(",");
            }
        }
        line.append("\n");
        writer.write(line.toString());
    }
}
