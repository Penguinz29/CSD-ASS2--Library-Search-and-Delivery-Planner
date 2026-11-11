package model;

import java.util.ArrayList;
import java.util.List;

/** Ordinary, unbalanced BST keyed by lexicographic bookId. */
public final class BookTree {
    private static final class Node {
        private final Book book;
        private Node left;
        private Node right;
        private Node(Book book) { this.book = book; }
    }

    private Node root;

    public void insert(Book book) {
        if (book == null) throw new IllegalArgumentException("book must not be null");
        root = insert(root, book);
    }

    private Node insert(Node node, Book book) {
        if (node == null) return new Node(book);
        int order = book.getBookId().compareTo(node.book.getBookId());
        if (order == 0) throw new IllegalArgumentException("duplicate bookId: " + book.getBookId());
        if (order < 0) node.left = insert(node.left, book);
        else node.right = insert(node.right, book);
        return node;
    }

    public Book search(String bookId) {
        if (bookId == null) return null;
        Node node = root;
        while (node != null) {
            int order = bookId.compareTo(node.book.getBookId());
            if (order == 0) return node.book;
            node = order < 0 ? node.left : node.right;
        }
        return null;
    }

    public boolean delete(String bookId) {
        if (bookId == null) return false;
        boolean[] removed = new boolean[1];
        root = delete(root, bookId, removed);
        return removed[0];
    }

    private Node delete(Node node, String bookId, boolean[] removed) {
        if (node == null) return null;
        int order = bookId.compareTo(node.book.getBookId());
        if (order < 0) node.left = delete(node.left, bookId, removed);
        else if (order > 0) node.right = delete(node.right, bookId, removed);
        else {
            removed[0] = true;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            Node successor = minimum(node.right);
            Node replacement = new Node(successor.book);
            replacement.left = node.left;
            replacement.right = deleteMin(node.right);
            return replacement;
        }
        return node;
    }

    private Node deleteMin(Node node) {
        if (node.left == null) return node.right;
        node.left = deleteMin(node.left);
        return node;
    }

    private Node minimum(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    public List<Book> inorder() {
        List<Book> result = new ArrayList<Book>();
        inorder(root, result);
        return result;
    }

    private void inorder(Node node, List<Book> result) {
        if (node == null) return;
        inorder(node.left, result);
        result.add(node.book);
        inorder(node.right, result);
    }

    public int size() { return count(root); }
    private int count(Node node) { return node == null ? 0 : 1 + count(node.left) + count(node.right); }

    /** Height is measured in nodes: empty tree=0, leaf=1. */
    public int height() { return height(root); }
    private int height(Node node) {
        return node == null ? 0 : 1 + Math.max(height(node.left), height(node.right));
    }
}

