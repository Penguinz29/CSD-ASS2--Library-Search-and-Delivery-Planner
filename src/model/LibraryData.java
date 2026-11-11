package model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Strict CSV loader. A new state is published only after all three files validate. */
public final class LibraryData {
    private static final class CsvRow {
        private final int lineNumber;
        private final List<String> values;
        private CsvRow(int lineNumber, List<String> values) {
            this.lineNumber = lineNumber;
            this.values = values;
        }
    }

    public static final class State {
        private final List<Book> books;
        private final List<String> branchIds;
        private final BookTree bookTree;
        private final BranchGraph graph;

        private State(List<Book> books, List<String> branchIds, List<BranchGraph.Edge> edges) {
            this.books = Collections.unmodifiableList(new ArrayList<Book>(books));
            this.branchIds = Collections.unmodifiableList(new ArrayList<String>(branchIds));
            this.bookTree = new BookTree();
            for (Book book : books) bookTree.insert(book);
            this.graph = new BranchGraph(branchIds, edges);
        }
        public List<Book> getBooks() { return books; }
        public List<String> getBranchIds() { return branchIds; }
        public BookTree getBookTree() { return bookTree; }
        public BranchGraph getGraph() { return graph; }
    }

    private State current;

    public State getCurrent() { return current; }

    public void loadAndReplace(Path directory) throws IOException {
        State candidate = load(directory);
        current = candidate;
    }

    public static State load(Path directory) throws IOException {
        Path booksPath = directory.resolve("books.csv");
        Path branchesPath = directory.resolve("branches.csv");
        Path routesPath = directory.resolve("routes.csv");
        List<CsvRow> bookRows = readRows(booksPath, "bookId,title,category,copies");
        List<CsvRow> branchRows = readRows(branchesPath, "branchId,name");
        List<CsvRow> routeRows = readRows(routesPath, "from,to,distance");

        List<Book> books = new ArrayList<Book>();
        Set<String> bookIds = new HashSet<String>();
        for (int i = 0; i < bookRows.size(); i++) {
            CsvRow csvRow = bookRows.get(i);
            List<String> row = csvRow.values;
            int line = csvRow.lineNumber;
            requireColumns(booksPath, line, row, 4);
            String id = required(booksPath, line, row.get(0), "bookId");
            String title = required(booksPath, line, row.get(1), "title");
            String category = required(booksPath, line, row.get(2), "category");
            if (!bookIds.add(id)) fail(booksPath, line, "duplicate bookId '" + id + "'");
            int copies = parseInteger(booksPath, line, row.get(3), "copies");
            if (copies < 0) fail(booksPath, line, "copies must be >= 0");
            books.add(new Book(id, title, category, copies));
        }
        if (books.size() < 20) fail(booksPath, 1, "at least 20 books are required");

        List<String> branchIds = new ArrayList<String>();
        Set<String> branchSet = new LinkedHashSet<String>();
        for (int i = 0; i < branchRows.size(); i++) {
            CsvRow csvRow = branchRows.get(i);
            List<String> row = csvRow.values;
            int line = csvRow.lineNumber;
            requireColumns(branchesPath, line, row, 2);
            String id = required(branchesPath, line, row.get(0), "branchId");
            required(branchesPath, line, row.get(1), "name");
            if (!branchSet.add(id)) fail(branchesPath, line, "duplicate branchId '" + id + "'");
            branchIds.add(id);
        }
        if (branchIds.size() < 6) fail(branchesPath, 1, "at least six branches are required");

        List<BranchGraph.Edge> edges = new ArrayList<BranchGraph.Edge>();
        Set<String> pairs = new HashSet<String>();
        for (int i = 0; i < routeRows.size(); i++) {
            CsvRow csvRow = routeRows.get(i);
            List<String> row = csvRow.values;
            int line = csvRow.lineNumber;
            requireColumns(routesPath, line, row, 3);
            String from = required(routesPath, line, row.get(0), "from");
            String to = required(routesPath, line, row.get(1), "to");
            int distance = parseInteger(routesPath, line, row.get(2), "distance");
            if (!branchSet.contains(from)) fail(routesPath, line, "unknown branch ID '" + from + "'");
            if (!branchSet.contains(to)) fail(routesPath, line, "unknown branch ID '" + to + "'");
            if (from.equals(to)) fail(routesPath, line, "self-loop is not allowed");
            if (distance <= 0) fail(routesPath, line, "distance must be a positive integer");
            String first = from.compareTo(to) <= 0 ? from : to;
            String second = from.compareTo(to) <= 0 ? to : from;
            if (!pairs.add(first + "\0" + second)) fail(routesPath, line, "duplicate unordered route");
            edges.add(new BranchGraph.Edge(from, to, distance));
        }
        return new State(books, branchIds, edges);
    }

    private static List<CsvRow> readRows(Path path, String expectedHeader) throws IOException {
        if (!Files.isRegularFile(path)) throw new IOException("missing required file: " + path);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) fail(path, 1, "missing CSV header");
        List<String> header = parseCsv(path, 1, lines.get(0));
        if (!join(header).equals(expectedHeader)) {
            fail(path, 1, "expected header " + expectedHeader);
        }
        List<CsvRow> rows = new ArrayList<CsvRow>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) continue;
            rows.add(new CsvRow(i + 1, parseCsv(path, i + 1, lines.get(i))));
        }
        return rows;
    }

    private static List<String> parseCsv(Path path, int line, String text) {
        List<String> fields = new ArrayList<String>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        quoteClosed = true;
                    }
                } else field.append(ch);
            } else if (ch == ',') {
                fields.add(field.toString().trim());
                field.setLength(0);
                quoteClosed = false;
            } else if (ch == '"') {
                if (field.length() != 0 || quoteClosed) fail(path, line, "unexpected quote in CSV field");
                quoted = true;
            } else {
                if (quoteClosed) fail(path, line, "characters after closing quote");
                field.append(ch);
            }
        }
        if (quoted) fail(path, line, "unclosed quoted CSV field");
        fields.add(field.toString().trim());
        return fields;
    }

    private static String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) result.append(',');
            result.append(values.get(i));
        }
        return result.toString();
    }

    private static void requireColumns(Path path, int line, List<String> row, int expected) {
        if (row.size() != expected) fail(path, line, "expected " + expected + " columns, found " + row.size());
    }

    private static String required(Path path, int line, String value, String label) {
        if (value.trim().isEmpty()) fail(path, line, label + " must not be empty");
        return value.trim();
    }

    private static int parseInteger(Path path, int line, String value, String label) {
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException ex) { fail(path, line, label + " must be an integer"); return 0; }
    }

    private static void fail(Path path, int line, String message) {
        throw new IllegalArgumentException(path.getFileName() + ":" + line + ": " + message);
    }
}

