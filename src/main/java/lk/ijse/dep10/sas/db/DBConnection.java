package lk.ijse.dep10.sas.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection dbConnection;
    private final Connection connection;
    private DBConnection(){
        try {
            File configFile=new File("application.properties");
            Properties configuration = new Properties();
            FileReader fr=new FileReader(configFile);
            configuration.load(fr);
            fr.close();
            String host =configuration.getProperty("dep10.sas.host", "localhost");
            String port = configuration.getProperty("dep10.sas.port", "3306");
            String database = configuration.getProperty("dep10.sas.database", "student_registration_dep10");
            String userName=configuration.getProperty("dep10.sas.username","root");
            String password= configuration.getProperty("dep10.sas.password","mysql");

            String queryString="createDatabaseIfNotExist=true&allowMultiQueries=true";
            String url=String.format("jdbc:mysql://%s:%s/%s?%s",host,port,database,queryString);
            connection=DriverManager.getConnection(url,userName,password);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Configuration file does not exist").showAndWait();
            throw new RuntimeException(e);
        }catch (IOException e){
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Failed to read configuration").showAndWait();
            throw  new RuntimeException();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Failed to establish the database connection, try again if the problem persist").showAndWait();
            throw new RuntimeException(e);
        }
    }
    public static DBConnection getDbConnection(){
        return (dbConnection==null)? dbConnection=new DBConnection(): dbConnection;
    }
    public  Connection getConnection(){
        return connection;
    }
}
