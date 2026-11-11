import model.Book;
import model.BookTree;
import model.BranchGraph;
import model.LibraryData;
import model.Sorts;
import util.Constants;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class that runs a series of tests on the BookTree, BranchGraph, and
 * Sorts classes.
 */
public final class TestRunner {
    private static int passed;

    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        testTree();
        testGraph();
        testSorters();
        testInvalidLoadPreservesState(args.length == 0 ? Constants.DEFAULT_TEST_DATA_DIR : args[0]);
        System.out.println("ALL TESTS PASSED: " + passed + " checks");
    }

    /**
     * Tests the functionality of the BookTree class.
     */
    private static void testTree() {
        BookTree empty = new BookTree();
        check("BST empty state", empty.size() == 0 && empty.height() == 0 && empty.inorder().isEmpty());
        check("BST empty search/delete", empty.search("X") == null && !empty.delete("X"));

        // searching and deleting null should return null and false respectively,
        // without throwing an exception
        check("BST null search and delete", empty.search(null) == null && !empty.delete(null));

        BookTree singleton = new BookTree();
        singleton.insert(book("M", 1));
        check("BST singleton", singleton.size() == 1 && singleton.height() == 1 && singleton.search("M") != null);

        // delete root singleton should return true, and the tree should be empty
        // afterward
        check("BST delete root singleton", singleton.delete("M") && singleton.size() == 0 && singleton.height() == 0);

        // inserting a duplicate bookId should throw an IllegalArgumentException
        BookTree dupTree = new BookTree();
        dupTree.insert(book("M", 1));
        boolean duplicateRejected = false;
        try {
            dupTree.insert(book("M", 2));
        } catch (IllegalArgumentException expected) {
            duplicateRejected = true;
        }
        check("BST duplicate rejected", duplicateRejected && dupTree.search("M").getCopies() == 1);

        // remove leaf node
        BookTree leaf = tree("M", "F", "T");
        check("BST delete leaf", leaf.delete("F") && ids(leaf.inorder()).equals(Arrays.asList("M", "T")));

        // remove root with one child
        BookTree oneLeftChild = tree("M", "C", "B");
        check("BST delete one left child",
                oneLeftChild.delete("C") && ids(oneLeftChild.inorder()).equals(Arrays.asList("B", "M")));

        // remove root with one right child
        BookTree oneRightChild = tree("M", "C", "D");
        check("BST delete one right child",
                oneRightChild.delete("C") && ids(oneRightChild.inorder()).equals(Arrays.asList("D", "M")));

        // remove node with two children, where the successor is the direct right child
        BookTree twoChildren = tree("M", "C", "T", "B", "D");
        check("BST delete two children direct successor", twoChildren.delete("C")
                && ids(twoChildren.inorder()).equals(Arrays.asList("B", "D", "M", "T")));

        // remove node with two children, where the successor is deeper in the right
        // subtree
        BookTree deepSuccessorTree = tree("C", "A", "F", "E", "G");
        check("BST delete node with deep successor", deepSuccessorTree.delete("C")
                && ids(deepSuccessorTree.inorder()).equals(Arrays.asList("A", "E", "F", "G")));

        // remove root with two children
        check("BST delete root with two children", twoChildren.delete("M")
                && ids(twoChildren.inorder()).equals(Arrays.asList("B", "D", "T")));

        // remove non-existent key should return false and not change the size of the
        // tree
        check("BST delete non-existent key", !twoChildren.delete("NOT_FOUND") && twoChildren.size() == 3);

        // check inorder traversal of a degenerate tree (linked list) and its height
        BookTree degenerate = tree("A", "B", "C", "D", "E");
        check("BST inorder and degenerate height", ids(degenerate.inorder())
                .equals(Arrays.asList("A", "B", "C", "D", "E")) && degenerate.height() == 5);
    }

    /**
     * Tests the functionality of the BranchGraph class, including connectivity,
     * shortest paths, and minimum spanning trees.
     */
    private static void testGraph() {
        // check disconnected graph with 5 vertices and 5 edges, including an isolated
        // vertex E
        BranchGraph graph = sampleGraph(true);
        check("Graph vertex/edge count and disconnected status",
                graph.vertexCount() == 5 && graph.edgeCount() == 5 && !graph.isConnected());

        // check connected graph with 4 vertices and 5 edges, no isolated vertices
        BranchGraph connectedGraph = sampleGraph(false);
        check("Connected graph vertex/edge count and status",
                connectedGraph.isConnected() && connectedGraph.vertexCount() == 4 && connectedGraph.edgeCount() == 5);

        // validate that the graph rejects self-loops by attempting to create a graph
        // with an edge from A to A
        boolean invalidGraphRejected = false;
        try {
            new BranchGraph(Arrays.asList("A", "B"), Arrays.asList(new BranchGraph.Edge("A", "A", 5)));
        } catch (IllegalArgumentException expected) {
            invalidGraphRejected = true;
        }
        check("Graph rejects self-loops", invalidGraphRejected);

        // bfs reachable component without repeats (should not include isolated vertex
        // E)
        List<String> bfs = graph.breadthFirstSearch("A");
        check("BFS reachable component without repeats", bfs.equals(Arrays.asList("A", "B", "C", "D"))
                && new java.util.HashSet<String>(bfs).size() == bfs.size() && !bfs.contains("E"));

        // dijkstra source equals destination
        BranchGraph.RouteResult self = graph.shortestRoute("A", "A");
        check("Dijkstra source equals destination", self.isReachable()
                && self.getPath().equals(Collections.singletonList("A")) && self.getDistance() == 0L);

        // dijkstra competing routes (A -> D) should return the shortest path A -> C ->
        // B -> D with distance 4
        BranchGraph.RouteResult competing = graph.shortestRoute("A", "D");
        check("Dijkstra competing routes", competing.isReachable()
                && competing.getPath().equals(Arrays.asList("A", "C", "B", "D"))
                && competing.getDistance() == 4L);

        // dijkstra unreachable destination
        BranchGraph.RouteResult unreachable = graph.shortestRoute("A", "E");
        check("Dijkstra unreachable destination", !unreachable.isReachable());

        // prim MSt on connected graph should return a spanning tree with 3 edges and
        // total distance 4
        BranchGraph.MstResult connected = connectedGraph.minimumSpanningTree();
        check("Prim connected graph", connected.hasSpanningTree() && connected.getEdges().size() == 3
                && connected.getTotalDistance() == 4L);

        // prim MST on disconnected graph should return no spanning tree and an empty
        // edge list
        BranchGraph.MstResult disconnected = graph.minimumSpanningTree();
        check("Prim disconnected graph", !disconnected.hasSpanningTree() && disconnected.getEdges().isEmpty());
    }

    /**
     * Tests the sorting functionality of various algorithms.
     */
    private static void testSorters() {
        for (Sorts.Algorithm algorithm : Sorts.Algorithm.values()) {
            assertSort(algorithm, new Book[0], "empty");
            assertSort(algorithm, new Book[] { book("ONE", 4) }, "singleton");
            assertSort(algorithm, new Book[] { book("C", 8), book("A", 8), book("B", 8) }, "equal copies");
            assertSort(algorithm, new Book[] { book("A", 1), book("B", 2), book("C", 3) }, "sorted");
            assertSort(algorithm, new Book[] { book("C", 3), book("B", 2), book("A", 1) }, "reverse sorted");

            // test with identical elements to ensure stability of the sorting algorithm
            assertSort(algorithm, new Book[] { book("SAME", 5), book("SAME", 5), book("SAME", 5) },
                    "identical elements");
        }
    }

    /**
     * Asserts that the given sorting algorithm produces the expected output.
     */
    private static void assertSort(Sorts.Algorithm algorithm, Book[] input, String label) {
        Book[] expected = input.clone();
        Arrays.sort(expected, Sorts::compareBooks);
        Book[] actual = input.clone();
        Sorts.Metrics metrics = new Sorts.Metrics();
        Sorts.sort(actual, algorithm, metrics);
        check(algorithm + " " + label + " and preserves records", Arrays.equals(actual, expected)
                && Sorts.isSortedAndPreserves(input, actual));
    }

    /**
     * Tests that loading invalid data into the LibraryData store does not change
     * its state
     * and throws an exception with the correct line number.
     */
    private static void testInvalidLoadPreservesState(String dataRoot) throws IOException {
        LibraryData store = new LibraryData();
        store.loadAndReplace(Paths.get(dataRoot, "valid"));
        LibraryData.State previous = store.getCurrent();
        boolean rejected = false;
        try {
            store.loadAndReplace(Paths.get(dataRoot, "invalid", "unknown_branch"));
        } catch (IllegalArgumentException expected) {
            rejected = expected.getMessage().contains("routes.csv:2");
        }
        check("invalid input rejected with line number and old state retained",
                rejected && store.getCurrent() == previous && store.getCurrent().getBooks().size() >= 20);
    }

    /**
     * Creates a sample graph for testing purposes.
     * 
     * @param includeIsolated whether to include an isolated vertex in the graph
     * @return a BranchGraph instance representing the sample graph
     */
    private static BranchGraph sampleGraph(boolean includeIsolated) {
        List<String> ids = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
        if (includeIsolated)
            ids.add("E");
        List<BranchGraph.Edge> edges = Arrays.asList(
                new BranchGraph.Edge("A", "B", 4),
                new BranchGraph.Edge("A", "C", 1),
                new BranchGraph.Edge("C", "B", 2),
                new BranchGraph.Edge("B", "D", 1),
                new BranchGraph.Edge("C", "D", 5));
        return new BranchGraph(ids, edges);
    }

    /**
     * Creates a sample book tree for testing purposes.
     * 
     * @param ids the IDs of the books to include in the tree
     * @return a BookTree instance representing the sample tree
     */
    private static BookTree tree(String... ids) {
        BookTree tree = new BookTree();
        for (String id : ids)
            tree.insert(book(id, 0));
        return tree;
    }

    /**
     * Creates a sample book for testing purposes.
     * 
     * @param id     the ID of the book
     * @param copies the number of copies available
     * @return a Book instance representing the sample book
     */
    private static Book book(String id, int copies) {
        return new Book(id, "Title " + id, "Test", copies);
    }

    /**
     * Extracts the IDs of a list of books.
     * 
     * @param books the list of books
     * @return a list of book IDs
     */
    private static List<String> ids(List<Book> books) {
        List<String> result = new ArrayList<String>();
        for (Book book : books)
            result.add(book.getBookId());
        return result;
    }

    /**
     * Checks a condition and throws an assertion error if it is false.
     * 
     * @param name      the name of the test
     * @param condition the condition to check
     */
    private static void check(String name, boolean condition) {
        if (!condition)
            throw new AssertionError("FAILED: " + name);
        passed++;
        System.out.println("PASS: " + name);
    }
}
