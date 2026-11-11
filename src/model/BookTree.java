package model;

import java.util.ArrayList;
import java.util.List;

public final class BookTree {
    private static final class Node {
        private Book book;
        private Node left;
        private Node right;

        private Node(Book book) {
            this.book = book;
        }
    }

    private Node root;

    /**
     * this method inserts a book into the binary search tree based on its bookId.
     * @param book the book to be inserted; must not be null
     */
    public void insert(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("book must not be null");
        }
        root = insertNode(root, book);
    }


    //  supporting recursive method to insert a book into the BST
    private Node insertNode(Node node, Book book) {
        // Base case: found empty spot to create a new leaf node
        if (node == null) {
            return new Node(book);
        }

        int order = book.getBookId().compareTo(node.book.getBookId());

        // Reject duplicate bookId instead of overwriting
        if (order == 0) {
            throw new IllegalArgumentException("duplicate bookId: " + book.getBookId());
        }

        if (order < 0) {
            node.left = insertNode(node.left, book);
        } else {
            node.right = insertNode(node.right, book);
        }
        return node;
    }

    /**
     * this method searches for a book in the binary search tree based on its bookId.
     * @param bookId the bookId to search for; must not be null
     * @return the book if found, null otherwise
     */
    public Book search(String bookId) {
        if (bookId == null) {
            return null;
        }

        Node current = root;
        while (current != null) {
            int order = bookId.compareTo(current.book.getBookId());
            if (order == 0) {
                return current.book;
            }
            if (order < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    /**
     * this method deletes a book from the binary search tree based on its bookId.
     * @param bookId the bookId of the book to delete; must not be null
     * @return true if the book was found and deleted, false otherwise
     */
    public boolean delete(String bookId) {
        if (bookId == null || search(bookId) == null) {
            return false;
        }
        root = deleteNode(root, bookId);
        return true;
    }

    private Node deleteNode(Node node, String bookId) {
        if (node == null) {
            return null;
        }

        int order = bookId.compareTo(node.book.getBookId());
        if (order < 0) {
            node.left = deleteNode(node.left, bookId);
            return node;
        }
        if (order > 0) {
            node.right = deleteNode(node.right, bookId);
            return node;
        }

        // Case 1 & 2: node has no left child (includes leaf node)
        if (node.left == null) {
            return node.right;
        }

        // Case 2: node has no right child
        if (node.right == null) {
            return node.left;
        }

        // Case 3: node has two children => replace with in-order successor (min of right subtree)
        Node successor = findMinNode(node.right);
        node.book = successor.book;
        node.right = deleteNode(node.right, successor.book.getBookId());
        return node;
    }

    private Node findMinNode(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    /**
     * this method returns a list of all books in the binary search tree in sorted order based on their bookId.
     * @return a list of books in sorted order
     */
    public List<Book> inorder() {
        List<Book> resultList = new ArrayList<Book>();
        traverseInorder(root, resultList);
        return resultList;
    }

    // supporting recursive method to traverse the BST in-order and collect books
    private void traverseInorder(Node node, List<Book> resultList) {
        if (node == null) {
            return;
        }
        traverseInorder(node.left, resultList);
        resultList.add(node.book);
        traverseInorder(node.right, resultList);
    }

    /**
     * this method returns the number of books in the binary search tree.
     * @return the number of books in the tree
     */
    public int size() {
        return countNodes(root);
    }

    // supporting recursive method to count the number of nodes in the BST
    private int countNodes(Node node) {
        if (node == null) {
            return 0;
        }
        int leftCount = countNodes(node.left);
        int rightCount = countNodes(node.right);
        return 1 + leftCount + rightCount;
    }

    /**
     * this method returns the height of the binary search tree.
     * @return the height of the tree
     */
    public int height() {
        return computeHeight(root);
    }

    /**
     * this method computes the height of the binary search tree recursively.
     * @param node the current node being evaluated; can be null
     * @return the height of the tree
     */
    private int computeHeight(Node node) {
        if (node == null) {
            return 0;
        }
        int leftHeight = computeHeight(node.left);
        int rightHeight = computeHeight(node.right);
        if (leftHeight > rightHeight) {
            return 1 + leftHeight;
        } else {
            return 1 + rightHeight;
        }
    }
}
