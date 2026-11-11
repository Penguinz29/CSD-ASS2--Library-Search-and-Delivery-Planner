package controller;

import model.Book;
import model.BookTree;
import model.BranchGraph;
import model.LibraryData;
import model.Sorts;
import util.Constants;
import view.ConsoleView;

import java.util.Arrays;
import java.util.List;

/**
 * Thao khao AI
 * this class is used to control the flow of the program
 */
public final class LibraryController {
    private final ConsoleView view;
    private final BookTree tree;
    private final BranchGraph graph;

    public LibraryController(LibraryData.State state, ConsoleView view) {
        this.view = view;
        this.tree = state.getBookTree();
        this.graph = state.getGraph();
    }

    // main loop
    public void run() {
        while (true) {
            view.printMenu();
            String choice = view.readLine();
            try {
                if (Constants.MENU_EXIT.equals(choice)) {
                    return;
                }
                switch (choice) {
                    case Constants.MENU_LIST_BOOKS:
                        handleListBooks();
                        break;
                    case Constants.MENU_SEARCH_BOOK:
                        handleSearchBook();
                        break;
                    case Constants.MENU_DELETE_BOOK:
                        handleDeleteBook();
                        break;
                    case Constants.MENU_BST_STATS:
                        handleBstStats();
                        break;
                    case Constants.MENU_SHOW_GRAPH:
                        handleShowGraph();
                        break;
                    case Constants.MENU_BFS:
                        handleBfs();
                        break;
                    case Constants.MENU_SHORTEST_ROUTE:
                        handleShortestRoute();
                        break;
                    case Constants.MENU_MST:
                        handleMst();
                        break;
                    case Constants.MENU_SORT_BOOKS:
                        handleSort();
                        break;
                    default:
                        view.showUnknownChoice();
                        break;
                }
            } catch (IllegalArgumentException ex) {
                view.showError(ex.getMessage());
            }
        }
    }

    // this method handles the user's choice to list all books in the binary search
    // tree
    private void handleListBooks() {
        view.showBooks(tree.inorder());
    }

    // this method handles the user's choice to search for a book by its ID in the
    // binary search tree
    private void handleSearchBook() {
        String bookId = view.promptBookId();
        Book found = tree.search(bookId);
        view.showBookFound(found);
    }

    // this method handles the user's choice to delete a book by its ID in the
    // binary search tree
    private void handleDeleteBook() {
        String bookId = view.promptBookId();
        boolean deleted = tree.delete(bookId);
        view.showDeleteResult(deleted);
    }

    // this method handles the user's choice to show the statistics of the binary
    // search tree
    private void handleBstStats() {
        view.showBstStats(tree.size(), tree.height());
    }

    // this method handles the user's choice to show the graph
    private void handleShowGraph() {
        view.showGraph(graph.getAdjacency());
    }

    // this method handles the user's choice to perform a breadth-first search
    private void handleBfs() {
        String start = view.promptBranchId("start");
        List<String> order = graph.breadthFirstSearch(start);
        view.showBfsResult(order);
    }

    // this method handles the user's choice to find the shortest route between two
    // branches
    private void handleShortestRoute() {
        String from = view.promptBranchId("from");
        String to = view.promptBranchId("to");
        BranchGraph.RouteResult result = graph.shortestRoute(from, to);
        view.showRouteResult(result);
    }

    // this method handles the user's choice to find the minimum spanning tree
    private void handleMst() {
        BranchGraph.MstResult result = graph.minimumSpanningTree();
        view.showMstResult(result);
    }

    // this method handles the user's choice to sort the books
    private void handleSort() {
        String algName = view.promptAlgorithm();
        Sorts.Algorithm algorithm = parseAlgorithm(algName);
        Book[] copy = tree.inorder().toArray(new Book[0]);
        Sorts.Metrics metrics = new Sorts.Metrics();
        Sorts.sort(copy, algorithm, metrics);
        view.showSortResult(Arrays.asList(copy), metrics.getComparatorCalls(), metrics.getArrayWrites());
    }

    // this support method is used to parse the algorithm choice
    private static Sorts.Algorithm parseAlgorithm(String value) {
        if (Constants.SORT_CHOICE_QUICK.equals(value)
                || Constants.SORT_NAME_QUICK.equalsIgnoreCase(value)) {
            return Sorts.Algorithm.QUICK;
        }
        if (Constants.SORT_CHOICE_MERGE.equals(value)
                || Constants.SORT_NAME_MERGE.equalsIgnoreCase(value)) {
            return Sorts.Algorithm.MERGE;
        }
        if (Constants.SORT_CHOICE_HEAP.equals(value)
                || Constants.SORT_NAME_HEAP.equalsIgnoreCase(value)) {
            return Sorts.Algorithm.HEAP;
        }
        if (Constants.SORT_CHOICE_INSERTION.equals(value)
                || Constants.SORT_NAME_INSERTION.equalsIgnoreCase(value)) {
            return Sorts.Algorithm.INSERTION;
        }
        throw new IllegalArgumentException("choose 1 (quick), 2 (merge), 3 (heap), or 4 (insertion)");
    }
}
