package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Sorting module of the library catalogue (Model layer).
 * The four assessed algorithms order a copy of the catalogue by copies
 * ascending, then bookId
 * ascending, through one shared comparator. A run counts comparator calls and
 * array writes
 * (a swap counts two writes, Merge Sort also counts its temporary buffer) and
 * resets the counters
 * before the algorithm starts.
 */
public final class Sorts {
    private Sorts() {
    }

    /**
     * The four assessed algorithms; INSERTION is the extra one required by the
     * assignment.
     */
    public enum Algorithm {
        QUICK, MERGE, HEAP, INSERTION
    }

    /**
     * Counters of one sort run; {@link #sort} resets them before the algorithm
     * starts.
     */
    public static final class Metrics {
        /** Comparisons made through the shared comparator. */
        private long comparatorCalls;
        /** Assignments into record array positions; one swap counts as two. */
        private long arrayWrites;

        /**
         * Returns the comparisons made during the last run, without generation or
         * verification.
         */
        public long getComparatorCalls() {
            return comparatorCalls;
        }

        /**
         * Returns the array writes of the last run, including Merge Sort's temporary
         * buffer.
         */
        public long getArrayWrites() {
            return arrayWrites;
        }

        /** Clears both counters so the next run is measured from zero. */
        private void reset() {
            comparatorCalls = 0L;
            arrayWrites = 0L;
        }
    }

    /**
     * Compares two books by copies ascending, then bookId ascending.
     * Every algorithm uses this method, so all four runs share the same order.
     * 
     * @param first  left book
     * @param second right book
     * @return negative, zero or positive when the first book is smaller, equal or
     *         larger
     */
    public static int compareBooks(Book first, Book second) {
        int byCopies = Integer.compare(first.getCopies(), second.getCopies());
        if (byCopies != 0) {
            return byCopies;
        }
        return first.getBookId().compareTo(second.getBookId());
    }

    /**
     * Counted wrapper around {@link #compareBooks}; only comparisons made while
     * sorting pass through it.
     */
    private static int compare(Book first, Book second, Metrics metrics) {
        metrics.comparatorCalls++;
        return compareBooks(first, second);
    }

    /**
     * Sorts one copy of the catalogue in place with the selected algorithm.
     * The caller passes the copy, so the loaded catalogue is never modified here.
     * 
     * @param values    copy of the catalogue to order
     * @param algorithm algorithm to run
     * @param metrics   counters receiving the measured work
     * @throws IllegalArgumentException when the array, the algorithm or the
     *                                  counters are missing
     */
    public static void sort(Book[] values, Algorithm algorithm, Metrics metrics) {
        if (values == null || algorithm == null || metrics == null) {
            throw new IllegalArgumentException("values, algorithm and metrics are required");
        }
        metrics.reset();
        switch (algorithm) {
            case QUICK:
                quickSort(values, 0, values.length - 1, metrics);
                break;
            case MERGE:
                mergeSort(values, metrics);
                break;
            case HEAP:
                heapSort(values, metrics);
                break;
            case INSERTION:
                insertionSort(values, metrics);
                break;
            default:
                throw new AssertionError("unhandled algorithm: " + algorithm);
        }
    }

    /**
     * Quick Sort with Lomuto partition. The last record is the pivot on purpose:
     * that plain choice exposes the quadratic worst case measured in the report.
     */
    private static void quickSort(Book[] values, int low, int high, Metrics metrics) {
        if (low >= high) {
            return;
        }
        int pivotIndex = partition(values, low, high, metrics);
        quickSort(values, low, pivotIndex - 1, metrics);
        quickSort(values, pivotIndex + 1, high, metrics);
    }

    /**
     * Partitions the range around its last record and returns the pivot position.
     */
    private static int partition(Book[] values, int low, int high, Metrics metrics) {
        Book pivot = values[high];
        int boundary = low;
        for (int i = low; i < high; i++) {
            if (compare(values[i], pivot, metrics) <= 0) {
                swap(values, boundary, i, metrics);
                boundary++;
            }
        }
        swap(values, boundary, high, metrics);
        return boundary;
    }

