package jxc1090.introDbEx2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    
    private static BufferedReader src_DbInit = new BufferedReader(new InputStreamReader(DbManager.class.getResourceAsStream("/DbInit.sql")));

    public static void main(String[] args) throws IOException {
    	String line;
    	while ( (line = src_DbInit.readLine()) != null ) 
    		System.out.println(line);
    	
    	
    	
//        try {
//            Class.forName("org.postgresql.Driver");
//        } catch (ClassNotFoundException e) {
//            System.out.println("Driver not found!");
//            return;
//        }
//
//        try {
//            conn = DriverManager.getConnection(DB_NAME, USERNAME, PASSWORD);
//        } catch (SQLException e) {
//            System.out.println("SQLException:");
//            System.out.println(e.getCause());
//            return;
//        }
//
//        if (conn == null) {
//            System.out.println("Connection failed");
//            return;
//        }
//
//        System.out.println("Connection successful!");
        
    }
}
