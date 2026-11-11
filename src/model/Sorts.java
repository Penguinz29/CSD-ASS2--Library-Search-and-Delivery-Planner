package model;

/** Four in-place sorting algorithms sharing copies-then-bookId order. */
public final class Sorts {
    private Sorts() { }

    public enum Algorithm { QUICK, MERGE, HEAP, INSERTION }

    public static final class Metrics {
        private long comparatorCalls;
        private long arrayWrites;
        public long getComparatorCalls() { return comparatorCalls; }
        public long getArrayWrites() { return arrayWrites; }
        private void reset() { comparatorCalls = 0L; arrayWrites = 0L; }
    }

    /** Comparator used by every sorter: copies ascending, then bookId ascending. */
    public static int compareBooks(Book first, Book second) {
        int byCopies = Integer.compare(first.getCopies(), second.getCopies());
        return byCopies != 0 ? byCopies : first.getBookId().compareTo(second.getBookId());
    }

    private static int compare(Book first, Book second, Metrics metrics) {
        metrics.comparatorCalls++;
        return compareBooks(first, second);
    }

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

    private static void quickSort(Book[] values, int low, int high, Metrics metrics) {
        if (low >= high) return;
        int pivotIndex = partition(values, low, high, metrics);
        quickSort(values, low, pivotIndex - 1, metrics);
        quickSort(values, pivotIndex + 1, high, metrics);
    }

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

    private static void mergeSort(Book[] values, Metrics metrics) {
        if (values.length < 2) return;
        Book[] temporary = new Book[values.length];
        mergeSort(values, temporary, 0, values.length - 1, metrics);
    }

    private static void mergeSort(Book[] values, Book[] temporary, int low, int high, Metrics metrics) {
        if (low >= high) return;
        int middle = low + (high - low) / 2;
        mergeSort(values, temporary, low, middle, metrics);
        mergeSort(values, temporary, middle + 1, high, metrics);
        int left = low;
        int right = middle + 1;
        int output = low;
        while (left <= middle && right <= high) {
            if (compare(values[left], values[right], metrics) <= 0) temporary[output++] = values[left++];
            else temporary[output++] = values[right++];
            metrics.arrayWrites++;
        }
        while (left <= middle) {
            temporary[output++] = values[left++];
            metrics.arrayWrites++;
        }
        while (right <= high) {
            temporary[output++] = values[right++];
            metrics.arrayWrites++;
        }
        for (int i = low; i <= high; i++) {
            values[i] = temporary[i];
            metrics.arrayWrites++;
        }
    }

    private static void heapSort(Book[] values, Metrics metrics) {
        int size = values.length;
        for (int root = size / 2 - 1; root >= 0; root--) siftDown(values, root, size, metrics);
        for (int end = size - 1; end > 0; end--) {
            swap(values, 0, end, metrics);
            siftDown(values, 0, end, metrics);
        }
    }

    private static void siftDown(Book[] values, int root, int size, Metrics metrics) {
        while (true) {
            int largest = root;
            int left = 2 * root + 1;
            int right = left + 1;
            if (left < size && compare(values[left], values[largest], metrics) > 0) largest = left;
            if (right < size && compare(values[right], values[largest], metrics) > 0) largest = right;
            if (largest == root) return;
            swap(values, root, largest, metrics);
            root = largest;
        }
    }

    private static void insertionSort(Book[] values, Metrics metrics) {
        for (int i = 1; i < values.length; i++) {
            Book current = values[i];
            int j = i - 1;
            while (j >= 0 && compare(values[j], current, metrics) > 0) {
                values[j + 1] = values[j];
                metrics.arrayWrites++;
                j--;
            }
            values[j + 1] = current;
            metrics.arrayWrites++;
        }
    }

    private static void swap(Book[] values, int first, int second, Metrics metrics) {
        Book temporary = values[first];
        values[first] = values[second];
        values[second] = temporary;
        metrics.arrayWrites += 2L;
    }

    /** Test and benchmark checker that does not call a library sorting method. */
    public static boolean isSortedAndPreserves(Book[] before, Book[] after) {
        return isSorted(after) && preservesRecords(before, after);
    }

    public static boolean isSorted(Book[] values) {
        for (int i = 1; i < values.length; i++) {
            if (compareBooks(values[i - 1], values[i]) > 0) return false;
        }
        return true;
    }

    public static boolean preservesRecords(Book[] before, Book[] after) {
        if (before.length != after.length) return false;
        java.util.Map<Book, Integer> counts = new java.util.HashMap<Book, Integer>();
        for (Book book : before) {
            Integer count = counts.get(book);
            counts.put(book, count == null ? 1 : count + 1);
        }
        for (int i = 0; i < after.length; i++) {
            Integer count = counts.get(after[i]);
            if (count == null) return false;
            if (count == 1) counts.remove(after[i]);
            else counts.put(after[i], count - 1);
        }
        return counts.isEmpty();
    }
}

