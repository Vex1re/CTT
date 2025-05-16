package space.krokodilich.ctt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresqlExample {
    public static void main(String[] args) throws ClassNotFoundException {
        try (final Connection connection =
                     DriverManager.getConnection("postgres://avnadmin:AVNS_esreOJMgYNU4pKxKnLs@pubications-enb8e-63e2.f.aivencloud.com:17749/defaultdb?sslmode=require");
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT version()")) {

            while (resultSet.next()) {
                System.out.println("Version: " + resultSet.getString("version"));
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}