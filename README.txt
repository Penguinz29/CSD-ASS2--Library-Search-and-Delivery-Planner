CSD201 Assignment 2: Library Search and Delivery Planner
Class: SE2072    
Group: AHK
Platform: Java 8 (JDK 8 or newer), zero external dependencies

1. COMPILATION INSTRUCTIONS (BUILD)
Run from the project root directory.

Windows PowerShell:
  if (!(Test-Path build)) { New-Item -ItemType Directory -Force build }
  javac -source 8 -target 8 -encoding UTF-8 -d build `
    src\util\Constants.java `
    src\model\Book.java src\model\BookTree.java src\model\BranchGraph.java `
    src\model\LibraryData.java src\model\Sorts.java `
    src\view\ConsoleView.java `
    src\controller\LibraryController.java `
    src\Main.java src\CompareRunner.java src\TestRunner.java

Linux / macOS (Bash):
  mkdir -p build
  javac -source 8 -target 8 -encoding UTF-8 -d build $(find src -name "*.java")


2. EXECUTION INSTRUCTIONS (RUN)
Run the interactive console application:

Windows PowerShell:
  java -cp build Main data\valid

Linux / macOS (Bash):
  java -cp build Main data/valid


3. TESTING INSTRUCTIONS (TEST)
Run the automated test runner (verifies BST, Graph, Sorters, and error rollback):

Windows PowerShell:
  java -cp build TestRunner data

Linux / macOS (Bash):
  java -cp build TestRunner data

Expected Output:
  ALL TESTS PASSED: 48 checks
  (Verified output recorded in results/test-output.txt)


4. BENCHMARK COMPARISON INSTRUCTIONS
Reproduce the 48 sorting experiments (4 algorithms x 3 sizes x 4 orders):

Windows PowerShell:
  java -cp build CompareRunner results\sorting.csv

Linux / macOS (Bash):
  java -cp build CompareRunner results/sorting.csv


5. TECHNICAL ASSUMPTIONS & SPECIFICATIONS
A. Binary Search Tree (BST):
   - Node-based height convention:
       * Empty tree: height = 0
       * Single leaf node: height = 1
       * General tree: height = 1 + max(height(left), height(right))
   - Duplicate Handling: Duplicate bookId values are explicitly rejected by
     throwing IllegalArgumentException; they are never overwritten silently.
   - Deletion Cases Handled: Leaf nodes (0 children), single child (1 child,
     left or right), and two children (replaced by in-order successor, i.e.,
     the minimum node of the right subtree).
   - Degenerate tree: Inserting pre-sorted IDs forms a degenerate linked list
     of height N with O(N) search/insert/delete complexity.

B. Branch Delivery Graph:
   - Representation: Undirected weighted graph using an Adjacency List
     (Map<String, List<Neighbor>> backed by LinkedHashMap).
   - Edge Distances: Strictly positive integers (> 0).
   - Self-loops (from == to) and duplicate undirected edges are strictly rejected.
   - Disconnected graphs: Disconnected route graphs are considered VALID input
     and are loaded into memory without throwing an error.
   - Dijkstra Shortest Path: Settles minimum-distance vertices. Ties between
     candidate vertices with equal distances are broken deterministically by
     choosing the lexicographically smaller branchId. If unreachable, outputs
     "NO ROUTE".
   - Prim's Minimum Spanning Tree (MST): Evaluates candidate edges by distance
     first, then by canonical endpoint IDs (u <= v lexicographically). If any
     branch remains unreached (disconnected graph), outputs "NO SPANNING TREE"
     (never labels a component tree as the MST of the whole graph).

C. Sorting Algorithms & Total Order:
   - 4 Algorithms: Quick Sort (Lomuto partition with last element as pivot to
     expose O(N^2) worst case on sorted data), Merge Sort (stable, out-of-place
     with temporary buffer of size N), Heap Sort (in-place max-heap sift-down),
     Insertion Sort (stable, adaptive O(N) on sorted data).
   - Shared Total Order Comparator:
       1. copies ascending (fewest to greatest).
       2. bookId ascending lexicographically (tie-breaker).
   - Non-Destructive Invariant: Sorters strictly operate on independent clones
     of the record array; the master catalogue in memory is never modified.
   - Operation Metrics:
       * comparator_calls: Number of invocations of the shared comparator.
       * array_writes: Number of array position writes (swaps count as 2 writes;
         Merge Sort counts writes to both the auxiliary buffer and destination).
       * Counters are reset before sorting starts; data generation and
         verification passes are excluded.
       * Wall-clock runtime is deliberately excluded as a primary ranking metric.

D. Transactional CSV Data Loading:
   - Atomic loading: books.csv, branches.csv, and routes.csv are read into
     temporary memory state. If any validation fails (file missing, column count
     mismatch, negative copies, invalid integer, duplicate key, or unknown branch),
     an IllegalArgumentException is thrown with the exact file name and line number
     (e.g., "routes.csv:2: unknown branch ID 'BX99'"), and the prior state is retained.

E. Zero External Library Policy:
   - 100% custom implementation of data structures and algorithms.
   - Standard Java library utilities like TreeMap, TreeSet, and Arrays.sort are
     strictly forbidden in algorithm code. Arrays.sort appears exclusively in
     TestRunner.java as a reference validation checker.
