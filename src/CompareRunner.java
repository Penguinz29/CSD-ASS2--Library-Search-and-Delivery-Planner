import model.Book;
import model.Sorts;
import util.Constants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Reproducible 48-case algorithm comparison with deterministic operation counts. */
public final class CompareRunner {
    private CompareRunner() { }

    public static void main(String[] args) throws IOException {
        Path output = Paths.get(args.length == 0 ? Constants.DEFAULT_SORT_RESULTS_PATH : args[0]);
        if (output.getParent() != null) Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write("algorithm,size,input_order,seed,comparator_calls,array_writes,sorted,records_preserved\n");
            int rows = 0;
            for (int size : Constants.COMPARE_SIZE_ARRAY) {
                for (String order : Constants.COMPARE_ORDER_ARRAY) {
                    Book[] input = makeInput(size, order);
                    for (Sorts.Algorithm algorithm : Sorts.Algorithm.values()) {
                        Book[] copy = input.clone();
                        Sorts.Metrics metrics = new Sorts.Metrics();
                        Sorts.sort(copy, algorithm, metrics);
                        boolean sorted = Sorts.isSorted(copy);
                        boolean preserved = Sorts.preservesRecords(input, copy);
                        boolean valid = sorted && preserved;
                        writer.write(algorithm.name().toLowerCase(Locale.ROOT) + "," + size + "," + order + ","
                                + Constants.COMPARE_SEED + "," + metrics.getComparatorCalls() + "," + metrics.getArrayWrites()
                                + "," + sorted + "," + preserved + "\n");
                        if (!valid) throw new IllegalStateException("incorrect result: " + algorithm + "/" + size + "/" + order);
                        rows++;
                    }
                }
            }
            System.out.println("Wrote " + rows + " verified rows to " + output);
            System.out.println("Seed=" + Constants.COMPARE_SEED + "; generator=java.util.Random, IDs B00000...B00999; copies 0..1000.");
        }
    }

    private static Book[] makeInput(int size, String order) {
        List<Book> books = new ArrayList<Book>();
        if ("seeded_random".equals(order)) {
            Random random = new Random(Constants.COMPARE_SEED + size);
            for (int i = 0; i < size; i++) books.add(book(i, random.nextInt(Constants.MAX_RANDOM_COPIES_BOUND)));
        } else if ("equal_copy_counts".equals(order)) {
            for (int i = 0; i < size; i++) books.add(book(i, Constants.EQUAL_COPIES_VALUE));
            Collections.shuffle(books, new Random(Constants.COMPARE_SEED + 17L * size));
        } else {
            for (int i = 0; i < size; i++) books.add(book(i, i / 10));
            if ("reverse_sorted".equals(order)) Collections.reverse(books);
        }
        return books.toArray(new Book[0]);
    }

    private static Book book(int index, int copies) {
        String id = String.format(Locale.ROOT, "B%05d", index);
        return new Book(id, "Generated title " + index, "Generated", copies);
    }
}
