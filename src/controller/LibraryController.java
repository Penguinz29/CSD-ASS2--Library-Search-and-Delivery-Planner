package controller;

import model.Book;
import model.BookTree;
import model.BranchGraph;
import model.LibraryData;
import model.Sorts;
import view.ConsoleView;

import java.util.Arrays;
import java.util.List;

/**
 * LibraryController – bridges Model and View.
 * Reads user choices from ConsoleView, invokes model operations,
 * and passes results back to ConsoleView for display.
 */
public final class LibraryController {
    private final LibraryData.State state;
    private final ConsoleView view;

    // Local mutable tree (supports delete during this session)
    private final BookTree tree;
    private final BranchGraph graph;

    public LibraryController(LibraryData.State state, ConsoleView view) {
        this.state = state;
        this.view = view;
        this.tree = state.getBookTree();
        this.graph = state.getGraph();
    }

    /** Main event loop. */
    public void run() {
        while (true) {
            view.printMenu();
            String choice = view.readLine();
            try {
                if ("0".equals(choice)) return;
                handleChoice(choice);
            } catch (IllegalArgumentException ex) {
                view.showError(ex.getMessage());
            }
        }
    }

    // ── Choice dispatcher ──────────────────────────────────────────────────────

    private void handleChoice(String choice) {
        if ("1".equals(choice)) {
            handleListBooks();
        } else if ("2".equals(choice)) {
            handleSearchBook();
        } else if ("3".equals(choice)) {
            handleDeleteBook();
        } else if ("4".equals(choice)) {
            handleBstStats();
        } else if ("5".equals(choice)) {
            handleShowGraph();
        } else if ("6".equals(choice)) {
            handleBfs();
        } else if ("7".equals(choice)) {
            handleShortestRoute();
        } else if ("8".equals(choice)) {
            handleMst();
        } else if ("9".equals(choice)) {
            handleSort();
        } else {
            view.showUnknownChoice();
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleListBooks() {
        view.showBooks(tree.inorder());
    }

    private void handleSearchBook() {
        String bookId = view.promptBookId();
        Book found = tree.search(bookId);
        view.showBookFound(found);
    }

    private void handleDeleteBook() {
        String bookId = view.promptBookId();
        boolean deleted = tree.delete(bookId);
        view.showDeleteResult(deleted);
    }

    private void handleBstStats() {
        view.showBstStats(tree.size(), tree.height());
    }

    private void handleShowGraph() {
        view.showGraph(graph.getAdjacency());
    }

    private void handleBfs() {
        String start = view.promptBranchId("start");
        List<String> order = graph.breadthFirstSearch(start);
        view.showBfsResult(order);
    }

    private void handleShortestRoute() {
        String from = view.promptBranchId("from");
        String to = view.promptBranchId("to");
        BranchGraph.RouteResult result = graph.shortestRoute(from, to);
        view.showRouteResult(result);
    }

    private void handleMst() {
        BranchGraph.MstResult result = graph.minimumSpanningTree();
        view.showMstResult(result);
    }

    private void handleSort() {
        String algName = view.promptAlgorithm();
        Sorts.Algorithm algorithm = parseAlgorithm(algName);
        Book[] copy = tree.inorder().toArray(new Book[0]);
        Sorts.Metrics metrics = new Sorts.Metrics();
        Sorts.sort(copy, algorithm, metrics);
        view.showSortResult(Arrays.asList(copy), metrics.getComparatorCalls(), metrics.getArrayWrites());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Sorts.Algorithm parseAlgorithm(String value) {
        // Accept numeric shortcut (1-4) or full name
        if ("1".equals(value) || "quick".equalsIgnoreCase(value))     return Sorts.Algorithm.QUICK;
        if ("2".equals(value) || "merge".equalsIgnoreCase(value))     return Sorts.Algorithm.MERGE;
        if ("3".equals(value) || "heap".equalsIgnoreCase(value))      return Sorts.Algorithm.HEAP;
        if ("4".equals(value) || "insertion".equalsIgnoreCase(value)) return Sorts.Algorithm.INSERTION;
        throw new IllegalArgumentException("choose 1 (quick), 2 (merge), 3 (heap), or 4 (insertion)");
    }
}

