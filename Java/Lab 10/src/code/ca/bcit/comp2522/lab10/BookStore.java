package ca.bcit.comp2522.lab10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookStore
{
    private final String      bookStoreName;
    private final List<Novel> references;

    public static final int DECADE_YEARS = 10;

    public BookStore(final String bookStoreName)
    {
        this.bookStoreName = bookStoreName;
        references         = new ArrayList<>();

        populateReferences();
    }

    public static void main(final String[] args)
    {
        final BookStore bookstore;
        final Novel oldest;
        final List<Novel> fifteenCharTitles;

        bookstore = new BookStore("Classic Novels Collection");
        System.out.println("All Titles in UPPERCASE:");
        bookstore.printAllTitles();
        System.out.println("\nBook Titles Containing 'the':");
        bookstore.printBookTitle("the");
        System.out.println("\nAll Titles in Alphabetical Order:");
        bookstore.printTitlesInAlphaOrder();
        System.out.println("\nBooks from the 2000s:");
        bookstore.printGroupByDecade(2000);
        System.out.println("\nLongest Book Title:");
        bookstore.getLongest();
        System.out.println("\nIs there a book written in 1950?");
        System.out.println(bookstore.isThereABookWrittenBetween(1950));
        System.out.println("\nHow many books contain 'heart'?");
        System.out.println(bookstore.howManyBooksContain("heart"));
        System.out.println("\nPercentage of books written between 1940 and 1950:");
        System.out.println(bookstore.whichPercentWrittenBetween(1940, 1950) + "%");
        System.out.println("\nOldest book:");
        oldest = bookstore.getOldestBook();
        System.out.println(oldest.getTitle() + " by " + oldest.getAuthorName() + ", " +
                           oldest.getYearPublished());
        System.out.println("\nBooks with titles 15 characters long:");
        fifteenCharTitles = bookstore.getBooksThisLength(15);
        fifteenCharTitles.forEach(novel -> System.out.println(novel.getTitle()));
    }

    private void populateReferences()
    {
        Path path;
        path = Paths.get("references.txt");

        try (BufferedReader reader = Files.newBufferedReader(path))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                final String[] parts;
                final String title;
                final String author;
                final int yearPublished;

                parts = line.split("\\|");
                if (parts.length == 3)
                {
                    title         = parts[0];
                    author        = parts[1];
                    yearPublished = Integer.parseInt(parts[2]);

                    references.add(new Novel(title, author, yearPublished));
                }
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private void printAllTitles()
    {
        references.stream().map(novel -> novel.getTitle().toUpperCase()).forEach(System.out::println);
    }

    private void printBookTitle(final String keyword)
    {
        references.stream().map(novel -> novel.getTitle().toLowerCase()).filter(novel -> novel.contains(keyword)).forEach(System.out::println);
    }

    private void printTitlesInAlphaOrder()
    {
        references.stream().map(Novel::getTitle).sorted().forEach(System.out::println);
    }

    private void printGroupByDecade(final int decade)
    {
        references.stream().filter(novel -> novel.getYearPublished() >= decade && novel.getYearPublished() < decade + DECADE_YEARS)
                  .map(Novel::getTitle)
                  .forEach(System.out::println);
    }

    private void getLongest()
    {
        references.stream().max(Comparator.comparingInt(novel -> novel.toString().length())).ifPresent(System.out::println);
    }

    private boolean isThereABookWrittenBetween(final int year)
    {
        return references.stream().anyMatch(novel -> novel.getYearPublished() == year);
    }

    private long howManyBooksContain(final String characters)
    {
        return references.stream().filter(novel -> novel.getTitle().toLowerCase().contains(characters)).count();
    }

    private long whichPercentWrittenBetween(final int year1,
                                            final int year2)
    {
        return (long) ((references.stream().filter(novel -> novel.getYearPublished() >= year1 && novel.getYearPublished() <= year2)
                                  .count() * 100.0) / references.size());
    }

    private Novel getOldestBook()
    {
        return references.stream().min(Comparator.comparingInt(Novel::getYearPublished)).orElse(null);
    }

    private List<Novel> getBooksThisLength(final int length)
    {
        return references.stream().filter(novel -> novel.getTitle().length() == length)
                         .toList();
    }
}
