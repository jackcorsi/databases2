package jxc1090.introDbEx2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {

    private static final String DB_NAME = "jdbc:postgresql://mod-intro-databases.cs.bham.ac.uk/jxc1090";
    private static final String USERNAME = "jxc1090";
    private static final String PASSWORD = "a0gsd8vrru";
    private static Connection conn;
    
    private static final Path res_DbInit = Paths.get(DbManager.class.getResource("ex2/res/jxc1090/introDbEx2/DbManager/DbInit.sql").getPath());

    public static void main(String[] args) {
    	String query;
    	try {
			query = new String(Files.readAllBytes(res_DbInit));
		} catch (IOException e1) {
			System.err.println("MISSING RESOURCE FILE LMAO");
		}
    	
    	
    	
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
        
    }
}
