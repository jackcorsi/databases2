package jxc1090.introDbEx2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

public class DbReset {

    private static final String DB_NAME = "jdbc:postgresql://mod-intro-databases.cs.bham.ac.uk/jxc1090";
    private static final String USERNAME = "jxc1090";
    private static final String PASSWORD = "a0gsd8vrru";

    private static final double MAX_VENUECOST = 1000.0;
    private static final double MAX_FOODCOST = 50.0;
    private static final double MAX_ENTERTAINMENTCOST = 500.0;
    private static final double MAX_PARTYCOST = 1000.0;
    private static final int MAX_NUMBER_OF_GUESTS = 500;
    private static final int MAX_YEAR_OF_PARTY = 2020;
    private static final int MIN_YEAR_OF_PARTY = 2007;
    private static final int NUMBER_OF_VENUES = 100;
    private static final int NUMBER_OF_MENUS = 100;
    private static final int NUMBER_OF_ENTERTAINMENTS = 100;
    private static final int NUMBER_OF_PARTIES = 1000;
    private static final int COOKING_MAX_LEVEL_OF_RECURSION = 4;

    private static Connection conn;

    private static BufferedReader src_DbInit = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/DbReset.sql")));

    private static BufferedReader src_venues = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/eventbrite-venues-filtered.txt")));

    private static BufferedReader src_bands = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/eventbrite-bands.txt")));

    private static BufferedReader src_events = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/eventbrite-events.txt")));

    private static BufferedReader src_cooking_items = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/cooking-items.txt")));

    private static BufferedReader src_cooking_adjectives = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/cooking-adjectives.txt")));

    private static BufferedReader src_cooking_composite_items = new BufferedReader(
            new InputStreamReader(DbReset.class.getResourceAsStream("/cooking-composite-items.txt")));

    private static ArrayList<String> cookingItems;
    private static ArrayList<String> cookingAdjectives;
    private static ArrayList<String> cookingCompositeItems;

    private static Random rand = new Random();

    public static void main(String[] args) {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found!");
            return;
        }

        try {
            conn = DriverManager.getConnection(DB_NAME, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection failed - SQLException...");
            System.out.println(e.getCause());
            return;
        }

        if (conn == null) {
            System.out.println("Connection failed");
            return;
        }

        System.out.println("Connection successful!");

        try {
            System.out.println("Creating tables...");
            Statement initStatement = conn.createStatement();
            initStatement.executeUpdate(collectAllLines(src_DbInit));

            System.out.println("Filling venues...");
            fillVenues();

            System.out.println("Filling menus...");
            fillMenus();

            System.out.println("Filling entertainment...");
            fillEntertainment();

            System.out.println("Filling parties...");
            fillParties();

            System.out.println("Done!");

        } catch (SQLException e) {
            System.err.println("SQL Exception: ");
            System.err.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("IOException: ");
            System.err.println(e.getMessage());
            return;
        }
    }

    private static void fillVenues() throws SQLException, IOException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Venue (name, venuecost) VALUES (?, ?)");