    /**
     * Stable Merge Sort; two runs are merged into one temporary buffer, left record
     * wins ties.
     */
    private static void mergeSort(Book[] values, Metrics metrics) {
        if (values.length < 2) {
            return;
        }
        Book[] buffer = new Book[values.length];
        mergeSort(values, buffer, 0, values.length - 1, metrics);
    }

    /** Sorts the inclusive range {@code [low, high]} using the shared buffer. */
    private static void mergeSort(Book[] values, Book[] buffer, int low, int high,
            Metrics metrics) {
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        mergeSort(values, buffer, low, middle, metrics);
        mergeSort(values, buffer, middle + 1, high, metrics);
        int left = low;
        int right = middle + 1;
        int output = low;
        while (left <= middle && right <= high) {
            if (compare(values[left], values[right], metrics) <= 0) {
                write(buffer, output++, values[left++], metrics);
            } else {
                write(buffer, output++, values[right++], metrics);
            }
        }
        while (left <= middle) {
            write(buffer, output++, values[left++], metrics);
        }
        while (right <= high) {
            write(buffer, output++, values[right++], metrics);
        }
        for (int i = low; i <= high; i++) {
            write(values, i, buffer[i], metrics);
        }
    }

    /**
     * In-place max heap; the maximum is swapped to the end before the heap is
     * repaired.
     */
    private static void heapSort(Book[] values, Metrics metrics) {
        int size = values.length;
        for (int root = size / 2 - 1; root >= 0; root--) {
            siftDown(values, root, size, metrics);
        }
        for (int end = size - 1; end > 0; end--) {
            swap(values, 0, end, metrics);
            siftDown(values, 0, end, metrics);
        }
    }

    /** Restores the max-heap property below one node. */
    private static void siftDown(Book[] values, int root, int size, Metrics metrics) {
        while (true) {
            int largest = root;
            int left = 2 * root + 1;
            int right = left + 1;
            if (left < size && compare(values[left], values[largest], metrics) > 0) {
                largest = left;
            }
            if (right < size && compare(values[right], values[largest], metrics) > 0) {
                largest = right;
            }
            if (largest == root) {
                return;
            }
            swap(values, root, largest, metrics);
            root = largest;
        }
    }

    /**
     * Stable insertion sort; larger records are shifted right until the current one
     * fits.
     */
    private static void insertionSort(Book[] values, Metrics metrics) {
        for (int i = 1; i < values.length; i++) {
            Book current = values[i];
            int j = i - 1;
            while (j >= 0 && compare(values[j], current, metrics) > 0) {
                write(values, j + 1, values[j], metrics);
                j--;
            }
            write(values, j + 1, current, metrics);
        }
    }

    /**
     * Swaps two positions; both assignments are counted, so one swap adds two
     * writes.
     */
    private static void swap(Book[] values, int first, int second, Metrics metrics) {
        Book temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
        metrics.arrayWrites += 2L;
    }

    /**
     * Stores one record and counts the write; shared by all sorters, including
     * Merge's buffer.
     */
    private static void write(Book[] values, int index, Book book, Metrics metrics) {
        values[index] = book;
        metrics.arrayWrites++;
    }

    /**
     * Checks the required order and that every record is preserved; never uses a
     * library sort.
     */
    public static boolean isSortedAndPreserves(Book[] before, Book[] after) {
        return isSorted(after) && preservesRecords(before, after);
    }

    /** Checks that adjacent records follow the shared total order. */
    public static boolean isSorted(Book[] values) {
        for (int i = 1; i < values.length; i++) {
            if (compareBooks(values[i - 1], values[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    /** Checks that both arrays hold the same multiset of records. */
    public static boolean preservesRecords(Book[] before, Book[] after) {
        if (before.length != after.length) {
            return false;
        }
        Map<Book, Integer> counts = new HashMap<Book, Integer>();
        for (Book book : before) {
            Integer count = counts.get(book);
            counts.put(book, count == null ? 1 : count + 1);
        }
        for (int i = 0; i < after.length; i++) {
            Integer count = counts.get(after[i]);
            if (count == null) {
                return false;
            }
            if (count == 1) {
                counts.remove(after[i]);
            } else {
                counts.put(after[i], count - 1);
            }
        }
        return counts.isEmpty();
    }
}
