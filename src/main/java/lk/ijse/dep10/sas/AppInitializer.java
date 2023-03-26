package lk.ijse.dep10.sas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep10.sas.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Database connection is about to close");
                if (!DBConnection.getDbConnection().getConnection().isClosed()) {
                    DBConnection.getDbConnection().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        })                                                       );
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        generateSchemaIfNotExist();
        FXMLLoader fxmlLoader=new FXMLLoader(this.getClass().getResource("/view/StudentView.fxml"));
        AnchorPane root=fxmlLoader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void generateSchemaIfNotExist() {
        try {
            Connection connection = DBConnection.getDbConnection().getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("show tables");
            HashSet<String> tableNameList = new HashSet<>();
            while(rst.next()){
                tableNameList.add(rst.getString(1));
            }
            boolean tableExists=tableNameList.containsAll(
                    Set.of("Attendance","Picture","Student","User"));
            if (!tableExists){
                System.out.println("Schema is about to auto generate");
                stm.execute(readDBScript());

            }
            System.out.println(tableExists);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    private String readDBScript(){
        InputStream is = getClass().getResourceAsStream("/schema.sql");
        try(BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            String line;
            StringBuilder dbScriptBuilder=new StringBuilder();
            while ((line= br.readLine())!=null){
                dbScriptBuilder.append(line);
            }
            return dbScriptBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
