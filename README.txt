CSD201 Assignment 2 - Library Search and Delivery Planner
Class: SE2072    Group: AHK

Requirements: Java 8 or newer JDK; no external libraries. All distances are positive integer units in a fictional network. BST height is measured in nodes: empty=0, leaf=1. Dijkstra and Prim break equal choices lexicographically by branch ID; Prim compares candidate edges by distance, then canonical endpoint IDs.

Source structure (MVC):
  src/
  ├── model/          (Model – data structures and algorithms)
  │   ├── Book.java
  │   ├── BookTree.java
  │   ├── BranchGraph.java
  │   ├── LibraryData.java
  │   └── Sorts.java
  ├── view/           (View – console I/O)
  │   └── ConsoleView.java
  ├── controller/     (Controller – event loop and dispatch)
  │   └── LibraryController.java
  ├── Main.java       (Entry point)
  ├── CompareRunner.java
  └── TestRunner.java

Build (run from this directory in Windows PowerShell):
  New-Item -ItemType Directory -Force build
  javac -source 8 -target 8 -encoding UTF-8 -d build `
    src\model\Book.java src\model\BookTree.java src\model\BranchGraph.java `
    src\model\LibraryData.java src\model\Sorts.java `
    src\view\ConsoleView.java `
    src\controller\LibraryController.java `
    src\Main.java src\CompareRunner.java src\TestRunner.java

Run the interactive application:
  java -cp build Main data\valid

Run the test runner:
  java -cp build TestRunner data

Recreate the 48 sorting comparisons:
  java -cp build CompareRunner results\sorting.csv

The comparison generator uses java.util.Random. Seed is 20261109; the random order uses seed + size. It creates unique IDs B00000 onward and copy counts in [0,1000]. The equal-copy scenario uses 42 copies and a seeded shuffle. Each algorithm receives a fresh clone of the same scenario array. Counters start inside the sorter after input cloning; verification and generation are excluded. Comparator calls count calls to the shared total-order comparator. Array writes count each assignment to a record array position, including merge temporary-array assignments; every swap writes two positions.

The result CSV contains all 48 combinations and correctness flags. Runtime was not used. results/test-output.txt records the dependency-free test run. Java Arrays.sort appears only in TestRunner as the trusted reference checker.

Data validation is transactional: books.csv, branches.csv and routes.csv are read into temporary state, and the current state is replaced only when every file and row validates. Errors include the source filename and line. A disconnected route graph is valid input. See data/README.txt for the invalid examples.

Files to review before upload:
  members.csv and contributions.csv contain an even proposed split across BST/data loading, graph algorithms, and sorting/comparison. Confirm or update the proposed tasks, evidence references, and peer reviews to match the group's actual work before submission.
  report.pdf summarizes design, traces, measurements, and algorithm choices.
  ai-use.txt discloses AI assistance.

Suggested fresh-copy check: copy this folder to a new directory, then run the build and test commands above. Submit only source/data/results/documentation; do not include build/ or .class files.
