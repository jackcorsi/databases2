package jxc1090.introDbEx2;

import java.sql.*;
import javax.sql.*;

public class DbManager {

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found!");
            return;
        }
        System.out.println("Yay it worked!");
    }
}
