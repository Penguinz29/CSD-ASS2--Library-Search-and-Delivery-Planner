import model.Book;
import model.BookTree;
import model.BranchGraph;
import model.LibraryData;
import model.Sorts;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Dependency-free tests; Java's reference sort is used only by these checks. */
public final class TestRunner {
    private static int passed;
    private TestRunner() { }

    public static void main(String[] args) throws Exception {
        testTree();
        testGraph();
        testSorters();
        testInvalidLoadPreservesState(args.length == 0 ? "data" : args[0]);
        System.out.println("ALL TESTS PASSED: " + passed + " checks");
    }

    private static void testTree() {
        BookTree empty = new BookTree();
        check("BST empty state", empty.size() == 0 && empty.height() == 0 && empty.inorder().isEmpty());
        check("BST empty search/delete", empty.search("X") == null && !empty.delete("X"));
        BookTree singleton = new BookTree();
        singleton.insert(book("M", 1));
        check("BST singleton", singleton.size() == 1 && singleton.height() == 1
                && singleton.search("M") != null);
        boolean duplicateRejected = false;
        try { singleton.insert(book("M", 2)); }
        catch (IllegalArgumentException expected) { duplicateRejected = true; }
        check("BST duplicate rejected", duplicateRejected && singleton.search("M").getCopies() == 1);

        BookTree leaf = tree("M", "F", "T");
        check("BST delete leaf", leaf.delete("F") && ids(leaf.inorder()).equals(Arrays.asList("M", "T")));
        BookTree oneChild = tree("M", "C", "B");
        check("BST delete one child", oneChild.delete("C")
                && ids(oneChild.inorder()).equals(Arrays.asList("B", "M")));
        BookTree twoChildren = tree("M", "C", "T", "B", "D");
        check("BST delete two children", twoChildren.delete("C")
                && ids(twoChildren.inorder()).equals(Arrays.asList("B", "D", "M", "T")));
        BookTree degenerate = tree("A", "B", "C", "D", "E");
        check("BST inorder and degenerate height", ids(degenerate.inorder())
                .equals(Arrays.asList("A", "B", "C", "D", "E")) && degenerate.height() == 5);
    }

    private static void testGraph() {
        BranchGraph graph = sampleGraph(true);
        List<String> bfs = graph.breadthFirstSearch("A");
        check("BFS reachable component without repeats", bfs.equals(Arrays.asList("A", "B", "C", "D"))
                && new java.util.HashSet<String>(bfs).size() == bfs.size() && !bfs.contains("E"));
        BranchGraph.RouteResult self = graph.shortestRoute("A", "A");
        check("Dijkstra source equals destination", self.isReachable()
                && self.getPath().equals(Collections.singletonList("A")) && self.getDistance() == 0L);
        BranchGraph.RouteResult competing = graph.shortestRoute("A", "D");
        check("Dijkstra competing routes", competing.isReachable()
                && competing.getPath().equals(Arrays.asList("A", "C", "B", "D"))
                && competing.getDistance() == 4L);
        BranchGraph.RouteResult unreachable = graph.shortestRoute("A", "E");
        check("Dijkstra unreachable destination", !unreachable.isReachable());
        BranchGraph.MstResult connected = sampleGraph(false).minimumSpanningTree();
        check("Prim connected graph", connected.hasSpanningTree() && connected.getEdges().size() == 3
                && connected.getTotalDistance() == 4L);
        BranchGraph.MstResult disconnected = graph.minimumSpanningTree();
        check("Prim disconnected graph", !disconnected.hasSpanningTree()
                && disconnected.getEdges().isEmpty());
    }

    private static void testSorters() {
        for (Sorts.Algorithm algorithm : Sorts.Algorithm.values()) {
            assertSort(algorithm, new Book[0], "empty");
            assertSort(algorithm, new Book[] { book("ONE", 4) }, "singleton");
            assertSort(algorithm, new Book[] { book("C", 8), book("A", 8), book("B", 8) }, "equal copies");
            assertSort(algorithm, new Book[] { book("A", 1), book("B", 2), book("C", 3) }, "sorted");
            assertSort(algorithm, new Book[] { book("C", 3), book("B", 2), book("A", 1) }, "reverse sorted");
        }
    }

    private static void assertSort(Sorts.Algorithm algorithm, Book[] input, String label) {
        Book[] expected = input.clone();
        Arrays.sort(expected, Sorts::compareBooks);
        Book[] actual = input.clone();
        Sorts.Metrics metrics = new Sorts.Metrics();
        Sorts.sort(actual, algorithm, metrics);
        check(algorithm + " " + label + " and preserves records", Arrays.equals(actual, expected)
                && Sorts.isSortedAndPreserves(input, actual));
    }

    private static void testInvalidLoadPreservesState(String dataRoot) throws IOException {
        LibraryData store = new LibraryData();
        store.loadAndReplace(Paths.get(dataRoot, "valid"));
        LibraryData.State previous = store.getCurrent();
        boolean rejected = false;
        try { store.loadAndReplace(Paths.get(dataRoot, "invalid", "unknown_branch")); }
        catch (IllegalArgumentException expected) { rejected = expected.getMessage().contains("routes.csv:2"); }
        check("invalid input rejected with line number and old state retained",
                rejected && store.getCurrent() == previous && store.getCurrent().getBooks().size() >= 20);
    }

    private static BranchGraph sampleGraph(boolean includeIsolated) {
        List<String> ids = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
        if (includeIsolated) ids.add("E");
        List<BranchGraph.Edge> edges = Arrays.asList(new BranchGraph.Edge("A", "B", 4),
                new BranchGraph.Edge("A", "C", 1), new BranchGraph.Edge("C", "B", 2),
                new BranchGraph.Edge("B", "D", 1), new BranchGraph.Edge("C", "D", 5));
        return new BranchGraph(ids, edges);
    }

    private static BookTree tree(String... ids) {
        BookTree tree = new BookTree();
        for (String id : ids) tree.insert(book(id, 0));
        return tree;
    }

    private static Book book(String id, int copies) { return new Book(id, "Title " + id, "Test", copies); }

    private static List<String> ids(List<Book> books) {
        List<String> result = new ArrayList<String>();
        for (Book book : books) result.add(book.getBookId());
        return result;
    }

    private static void check(String name, boolean condition) {
        if (!condition) throw new AssertionError("FAILED: " + name);
        passed++;
        System.out.println("PASS: " + name);
    }
}
