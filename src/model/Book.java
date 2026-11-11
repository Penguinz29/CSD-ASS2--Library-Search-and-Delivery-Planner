package model;

import java.util.Objects;

/** Immutable catalogue record. */
public final class Book {
    private final String bookId;
    private final String title;
    private final String category;
    private final int copies;

    public Book(String bookId, String title, String category, int copies) {
        this.bookId = Objects.requireNonNull(bookId, "bookId");
        this.title = Objects.requireNonNull(title, "title");
        this.category = Objects.requireNonNull(category, "category");
        this.copies = copies;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public int getCopies() { return copies; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Book)) return false;
        Book book = (Book) other;
        return copies == book.copies && bookId.equals(book.bookId)
                && title.equals(book.title) && category.equals(book.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, title, category, copies);
    }

    @Override
    public String toString() {
        return bookId + " | " + title + " | " + category + " | copies=" + copies;
    }
}

