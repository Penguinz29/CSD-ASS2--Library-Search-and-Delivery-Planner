package view;

import model.Book;
import model.BranchGraph;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * ConsoleView – responsible only for displaying output and reading raw user input.
 * No business logic lives here; it delegates all data decisions to LibraryController.
 *
 * Uses ASCII box-drawing characters for a neat tabular layout.
 */
public final class ConsoleView {
    // ── Box-drawing characters (ASCII-safe for all terminals) ─────────────────
    private static final char TL = '+';
    private static final char TR = '+';
    private static final char BL = '+';
    private static final char BR = '+';
    private static final char H  = '-';
    private static final char V  = '|';
    private static final char ML = '+';
    private static final char MR = '+';
    private static final char MT = '+';
    private static final char MB = '+';
    private static final char MC = '+';

    // Column widths for the book table
    private static final int W_ID    = 6;
    private static final int W_TITLE = 32;
    private static final int W_CAT   = 12;
    private static final int W_COPY  = 6;
    // Total inner width = W_ID + W_TITLE + W_CAT + W_COPY + (3 separators of 3 chars each) = 6+32+12+6+9 = 65
    private static final int TABLE_INNER = W_ID + W_TITLE + W_CAT + W_COPY + 9;

    private final Scanner scanner;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    public void printBanner(int bookCount, int branchCount) {
        int w = MENU_W;
        String title = "Library Search & Delivery Planner";
        String sub   = "Books: " + bookCount + "   |   Branches: " + branchCount;
        System.out.println(TL + repeat(H, w) + TR);
        System.out.println(V + center(title, w) + V);
        System.out.println(V + center(sub,   w) + V);
        System.out.println(BL + repeat(H, w) + BR);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    // Menu inner width = 44 chars (between the two | pipes)
    private static final int MENU_W = 44;

    public void printMenu() {
        System.out.println();
        printMenuBorder(TL, TR);
        printMenuLine("[1] List books (BST inorder)");
        printMenuLine("[2] Search book by ID");
        printMenuLine("[3] Delete book by ID");
        printMenuLine("[4] BST statistics");
        printMenuBorder(ML, MR);
        printMenuLine("[5] Show branch graph (adjacency list)");
        printMenuLine("[6] BFS traversal from branch");
        printMenuLine("[7] Shortest route (Dijkstra)");
        printMenuLine("[8] Minimum Spanning Tree (Prim)");
        printMenuBorder(ML, MR);
        printMenuLine("[9] Sort books");
        printMenuLine("[0] Exit");
        printMenuBorder(BL, BR);
        System.out.print("  Choice: ");
    }

    private void printMenuBorder(char left, char right) {
        System.out.println(left + repeat(H, MENU_W) + right);
    }

    private void printMenuLine(String text) {
        System.out.println(V + "  " + padRight(text, MENU_W - 2) + V);
    }

    // ── Prompts ───────────────────────────────────────────────────────────────

    public String readLine() {
        return scanner.nextLine().trim();
    }

    public String promptBookId() {
        System.out.print("  Enter Book ID: ");
        return readLine();
    }

    public String promptBranchId(String label) {
        System.out.print("  " + capitalize(label) + " Branch ID: ");
        return readLine();
    }

    public String promptAlgorithm() {
        System.out.println();
        System.out.println(TL + repeat(H, MENU_W) + TR);
        System.out.println(V + center("Select Sorting Algorithm", MENU_W) + V);
        System.out.println(ML + repeat(H, MENU_W) + MR);
        printMenuLine("[1] Quick Sort");
        printMenuLine("[2] Merge Sort");
        printMenuLine("[3] Heap Sort");
        printMenuLine("[4] Insertion Sort");
        printMenuBorder(BL, BR);
        System.out.print("  Choice (1-4): ");
        return readLine();
    }

    // ── Book table ────────────────────────────────────────────────────────────

    public void showBooks(List<Book> books) {
        printBookTableHeader();
        if (books.isEmpty()) {
            System.out.println(V + center("(no books)", TABLE_INNER) + V);
        } else {
            for (int i = 0; i < books.size(); i++) {
                printBookRow(books.get(i), i + 1);
                if (i < books.size() - 1) printBookRowSeparator();
            }
        }
        printBookTableFooter();
    }

    public void showBookFound(Book book) {
        if (book == null) {
            printInfoBox("NOT FOUND", false);
        } else {
            printBookTableHeader();
            printBookRow(book, 1);
            printBookTableFooter();
        }
    }

    public void showDeleteResult(boolean deleted) {
        if (deleted) printInfoBox("Book deleted successfully.", true);
        else         printInfoBox("NOT FOUND – no book with that ID.", false);
    }

    public void showBstStats(int size, int height) {
        System.out.println();
        System.out.println(TL + repeat(H, MENU_W) + TR);
        System.out.println(V + center("BST Statistics", MENU_W) + V);
        System.out.println(ML + repeat(H, MENU_W) + MR);
        System.out.printf( V + "  %-26s %12d  " + V + "%n", "Node count:", size);
        System.out.printf( V + "  %-26s %12d  " + V + "%n", "Height (nodes):", height);
        System.out.println(V + "  " + padRight("(height: empty=0, leaf=1)", MENU_W - 2) + V);
        System.out.println(BL + repeat(H, MENU_W) + BR);
    }

    public void showSortResult(List<Book> sorted, long comparatorCalls, long arrayWrites) {
        showBooks(sorted);
        System.out.println();
        System.out.println(TL + repeat(H, MENU_W) + TR);
        System.out.println(V + center("Sort Metrics", MENU_W) + V);
        System.out.println(ML + repeat(H, MENU_W) + MR);
        System.out.printf( V + "  %-26s %12d  " + V + "%n", "Comparator calls:", comparatorCalls);
        System.out.printf( V + "  %-26s %12d  " + V + "%n", "Array writes:", arrayWrites);
        System.out.println(BL + repeat(H, MENU_W) + BR);
    }

    // ── Graph output ──────────────────────────────────────────────────────────

    public void showGraph(Map<String, List<BranchGraph.Neighbor>> adjacency) {
        System.out.println();
        int innerW = 52;
        System.out.println(TL + repeat(H, innerW) + TR);
        System.out.println(V + center("Branch Graph – Adjacency List", innerW) + V);
        System.out.println(ML + repeat(H, innerW) + MR);
        for (Map.Entry<String, List<BranchGraph.Neighbor>> entry : adjacency.entrySet()) {
            StringBuilder nb = new StringBuilder();
            List<BranchGraph.Neighbor> neighbors = entry.getValue();
            if (neighbors.isEmpty()) {
                nb.append("(isolated)");
            } else {
                for (int i = 0; i < neighbors.size(); i++) {
                    if (i > 0) nb.append(", ");
                    BranchGraph.Neighbor n = neighbors.get(i);
                    nb.append(n.getBranchId()).append("(d=").append(n.getDistance()).append(")");
                }
            }
            String row = "  " + padRight(entry.getKey(), 6) + " -> " + nb.toString();
            // wrap if too long
            if (row.length() > innerW) row = row.substring(0, innerW - 2) + "..";
            System.out.println(V + padRight(row, innerW) + V);
        }
        System.out.println(BL + repeat(H, innerW) + BR);
    }

    public void showBfsResult(List<String> order) {
        System.out.println();
        System.out.println(TL + repeat(H, 50) + TR);
        System.out.println(V + center("BFS Traversal Order", 50) + V);
        System.out.println(ML + repeat(H, 50) + MR);
        System.out.println(V + "  " + padRight(joinStrings(order, " -> "), 48) + V);
        System.out.println(BL + repeat(H, 50) + BR);
    }

    public void showRouteResult(BranchGraph.RouteResult result) {
        System.out.println();
        System.out.println(TL + repeat(H, 50) + TR);
        System.out.println(V + center("Shortest Route (Dijkstra)", 50) + V);
        System.out.println(ML + repeat(H, 50) + MR);
        if (!result.isReachable()) {
            System.out.println(V + center("NO ROUTE FOUND", 50) + V);
        } else {
            String path = joinStrings(result.getPath(), " -> ");
            System.out.println(V + "  " + padRight("Path: " + path, 48) + V);
            System.out.println(V + "  " + padRight("Distance: " + result.getDistance(), 48) + V);
        }
        System.out.println(BL + repeat(H, 50) + BR);
    }

    public void showMstResult(BranchGraph.MstResult result) {
        System.out.println();
        System.out.println(TL + repeat(H, 36) + TR);
        System.out.println(V + center("Minimum Spanning Tree (Prim)", 36) + V);
        System.out.println(ML + repeat(H, 36) + MR);
        if (!result.hasSpanningTree()) {
            System.out.println(V + center("NO SPANNING TREE", 36) + V);
        } else {
            // sub-header
            System.out.printf(V + "  %-10s  %-10s  %8s  " + V + "%n", "From", "To", "Distance");
            System.out.println(ML + repeat(H, 36) + MR);
            for (BranchGraph.Edge edge : result.getEdges()) {
                System.out.printf(V + "  %-10s  %-10s  %8d  " + V + "%n",
                        edge.getFrom(), edge.getTo(), edge.getDistance());
            }
            System.out.println(ML + repeat(H, 36) + MR);
            System.out.printf(V + "  %-22s %10d  " + V + "%n", "Total distance:", result.getTotalDistance());
        }
        System.out.println(BL + repeat(H, 36) + BR);
    }

    // ── Info / Error ──────────────────────────────────────────────────────────

    public void showLoadSuccess(int bookCount, int branchCount) {
        printBanner(bookCount, branchCount);
    }

    public void showError(String message) {
        System.out.println();
        System.out.println(TL + repeat(H, 46) + TR);
        System.out.println(V + center("! ERROR", 46) + V);
        System.out.println(ML + repeat(H, 46) + MR);
        String msg = "  " + message;
        if (msg.length() > 46) msg = msg.substring(0, 43) + "...";
        System.out.println(V + padRight(msg, 46) + V);
        System.out.println(BL + repeat(H, 46) + BR);
    }

    public void showLoadError(String message) {
        System.err.println("[LOAD ERROR] " + message);
    }

    public void showUnknownChoice() {
        System.out.println("  Unknown choice. Please enter 0-9.");
    }

    // ── Book table internals ───────────────────────────────────────────────────

    private void printBookTableHeader() {
        System.out.println();
        // Top border
        System.out.println(TL + repeat(H, W_ID + 2) + MT
                + repeat(H, W_TITLE + 2) + MT
                + repeat(H, W_CAT   + 2) + MT
                + repeat(H, W_COPY  + 2) + TR);
        // Column headers
        System.out.println(V + " " + padRight("ID", W_ID) + " " + V
                + " " + padRight("Title", W_TITLE) + " " + V
                + " " + padRight("Category", W_CAT) + " " + V
                + " " + padRight("Copies", W_COPY) + " " + V);
        // Header divider
        System.out.println(ML + repeat(H, W_ID + 2) + MC
                + repeat(H, W_TITLE + 2) + MC
                + repeat(H, W_CAT   + 2) + MC
                + repeat(H, W_COPY  + 2) + MR);
    }

    private void printBookRow(Book book, int index) {
        String title = truncate(book.getTitle(), W_TITLE);
        String cat   = truncate(book.getCategory(), W_CAT);
        System.out.println(V + " " + padRight(book.getBookId(), W_ID) + " " + V
                + " " + padRight(title, W_TITLE) + " " + V
                + " " + padRight(cat,   W_CAT)   + " " + V
                + " " + padLeft(String.valueOf(book.getCopies()), W_COPY) + " " + V);
    }

    private void printBookRowSeparator() {
        System.out.println(ML + repeat(H, W_ID + 2) + MC
                + repeat(H, W_TITLE + 2) + MC
                + repeat(H, W_CAT   + 2) + MC
                + repeat(H, W_COPY  + 2) + MR);
    }

    private void printBookTableFooter() {
        System.out.println(BL + repeat(H, W_ID + 2) + MB
                + repeat(H, W_TITLE + 2) + MB
                + repeat(H, W_CAT   + 2) + MB
                + repeat(H, W_COPY  + 2) + BR);
    }

    private void printInfoBox(String message, boolean success) {
        String label = success ? "OK" : "!!";
        System.out.println();
        System.out.println(TL + repeat(H, 42) + TR);
        System.out.println(V + "  [" + label + "] " + padRight(message, 37) + V);
        System.out.println(BL + repeat(H, 42) + BR);
    }

    // ── String utilities ───────────────────────────────────────────────────────

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(ch);
        return sb.toString();
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private static String padLeft(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        StringBuilder sb = new StringBuilder();
        while (sb.length() + s.length() < width) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        int total = width - s.length();
        int left  = total / 2;
        int right = total - left;
        return repeat(' ', left) + s + repeat(' ', right);
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 2) + "..";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private static String joinStrings(List<String> values, String separator) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) result.append(separator);
            result.append(value);
        }
        return result.toString();
    }
}
