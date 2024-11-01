import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelSorting {

    private static final int[] ARRAY_SIZES = {10, 100, 1000, 10000}; // Various sizes for testing
    private static final int THREAD_COUNT = 4; // Number of threads for parallel execution
    private static final String OUTPUT_PATH = "parallel_sort_results.csv";

    public static void main(String[] args) {
        System.out.println("Evaluating Parallel Sorting Algorithms with Different Array Sizes\n");

        long mergeTime, bubbleTime, selectionTime, quickTime;

        String[][] results = new String[ARRAY_SIZES.length][];

        for (int size : ARRAY_SIZES) {
            System.out.println("Testing with Array Size: " + size);
            int[] randomArray = createRandomArray(size);

            // Parallel Merge Sort
            int[] mergeArray = randomArray.clone();
            mergeTime = timeExecution(() -> ParallelMergeSorter.mergeSort(mergeArray, THREAD_COUNT));
            System.out.println("Merge Sort Execution Time: " + mergeTime + " ns");

            // Parallel Bubble Sort
            int[] bubbleArray = randomArray.clone();
            bubbleTime = timeExecution(() -> ParallelBubbleSorter.bubbleSort(bubbleArray, THREAD_COUNT));
            System.out.println("Bubble Sort Execution Time: " + bubbleTime + " ns");

            // Parallel Quick Sort
            int[] quickArray = randomArray.clone();
            quickTime = timeExecution(() -> ParallelQuickSorter.quickSort(quickArray, THREAD_COUNT));
            System.out.println("Quick Sort Execution Time: " + quickTime + " ns");

            // Parallel Selection Sort
            int[] selectionArray = randomArray.clone();
            selectionTime = timeExecution(() -> ParallelSelectionSorter.selectionSort(selectionArray, THREAD_COUNT));
            System.out.println("Selection Sort Execution Time: " + selectionTime + " ns");

            int index = findIndex(ARRAY_SIZES, size);
            results[index] = new String[]{
                    String.valueOf(size),
                    String.valueOf(mergeTime),
                    String.valueOf(bubbleTime),
                    String.valueOf(quickTime),
                    String.valueOf(selectionTime),
            };
            System.out.println("--------------------------------------------------\n");
        }

        saveResultsToCSV(results);
    }

    private static long timeExecution(Runnable task) {
        long start = System.nanoTime();
        task.run();
        return System.nanoTime() - start;
    }

    private static int[] createRandomArray(int size) {
        Random randomGenerator = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = randomGenerator.nextInt(1000); // Random numbers in the range 0 to 1000
        }
        return array;
    }

    private static int findIndex(int[] sizes, int targetSize) {
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] == targetSize) {
                return i;
            }
        }
        return -1;
    }

    private static void saveResultsToCSV(String[][] data) {
        try (FileWriter writer = new FileWriter(OUTPUT_PATH)) {
            for (String[] row : data) {
                writeRow(writer, row);
            }
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }

    private static void writeRow(FileWriter writer, String[] values) throws IOException {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            row.append(values[i]);
            if (i < values.length - 1) {
                row.append(",");
            }
        }
        row.append("\n");
        writer.write(row.toString());
    }

    static void exchange(int[] array, int firstIndex, int secondIndex) {
        int temp = array[firstIndex];
        array[firstIndex] = array[secondIndex];
        array[secondIndex] = temp;
    }
}

class ParallelSelectionSorter {
    public static void selectionSort(int[] array, int threadCount) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        int length = array.length;

        for (int i = 0; i < length - 1; i++) {
            final int currentIndex = i;
            executorService.submit(() -> {
                int minIndex = currentIndex;
                for (int j = currentIndex + 1; j < length; j++) {
                    if (array[j] < array[minIndex]) {
                        minIndex = j;
                    }
                }
                ParallelSorting.exchange(array, currentIndex, minIndex);
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {} // Await completion of all tasks
    }
}

class ParallelQuickSorter {
    public static void quickSort(int[] array, int threadCount) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        forkJoinPool.invoke(new QuickSortAction(array, 0, array.length - 1));
    }

    private static class QuickSortAction extends RecursiveAction {
        private final int[] array;
        private final int start;
        private final int end;

        public QuickSortAction(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (start < end) {
                int pivotIndex = partition(array, start, end);
                invokeAll(new QuickSortAction(array, start, pivotIndex - 1), new QuickSortAction(array, pivotIndex + 1, end));
            }
        }

        private int partition(int[] array, int start, int end) {
            int pivot = array[end];
            int partitionIndex = start - 1;

            for (int i = start; i < end; i++) {
                if (array[i] <= pivot) {
                    partitionIndex++;
                    ParallelSorting.exchange(array, partitionIndex, i);
                }
            }
            ParallelSorting.exchange(array, partitionIndex + 1, end);
            return partitionIndex + 1;
        }
    }
}

class ParallelMergeSorter {
    public static void mergeSort(int[] array, int threadCount) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        forkJoinPool.invoke(new MergeSortAction(array, 0, array.length - 1));
    }

    private static class MergeSortAction extends RecursiveAction {
        private final int[] array;
        private final int leftIndex;
        private final int rightIndex;

        public MergeSortAction(int[] array, int leftIndex, int rightIndex) {
            this.array = array;
            this.leftIndex = leftIndex;
            this.rightIndex = rightIndex;
        }

        @Override
        protected void compute() {
            if (leftIndex < rightIndex) {
                int middle = (leftIndex + rightIndex) / 2;

                // Fork the tasks
                invokeAll(new MergeSortAction(array, leftIndex, middle), new MergeSortAction(array, middle + 1, rightIndex));

                // Merge the sorted halves
                merge(array, leftIndex, middle, rightIndex);
            }
        }

        private void merge(int[] array, int leftIndex, int middle, int rightIndex) {
            int leftSize = middle - leftIndex + 1;
            int rightSize = rightIndex - middle;

            int[] leftArray = new int[leftSize];
            int[] rightArray = new int[rightSize];

            // Copy data to temporary arrays
            System.arraycopy(array, leftIndex, leftArray, 0, leftSize);
            System.arraycopy(array, middle + 1, rightArray, 0, rightSize);

            int i = 0, j = 0, k = leftIndex;

            while (i < leftSize && j < rightSize) {
                if (leftArray[i] <= rightArray[j]) {
                    array[k++] = leftArray[i++];
                } else {
                    array[k++] = rightArray[j++];
                }
            }

            // Copy remaining elements of leftArray
            while (i < leftSize) {
                array[k++] = leftArray[i++];
            }

            // Copy remaining elements of rightArray
            while (j < rightSize) {
                array[k++] = rightArray[j++];
            }
        }
    }
}

class ParallelBubbleSorter {
    public static void bubbleSort(int[] array, int threadCount) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        int length = array.length;

        for (int i = 0; i < length - 1; i++) {
            final int currentIndex = i;
            executorService.submit(() -> {
                for (int j = 0; j < length - currentIndex - 1; j++) {
                    if (array[j] > array[j + 1]) {
                        ParallelSorting.exchange(array, j, j + 1);
                    }
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {} // Wait until all tasks are finished
    }
}
