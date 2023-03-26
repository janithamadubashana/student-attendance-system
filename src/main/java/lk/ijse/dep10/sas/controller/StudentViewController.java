package lk.ijse.dep10.sas.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import lk.ijse.dep10.sas.db.DBConnection;
import lk.ijse.dep10.sas.util.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;


public class StudentViewController {

    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPicture;

    @FXML
    private TableView<Student> tblStudentInfo;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSearchBar;

    public void initialize() {


//        background.setRadius(150);
//        imgPicture.setFitHeight(150);
//        imgPicture.setFitWidth(150);
//
//        StackPane root = new StackPane();
//        root.getChildren().addAll(background, imgPicture);




        btnClear.setDisable(true);
        btnDelete.setDisable(true);
        loadAllStudents();
        tblStudentInfo.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("pictureToTable"));
        tblStudentInfo.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudentInfo.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudentInfo.getSelectionModel().selectedItemProperty().addListener((observableValue, old, current) -> {
            btnDelete.setDisable(current == null);
            if (current == null) return;
            txtId.setText(current.getId());
            txtName.setText(current.getName());
            Blob picture = current.getPicture();
            if (picture != null) {
                try {
                    InputStream is = picture.getBinaryStream();
                    Image image = new Image(is);
                    imgPicture.setImage(image);
                    btnClear.setDisable(false);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                btnClear.fire();
            }
        });
        Platform.runLater(() -> {
            btnNewStudent.fire();
        });
        txtSearchBar.textProperty().addListener((ob, old, current) -> {
            Connection connection = DBConnection.getDbConnection().getConnection();
            try {
                PreparedStatement stm = connection.prepareStatement(
                        "select * from Student where name like ? or id like ? ");
                PreparedStatement stm2 = connection.prepareStatement(
                        "select * from Picture where student_id like ?");
                stm.setString(1, "%" + current + "%");
                stm.setString(2, "%" + current + "%");
                ResultSet rst = stm.executeQuery();
                ObservableList<Student> studentList = tblStudentInfo.getItems();
                studentList.clear();
                while (rst.next()) {
                    String id = rst.getString("id");
                    String name = rst.getString("name");
                    stm2.setString(1,id);
                    ResultSet rst2 = stm2.executeQuery();
                    Student student = new Student(id, name, null);
                    while (rst2.next()) {
                        Blob picture = rst2.getBlob("picture");
                        student.setPicture(picture);

                    }
                    studentList.add(student);
                }
//
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        });

    }

    private void loadAllStudents() {
        try {
            Connection connection = DBConnection.getDbConnection().getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("select * from Student");
            PreparedStatement stm2 = connection.prepareStatement(
                    "select * from Picture where student_id=?");
            while (rst.next()) {
                String id = rst.getString("id");
                String name = rst.getString("name");
                Student student = new Student(id, name, null);

                stm2.setString(1, id);
                ResultSet rstPicture = stm2.executeQuery();
                if (rstPicture.next()) {
                    Blob picture = rstPicture.getBlob("picture");
                    student.setPicture(picture);
                }
                tblStudentInfo.getItems().add(student);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files",
                "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null) {
            Image image = new Image(file.toURI().toURL().toString());
            imgPicture.setImage(image);
            btnClear.setDisable(false);
        }

    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        Image image = new Image("/images/No_Image_Available.jpg");
        imgPicture.setImage(image);
        btnClear.setDisable(true);
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Student selectedItem = tblStudentInfo.getSelectionModel().getSelectedItem();
        Connection connection = DBConnection.getDbConnection().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement stmStudent = connection.prepareStatement(
                    "delete from Student where id=? ");
            PreparedStatement stmPicture = connection.prepareStatement(
                    "delete from Picture where student_id=?");
            System.out.println(selectedItem.getId());
            stmPicture.setString(1, selectedItem.getId());
            stmPicture.executeUpdate();
            stmStudent.setString(1, selectedItem.getId());
            stmStudent.executeUpdate();
            connection.commit();
            tblStudentInfo.getItems().remove(selectedItem);
            if (tblStudentInfo.getItems().isEmpty()) btnNewStudent.fire();
            tblStudentInfo.refresh();

        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        String generatedId = generateNewId();
        txtId.setText(generatedId);
        txtName.clear();
        txtSearchBar.clear();
        tblStudentInfo.getSelectionModel().clearSelection();
        imgPicture.setImage(new Image("No_Image_Available"));
        
        txtName.requestFocus();

    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!isDataValid()) return;
        Connection connection = DBConnection.getDbConnection().getConnection();
        try {
            connection.setAutoCommit(false);
            if (tblStudentInfo.getSelectionModel().isEmpty()) {
                String newId = txtId.getText();
                PreparedStatement stmStudent = connection.prepareStatement(
                        "insert into Student(id, name) values (?,?)");
                PreparedStatement stmPicture = connection.prepareStatement(
                        "insert into Picture(picture, student_id) values (?,?)");
                stmStudent.setString(1, generateNewId());
                stmStudent.setString(2, txtName.getText());
                stmStudent.executeUpdate();
                Student student = new Student(newId, txtName.getText(), null);
                Image image = imgPicture.getImage();
                if (image != null) {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", baos);
                    byte[] bytes = baos.toByteArray();
                    Blob studentImage = new SerialBlob(bytes);
                    student.setPicture(studentImage);
                    stmPicture.setBlob(1, studentImage);
                    stmPicture.setString(2, newId);
                    stmPicture.executeUpdate();
                }
                tblStudentInfo.getItems().add(student);
                connection.commit();
                btnNewStudent.fire();
                return;
            }
            PreparedStatement stmStudent = connection.prepareStatement(
                    "update Student set name=? where id=?");
            PreparedStatement stmPicture = connection.prepareStatement(
                    "update Picture set picture=? where student_id=?");

            stmStudent.setString(1, txtName.getText());
            stmStudent.setString(2, txtId.getText());
            stmStudent.executeUpdate();

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imgPicture.getImage(), null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            Blob newStudentImage = new SerialBlob(bytes);
            stmPicture.setBlob(1, newStudentImage);
            stmPicture.setString(2, txtId.getText());

            Student selectedItem = tblStudentInfo.getSelectionModel().getSelectedItem();
            selectedItem.setName(txtName.getText());
            selectedItem.setPicture(newStudentImage);
            tblStudentInfo.refresh();


        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private boolean isDataValid() {
        boolean dataValid = true;
        String name = txtName.getText();
        if (!name.matches("[A-Za-z ]+")) {
            txtName.selectAll();
            txtName.requestFocus();
            dataValid = false;
        }
        return dataValid;
    }

    private String generateNewId() {
        ObservableList<Student> studentList = tblStudentInfo.getItems();
        if (studentList.isEmpty()) {

            return "DEP-10/s-001";
        }
        String id = studentList.get(tblStudentInfo.getItems().size() - 1).getId().substring(9);

        return String.format("DEP-10/s-%03d", (Integer.parseInt(id) + 1));

    }

}
