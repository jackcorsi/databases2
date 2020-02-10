package jxc1090.introDbEx2;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DbAccess {

    private static final String DB_NAME = "jdbc:postgresql://mod-intro-databases.cs.bham.ac.uk/jxc1090";
    private static final String USERNAME = "jxc1090";
    //Note from future Jack - no, this endpoint and login will not still work. Calm down
    private static final String PASSWORD = "a0gsd8vrru";
    private static final String DATE_PARSE_FORMAT = "dd/MM/yyyy HH:mm";
    private static final SimpleDateFormat dateParser = new SimpleDateFormat(DATE_PARSE_FORMAT);
    private static final Pattern regex_whitespace = Pattern.compile("\\s");

    private static Scanner in = new Scanner(System.in);
    private static Connection conn;

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Program configuration error - failed to link postgresql drivers");
            return;
        }

        System.out.println("Connecting...");

        try {
            conn = DriverManager.getConnection(DB_NAME, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection to the database failed! Reason: " + e.getMessage());
            System.out.println("Terminating.");
            return;
        }

        if (conn == null) {
            System.out.println("Connection to the database failed! Terminating.");
            return;
        }

        while (true) {
            System.out.print("\n$: ");
            String cmd;
            try {
                switch (cmd = in.next()) {
                    case "quit":
                        return;
                    case "q":
                        return;
                    case "help":
                        cmd_help();
                        break;
                    case "party":
                        cmd_party();
                        break;
                    case "menu":
                        cmd_menu();
                        break;
                    case "newparty":
                        cmd_newparty();
                        break;
                    default:
                        System.out.println("Command not recognised: \"" + cmd + "\"");
                        System.out.println("Type \"help\" for commands");
                }
            } catch (SQLException e) {
                System.out.println("Your operation failed with the following error -");
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    private static void cmd_party() throws SQLException {
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected Party ID");
            return;
        }

        int partyId = in.nextInt();
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT\n" +
                "  Party.pid,\n" +
                "  Party.numberofguests,\n" +
                "  Party.price::NUMERIC,\n" + //NUMERIC otherwise it won't convert to BigDec
                "  Party.name,\n" +
                "  Venue.name,\n" +
                "  Venue.venuecost::NUMERIC,\n" +
                "  Menu.description,\n" +
                "  Menu.costprice::NUMERIC,\n" +
                "  Entertainment.description,\n" +
                "  Entertainment.costprice::NUMERIC\n" +
                "\n" +
                "FROM Party, Venue, Menu, Entertainment\n" +
                "WHERE\n" +
                "  Party.pid = ? AND\n" +
                "  Venue.vid = Party.vid AND\n" +
                "  Menu.mid = Party.mid AND\n" +
                "  Entertainment.eid = Party.eid");

        stmt.setInt(1, partyId);
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            System.out.println("No party found for this ID");
            return;
        }

        //Unpack the results
        int Party_pid = result.getInt(1);
        System.out.println("Party ID: " + Party_pid);
        int Party_numberofguests = result.getInt(2);
        BigDecimal Party_price = result.getBigDecimal(3);
        String Party_name = result.getString(4);
        String Venue_name = result.getString(5);
        BigDecimal Venue_venuecost = result.getBigDecimal(6);
        String Menu_description = result.getString(7);
        BigDecimal Menu_costprice = result.getBigDecimal(8);
        String Entertainment_description = result.getString(9);
        BigDecimal Entertainment_costprice = result.getBigDecimal(10);

        BigDecimal totalCost = Venue_venuecost.add(
                Entertainment_costprice.add(
                        Menu_costprice.multiply(
                                new BigDecimal(Party_numberofguests)
                        )
                ));
        BigDecimal netProfit = Party_price.subtract(totalCost);

        //Print report
        System.out.println("--PARTY REPORT--");
        System.out.println("PARTY ID: " + Party_pid);
        System.out.println("PARTY NAME: " + Party_name);
        System.out.println("VENUE: " + Venue_name);
        System.out.println("MENU: " + Menu_description);
        System.out.println("ENTERTAINMENT: " + Entertainment_description);
        System.out.println("NUMBER OF GUESTS: " + Party_numberofguests);
        System.out.println("CHARGING PRICE: £" + Party_price);
        System.out.println("TOTAL COST: £" + totalCost);
        System.out.println("NET PROFIT: £" + netProfit);
    }

    private static void cmd_menu() throws SQLException {
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected Menu ID");
            return;
        }

        int menuId = in.nextInt();
        PreparedStatement menuStmt = conn.prepareStatement(
                "SELECT\n" +
                "  Menu.mid,\n" +
                "  Menu.description,\n" +
                "  Menu.costprice::NUMERIC\n" +
                "FROM Menu\n" +
                "WHERE mid = ?;");

        PreparedStatement partyStmt = conn.prepareStatement(
                "SELECT\n" +
                        "  COUNT(Party.pid),\n" +
                        "  SUM(Party.numberofguests)\n" +
                        "FROM Menu, party\n" +
                        "WHERE\n" +
                        "      Menu.mid = ? AND\n" +
                        "      Menu.mid = Party.mid"
        );

        menuStmt.setInt(1, menuId);
        ResultSet result = menuStmt.executeQuery();

        if (!result.next()) {
            System.out.println("No menu found for this ID");
            return;
        }

        int Menu_mid = result.getInt(1);
        String Menu_description = result.getString(2);
        BigDecimal Menu_costprice = result.getBigDecimal(3);

        partyStmt.setInt(1, menuId);
        result = partyStmt.executeQuery();

        result.next();
        int count_Party_pid = result.getInt(1);
        int sum_Party_numberofguests = result.getInt(2);

        //Print report
        System.out.println("--MENU REPORT--");
        System.out.println("MENU ID: " + Menu_mid);
        System.out.println("DESCRIPTION: " + Menu_description);
        System.out.println("PRICE: £" + Menu_costprice);
        System.out.println("NUMBER OF PARTIES: " + count_Party_pid);
        System.out.println("TOTAL GUESTS SERVED: " + sum_Party_numberofguests);
    }

    private static void cmd_newparty() throws SQLException {
        System.out.println("--CREATE PARTY--");
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Party (pid, name, mid, vid, eid, price, timing, numberofguests) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        System.out.println("PARTY ID: ");
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected an integer (Party ID)");
            return;
        }
        stmt.setInt(1, in.nextInt());

        System.out.print("PARTY NAME: ");
        in.skip(regex_whitespace);
        stmt.setString(2, in.nextLine());

        System.out.println("MENU ID: ");
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected an integer (Menu ID)");
            return;
        }
        stmt.setInt(3, in.nextInt());

        System.out.println("VENUE ID: ");
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected an integer (Venue ID)");
            return;
        }
        stmt.setInt(4, in.nextInt());

        System.out.println("ENTERTAINMENT ID: ");
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected an integer (Entertainment ID)");
            return;
        }
        stmt.setInt(5, in.nextInt());

        System.out.println("PRICE: ");
        if (!in.hasNextBigDecimal()) {
            System.out.println("Could not interpret your input as a decimal number (Party Price)");
            return;
        }
        stmt.setBigDecimal(6, in.nextBigDecimal());

        System.out.println("TIMING (accepted format displayed)");
        System.out.println(DATE_PARSE_FORMAT);
        java.util.Date d;
        Calendar cal = new GregorianCalendar();
        try {
            in.skip(regex_whitespace);
            d = dateParser.parse(in.nextLine());
        } catch (ParseException e) {
            System.out.println("Could not interpret your input as a date and time (" + DATE_PARSE_FORMAT + ")");
            return;
        }
        cal.setTime(d);
        stmt.setTimestamp(7, new Timestamp(cal.getTimeInMillis()));

        System.out.println("NUMBER OF GUESTS: ");
        if (!in.hasNextInt()) {
            System.out.println("Invalid input - expected an integer (Number of guests)");
            return;
        }
        stmt.setInt(8, in.nextInt());

        System.out.println("Confirm create new party? (y) ");
        if (!in.next().equals("y")) {
            System.out.println("Cancel.");
            return;
        }

        stmt.executeUpdate();

        System.out.println("Created.");
    }

    private static void cmd_help() {
        System.out.println("--COMMANDS--");
        System.out.println("q OR quit - exit program");
        System.out.println("party [party-id] - generate report for party");
        System.out.println("menu [menu-id] - generate report for menu");
        System.out.println("newparty - create new party");
    }
}