        for (int i = 0; i < NUMBER_OF_VENUES; i++) {
            String venue = src_venues.readLine();
            if (venue == null) {
                System.err.println("Ran out of venues to populate at " + i);
                return;
            }

            stmt.setString(1, venue);
            stmt.setBigDecimal(2, new BigDecimal(Math.random() * MAX_VENUECOST));
            stmt.executeUpdate();
        }
    }

    private static void fillMenus() throws SQLException, IOException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Menu (description, costprice) VALUES (?, ?)");

        for (int i = 0; i < NUMBER_OF_MENUS; i++) {
            String description = getRandomMeal(0);
            stmt.setString(1, description);
            stmt.setBigDecimal(2, new BigDecimal(Math.random() * MAX_FOODCOST));
            stmt.execute();
        }
    }

    private static void fillEntertainment() throws SQLException, IOException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO Entertainment (description, costprice) VALUES (?, ?)");

        for (int i= 0; i < NUMBER_OF_ENTERTAINMENTS; i++) {
            String description = src_bands.readLine();
            if (description == null) {
                System.err.println("Ran out of band name data at " + i);
                return;
            }
            stmt.setString(1, description);
            stmt.setBigDecimal(2, new BigDecimal(Math.random() * MAX_ENTERTAINMENTCOST));
            stmt.executeUpdate();
        }
    }

    private static void fillParties() throws SQLException, IOException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Party (name, mid, vid, eid, price, timing, numberofguests) VALUES (?, ?, ?, ?, ?, ?, ?)");

        for (int i = 0; i < NUMBER_OF_PARTIES; i++) {
            String name = src_events.readLine();
            if (name == null) {
                System.err.println("Ran out of event name data at " + i);
                return;
            }
            stmt.setString(1, name); //name
            stmt.setInt(2, rand.nextInt(NUMBER_OF_MENUS ) + 1); //mid
            stmt.setInt(3, rand.nextInt(NUMBER_OF_VENUES) + 1); //vid
            stmt.setInt(4, rand.nextInt(NUMBER_OF_ENTERTAINMENTS) + 1); //eid
            stmt.setBigDecimal(5, new BigDecimal(Math.random() * MAX_PARTYCOST)); //price
            int year = rand.nextInt(MAX_YEAR_OF_PARTY - MIN_YEAR_OF_PARTY + 1) + MIN_YEAR_OF_PARTY;
            int date = rand.nextInt(32) + 1; //31 days in December
            int hour = rand.nextInt(24);
            int minute = rand.nextInt(6) * 10; //timing
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(0l); //0 initialising this prevents random sub-second values for some reason
            cal.set(year, GregorianCalendar.DECEMBER, date, hour, minute, 0);
            stmt.setTimestamp(6, new Timestamp(cal.getTimeInMillis())); //timing
            stmt.setInt(7, rand.nextInt(MAX_NUMBER_OF_GUESTS + 1)); //numberofguests
            stmt.executeUpdate();
        }
    }

    // Used for reading the sql files into a single String
    private static String collectAllLines(BufferedReader r) {
        try {
            StringBuilder b = new StringBuilder();
            String line;
            while ((line = src_DbInit.readLine()) != null) {
                b.append(line);
                b.append('\n');
            }
            return b.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static String getRandomMeal(int lor) throws IOException {
        prepareKitchen();
        // meal -> item | adjective
        // adjective -> adjective | item
        // item -> "with" meal | "and" meal | compositeItem | END
        // compositeItem -> "with" meal | "and" meal | END
        switch (rand.nextInt(2)) {
            case 0:
                return getCookingItem(lor + 1);
            case 1:
                return getCookingAdjective(lor + 1);
            default:
                return "";
        }
    }

    private static String getCookingItem(int lor) throws IOException {
        String item = cookingItems.get(rand.nextInt(cookingItems.size() - 1));
        if (lor <= COOKING_MAX_LEVEL_OF_RECURSION) {
            switch (rand.nextInt(4)) {
                case 0:
                    item += " with";
                    item += getRandomMeal(lor + 1);
                    break;
                case 1:
                    item += " and";
                    item += getRandomMeal(lor  + 1);
                    break;
                case 2:
                    item += getCookingCompositeItem(lor + 1);
                    break;
                case 3:
                    break;
            }
        }
        return item;
    }

    private static String getCookingAdjective(int lor) throws IOException {
        String adjective = cookingAdjectives.get(rand.nextInt(cookingAdjectives.size() - 1));
        switch (rand.nextInt(2)) {
            case 0:
                adjective += getCookingAdjective(lor + 1);
                break;
            case 1:
                adjective += getCookingItem(lor + 1);
                break;
        }
        return adjective;
    }

    private static String getCookingCompositeItem(int lor) throws IOException {
        String compositeItem = cookingCompositeItems.get(rand.nextInt(cookingCompositeItems.size() - 1));
        if (lor <= COOKING_MAX_LEVEL_OF_RECURSION) {
            switch (rand.nextInt(3)) {
                case 0:
                    compositeItem += " with";
                    compositeItem += getRandomMeal(lor + 1);
                    break;
                case 1:
                    compositeItem += " and";
                    compositeItem += getRandomMeal(lor + 1);
                    break;
                case 2:
                    break;
            }
        }
        return compositeItem;
    }

    // Reads everything from the files
    private static void prepareKitchen() throws IOException {
        if (cookingItems == null) {
            cookingItems = new ArrayList<String>();
            String line;
            while ((line = src_cooking_items.readLine()) != null)
                cookingItems.add(line);
        }

        if (cookingAdjectives == null) {
            cookingAdjectives = new ArrayList<String>();
            String line;
            while ((line = src_cooking_adjectives.readLine()) != null)
                cookingAdjectives.add(line);
        }

        if (cookingCompositeItems == null) {
            cookingCompositeItems = new ArrayList<String>();
            String line;
            while ((line = src_cooking_composite_items.readLine()) != null)
                cookingCompositeItems.add(line);
        }
    }
}
