package view;

import model.Book;
import model.BranchGraph;
import util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * this class is used for printing the output and reading the input from the
 * user
 */
public final class ConsoleView {
    private final Scanner scanner;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    public void printBanner(int bookCount, int branchCount) {
        System.out.println("\tLibrary Search & Delivery Planner");
        System.out.println("Loaded " + bookCount + " books and " + branchCount + " branches.");
    }

    public void printMenu() {
        System.out.println();
        System.out.println(Constants.MENU_LINE);
        System.out.println("| 1. List books (BST inorder)   | 6. BFS traversal         |");
        System.out.println("| 2. Search book by ID          | 7. Shortest route        |");
        System.out.println("| 3. Delete book by ID          | 8. Minimum Spanning Tree |");
        System.out.println("| 4. BST statistics             | 9. Sort books            |");
        System.out.println("| 5. Show branch graph          | 0. Exit                  |");
        System.out.println(Constants.MENU_LINE);
        System.out.print("Choice: ");
    }

    public String readLine() {
        return scanner.nextLine().trim();
    }

    public String promptBookId() {
        System.out.print("Enter Book ID: ");
        return readLine();
    }

    public String promptBranchId(String label) {
        System.out.print(capitalize(label) + " Branch ID: ");
        return readLine();
    }

    public String promptAlgorithm() {
        System.out.println();
        System.out.println("Select sorting algorithm:");
        System.out.println("1. Quick Sort \n2. Merge Sort \n3. Heap Sort \n4. Insertion Sort ");
        System.out.print("Choice (1-4): ");
        return readLine();
    }

    public void showBooks(List<Book> books) {
        printBookHeader();
        if (books.isEmpty()) {
            System.out.printf("| %-65s |%n", "(no books)");
        } else {
            for (Book book : books) {
                printBookRow(book);
            }
        }
        System.out.println(Constants.BOOK_TABLE_LINE);
    }

    public void showBookFound(Book book) {
        if (book == null) {
            System.out.println("NOT FOUND");
        } else {
            printBookHeader();
            printBookRow(book);
            System.out.println(Constants.BOOK_TABLE_LINE);
        }
    }

    public void showDeleteResult(boolean deleted) {
        System.out.println(deleted ? "Deleted." : "NOT FOUND");
    }

    public void showBstStats(int size, int height) {
        System.out.println();
        System.out.println(Constants.STATS_TABLE_LINE);
        System.out.printf("| %-16s | %13d |%n", "Node count", size);
        System.out.printf("| %-16s | %13d |%n", "Height (nodes)", height);
        System.out.println(Constants.STATS_TABLE_LINE);
        System.out.println("(height: empty=0, leaf=1)");
    }

    public void showSortResult(List<Book> sorted, long comparatorCalls, long arrayWrites) {
        showBooks(sorted);
        System.out.println(Constants.STATS_TABLE_LINE);
        System.out.printf("| %-16s | %13d |%n", "Comparator calls", comparatorCalls);
        System.out.printf("| %-16s | %13d |%n", "Array writes", arrayWrites);
        System.out.println(Constants.STATS_TABLE_LINE);
    }

    // ── Graph output ──────────────────────────────────────────────────────────

    public void showGraph(Map<String, List<BranchGraph.Neighbor>> adjacency) {
        System.out.println();
        System.out.println(Constants.MENU_LINE);
        System.out.printf("| %-" + Constants.COL_WIDTH_BRANCH + "s | %-"
                + Constants.COL_WIDTH_NEIGHBORS + "s |%n", "Branch", "Neighbors (Distance)");
        System.out.println(Constants.MENU_LINE);
        for (Map.Entry<String, List<BranchGraph.Neighbor>> entry : adjacency.entrySet()) {
            StringBuilder nb = new StringBuilder();
            List<BranchGraph.Neighbor> neighbors = entry.getValue();
            if (neighbors.isEmpty()) {
                nb.append("(isolated)");
            } else {
                for (int i = 0; i < neighbors.size(); i++) {
                    if (i > 0) {
                        nb.append(", ");
                    }
                    BranchGraph.Neighbor n = neighbors.get(i);
                    nb.append(n.getBranchId()).append("(").append(n.getDistance()).append(")");
                }
            }
            System.out.printf("| %-" + Constants.COL_WIDTH_BRANCH + "s | %-"
                    + Constants.COL_WIDTH_NEIGHBORS + "s |%n",
                    entry.getKey(), truncate(nb.toString(), Constants.COL_WIDTH_NEIGHBORS));
        }
        System.out.println(Constants.MENU_LINE);
    }

    public void showBfsResult(List<String> order) {
        System.out.println("BFS: " + joinStrings(order, " -> "));
    }

    public void showRouteResult(BranchGraph.RouteResult result) {
        if (result.isReachable()) {
            System.out.println("Route: " + joinStrings(result.getPath(), " -> ")
                    + " (distance " + result.getDistance() + ")");
        } else {
            System.out.println("NO ROUTE");
        }
    }

    public void showMstResult(BranchGraph.MstResult result) {
        if (!result.hasSpanningTree()) {
            System.out.println("NO SPANNING TREE");
            return;
        }
        System.out.println();
        System.out.println(Constants.STATS_TABLE_LINE);
        System.out.printf("| %-8s | %-8s | %10s |%n", "From", "To", "Distance");
        System.out.println(Constants.STATS_TABLE_LINE);
        for (BranchGraph.Edge edge : result.getEdges()) {
            System.out.printf("| %-8s | %-8s | %10d |%n",
                    edge.getFrom(), edge.getTo(), edge.getDistance());
        }
        System.out.println(Constants.STATS_TABLE_LINE);
        System.out.println("Total distance = " + result.getTotalDistance());
    }

    // ── Info / Error ──────────────────────────────────────────────────────────

    public void showLoadSuccess(int bookCount, int branchCount) {
        printBanner(bookCount, branchCount);
    }

    public void showError(String message) {
        System.out.println("Invalid input: " + message);
    }

    public void showLoadError(String message) {
        System.err.println("Could not load data: " + message);
    }

    public void showUnknownChoice() {
        System.out.println("Unknown choice. Please enter 0-9.");
    }


    private void printBookHeader() {
        System.out.println();
        System.out.println(Constants.BOOK_TABLE_LINE);
        System.out.printf("| %-" + Constants.COL_WIDTH_ID + "s | %-"
                + Constants.COL_WIDTH_TITLE + "s | %-"
                + Constants.COL_WIDTH_CATEGORY + "s | %"
                + Constants.COL_WIDTH_COPIES + "s |%n",
                "ID", "Title", "Category", "Copies");
        System.out.println(Constants.BOOK_TABLE_LINE);
    }

    private void printBookRow(Book book) {
        System.out.printf("| %-" + Constants.COL_WIDTH_ID + "s | %-"
                + Constants.COL_WIDTH_TITLE + "s | %-"
                + Constants.COL_WIDTH_CATEGORY + "s | %-"
                + Constants.COL_WIDTH_COPIES + "d |%n",
                truncate(book.getBookId(), Constants.COL_WIDTH_ID),
                truncate(book.getTitle(), Constants.COL_WIDTH_TITLE),
                truncate(book.getCategory(), Constants.COL_WIDTH_CATEGORY),
                book.getCopies());
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 2) + "..";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private static String joinStrings(List<String> values, String separator) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(value);
        }
        return result.toString();
    }
}
