package jxc1090.introDbEx2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

public class DbReset {

	private static final String DB_NAME = "jdbc:postgresql://mod-intro-databases.cs.bham.ac.uk/jxc1090";
	private static final String USERNAME = "jxc1090";
	private static final String PASSWORD = "a0gsd8vrru";

	private static final double MAX_VENUECOST = 1000.0;
	private static final int NUMBER_OF_VENUES = 500;

	private static Connection conn;

	private static BufferedReader src_DbInit = new BufferedReader(
			new InputStreamReader(DbReset.class.getResourceAsStream("/DbReset.sql")));

	private static BufferedReader src_venues = new BufferedReader(
			new InputStreamReader(DbReset.class.getResourceAsStream("/eventbrite-venues-filtered.txt")));

	private static BufferedReader src_bands = new BufferedReader(
			new InputStreamReader(DbReset.class.getResourceAsStream("/eventbrite-bands-filtered.txt")));

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
			System.out.println("SQLException:");
			System.out.println(e.getCause());
			return;
		}

		if (conn == null) {
			System.out.println("Connection failed");
			return;
		}

		System.out.println("Connection successful!");

		try {
			Statement initStatement = conn.createStatement();
			initStatement.executeUpdate(collectAllLines(src_DbInit));

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

	private static String getRandomMeal() throws IOException {
		prepareKitchen();
		// meal -> item | adjective
		// adjective -> adjective | item
		// item -> "with" meal | "and" meal | compositeItem | END
		// compositeItem -> "with" meal | "and" meal | END
		switch (rand.nextInt(1)) {
		case 0:
			return getCookingItem();
		case 1:
			return getCookingAdjective();
		default:
			return "";
		}
	}

	private static String getCookingItem() throws IOException {
		String item = cookingItems.get(rand.nextInt(cookingItems.size() - 1));
		switch (rand.nextInt(3)) {
		case 0:
			item += "with";
			item += getRandomMeal();
			break;
		case 1:
			item += "and";
			item += getRandomMeal();
			break;
		case 2:
			item += getCookingCompositeItem();
			break;
		case 3:
			break;
		}
		return item;
	}

	private static String getCookingAdjective() throws IOException {
		String adjective = cookingAdjectives.get(rand.nextInt(cookingAdjectives.size() - 1));
		switch (rand.nextInt(1)) {
		case 0:
			adjective += getCookingAdjective();
			break;
		case 1:
			adjective += getCookingItem();
			break;
		}
		return adjective;
	}

	private static String getCookingCompositeItem() throws IOException {
		String compositeItem = cookingCompositeItems.get(rand.nextInt(cookingCompositeItems.size() - 1));
		switch (rand.nextInt(2)) {
		case 0:
			compositeItem += "with";
			compositeItem += getRandomMeal();
			break;
		case 1:
			compositeItem += "and";
			compositeItem += getRandomMeal();
			break;
		case 2:
			break;
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
