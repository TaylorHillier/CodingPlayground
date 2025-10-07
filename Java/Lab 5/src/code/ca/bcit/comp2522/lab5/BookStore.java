package ca.bcit.comp2522.lab5;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class BookStore
{
    private final        String                 bookStoreName;
    private final        List<Novel>            references;
    private final        HashMap<String, Novel> map;
    private static final int                    DECADE_ADDER     = 9;
    private static final int                    MIN_TITLE_LENGTH = 0;
    private static final int                    INITIAL_COUNT    = 0;

    public BookStore(final String bookStoreName)
    {
        this.bookStoreName = bookStoreName;
        references         = new ArrayList<>();
        map                = new HashMap<>();
        loadBooks("references.txt");

        for (Novel novel : references)
        {
            map.put(novel.getTitle(), novel);
        }

        final Set<String> keySet = map.keySet();

        final List<String> keyList = new ArrayList<>(keySet);

        Collections.sort(keyList);

        final Iterator<String> iterator = keyList.iterator();

        while (iterator.hasNext())
        {
            final String key = iterator.next();
            final Novel novel = map.get(key);
            if (novel.getTitle().
                     toLowerCase().
                     contains("the"))
            {
                map.remove(key);
            }
        }

        for (Novel novel : map.values())
        {
            System.out.println(novel.toString());
        }
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
        System.out.println(bookstore.isThereABookWrittenIn(1950));
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

    public void loadBooks(final String filename)
    {
        try
        {
            final Scanner scan = new Scanner(new File(filename));

            while (scan.hasNextLine())
            {
                final String line = scan.nextLine().trim();

                if (line.isEmpty())
                {
                    continue;
                }

                final String[] parts = line.split("\\|");

                if (parts.length == 3)
                {
                    final String title = parts[0].trim();
                    final String author = parts[1].trim();
                    final int year = Integer.parseInt(parts[2].trim());

                    references.add(new Novel(title, author, year));
                }
                else
                {
                    System.out.println("Invalid line format");
                }
            }

            scan.close();

        }
        catch (FileNotFoundException e)
        {
            System.out.println("File was not found");
        }
    }

    public void printAllTitles()
    {
        for (final Novel novel : references)
        {
            final String title = novel.getTitle();

            if (title != null)
            {
                System.out.println(title.toUpperCase());
            }
        }
    }

    public void printBookTitle(final String title)
    {
        for (final Novel novel : references)
        {
            final String currentTitle = novel.getTitle();

            if (currentTitle != null && currentTitle.contains(title))
            {
                System.out.println(currentTitle);
            }
        }
    }

    public void printTitlesInAlphaOrder()
    {
        final ArrayList<String> titles = new ArrayList<>();

        for (final Novel novel : references)
        {
            String title = novel.getTitle();

            if (title != null)
            {
                titles.add(title);
            }
        }

        Collections.sort(titles);

        for (final String title : titles)
        {
            System.out.println(title);
        }
    }

    public void printGroupByDecade(final int decade)
    {
        final ArrayList<String> titles = new ArrayList<>();

        for (final Novel novel : references)
        {
            final String title = novel.getTitle();
            final int novelYear = novel.getYearPublished();

            if (title != null &&
                novelYear >= decade &&
                novelYear <= decade + DECADE_ADDER)
            {
                titles.add(title);
            }
        }

        for (final String title : titles)
        {
            System.out.println(title);
        }
    }

    public void getLongest()
    {
        String longestTitle;
        int longestTitleLength;

        longestTitle       = "";
        longestTitleLength = MIN_TITLE_LENGTH;

        for (final Novel novel : references)
        {
            final String title = novel.getTitle();

            if (title != null)
            {
                if (title.length() > longestTitleLength)
                {
                    longestTitle       = title;
                    longestTitleLength = title.length();
                }
            }
        }

        System.out.println(longestTitle);
    }

    public boolean isThereABookWrittenIn(final int year)
    {
        for (final Novel novel : references)
        {
            final int publishDate = novel.getYearPublished();

            if (publishDate == year)
            {
                return true;
            }
        }

        return false;
    }

    public int howManyBooksContain(final String word)
    {
        int count = INITIAL_COUNT;

        for (final Novel novel : references)
        {
            final String title = novel.getTitle();

            if (title != null &&
                title.toLowerCase().contains(word)
            )
            {
                count++;
            }
        }

        return count;
    }

    public double whichPercentWrittenBetween(final int first,
                                             final int last)
    {
        int inRange = INITIAL_COUNT;

        for (final Novel novel : references)
        {
            final int publishDate = novel.getYearPublished();

            if (publishDate >= first && publishDate <= last)
            {
                inRange++;
            }

        }

        return (inRange * 100.0) / references.size();
    }

    public Novel getOldestBook()
    {
        Integer oldest = null;
        Novel oldestNovel = null;

        for (final Novel novel : references)
        {
            final int publishDate = novel.getYearPublished();

            if (oldestNovel == null)
            {
                oldest      = publishDate;
                oldestNovel = novel;
            }

            if (publishDate < oldest)
            {
                oldestNovel = novel;
                oldest      = publishDate;
            }

        }

        return oldestNovel;
    }

    public List<Novel> getBooksThisLength(final int titleLength)
    {
        final ArrayList<Novel> novels = new ArrayList<>();

        for (final Novel novel : references)
        {
            final String novelTitle = novel.getTitle();

            if (novelTitle != null &&
                novelTitle.length() == titleLength)
            {
                novels.add(novel);
            }
        }

        return novels;
    }
}
