package util;

/**
 * Application-wide constants shared across Model, View, Controller, and
 * runners.
 */
public final class Constants {
        private Constants() {
        }

        // Data files and validation rules
        public static final String DEFAULT_DATA_DIR = "data/valid";
        public static final String DEFAULT_TEST_DATA_DIR = "data";
        public static final String DEFAULT_SORT_RESULTS_PATH = "results/sorting.csv";
        public static final String BOOKS_FILE = "books.csv";
        public static final String BRANCHES_FILE = "branches.csv";
        public static final String ROUTES_FILE = "routes.csv";
        public static final String BOOKS_HEADER = "bookId,title,category,copies";
        public static final String BRANCHES_HEADER = "branchId,name";
        public static final String ROUTES_HEADER = "from,to,distance";
        public static final int MIN_BOOKS_COUNT = 20;
        public static final int MIN_BRANCHES_COUNT = 6;

        // Main menu options
        public static final String MENU_EXIT = "0";
        public static final String MENU_LIST_BOOKS = "1";
        public static final String MENU_SEARCH_BOOK = "2";
        public static final String MENU_DELETE_BOOK = "3";
        public static final String MENU_BST_STATS = "4";
        public static final String MENU_SHOW_GRAPH = "5";
        public static final String MENU_BFS = "6";
        public static final String MENU_SHORTEST_ROUTE = "7";
        public static final String MENU_MST = "8";
        public static final String MENU_SORT_BOOKS = "9";

        // Sorting options
        public static final String SORT_CHOICE_QUICK = "1";
        public static final String SORT_CHOICE_MERGE = "2";
        public static final String SORT_CHOICE_HEAP = "3";
        public static final String SORT_CHOICE_INSERTION = "4";
        public static final String SORT_NAME_QUICK = "quick";
        public static final String SORT_NAME_MERGE = "merge";
        public static final String SORT_NAME_HEAP = "heap";
        public static final String SORT_NAME_INSERTION = "insertion";

        // Console table formatting
        public static final int COL_WIDTH_ID = 6;
        public static final int COL_WIDTH_TITLE = 25;
        public static final int COL_WIDTH_CATEGORY = 12;
        public static final int COL_WIDTH_COPIES = 6;
        public static final int COL_WIDTH_BRANCH = 8;
        public static final int COL_WIDTH_NEIGHBORS = 45;
        public static final String BOOK_TABLE_LINE = "--------------------------------------------------------------";
        public static final String MENU_LINE = "------------------------------------------------------------";
        public static final String STATS_TABLE_LINE = "------------------------------------";

        // Comparison benchmark parameters
        public static final long COMPARE_SEED = 20261109L;
        public static final int[] COMPARE_SIZE_ARRAY = { 100, 500, 1000 };
        public static final String[] COMPARE_ORDER_ARRAY = {
                        "seeded_random",
                        "already_sorted",
                        "reverse_sorted",
                        "equal_copy_counts"
        };
        public static final int EQUAL_COPIES_VALUE = 42;
        public static final int MAX_RANDOM_COPIES_BOUND = 1001;
}
