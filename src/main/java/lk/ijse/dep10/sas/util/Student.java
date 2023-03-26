package lk.ijse.dep10.sas.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

public class Student implements Serializable {
    private String id;
    private String name;
    private Blob picture;

    public Student() {
    }

    public Student(String id, String name, Blob picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getPicture() {
       return picture;
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }
    public ImageView getPictureToTable(){

        try {
            Image image;
            if (picture !=null){
                image=new Image(picture.getBinaryStream(),30,30,false,false);
                return new ImageView(image);
            }
            image=new Image("/images/No_Image_Available.jpg",30,30,false,false);
            return new ImageView(image);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
