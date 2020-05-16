package com.darsh.messaging;

import android.widget.ImageView;

import java.util.Date;

public class Message {

    private String message;
    private String by;
    private Date timestamp;
    private String imageURL;
    private String imageLocation;
    private String id;


    public Message(String message, String by, Date timestamp,String imageURL,String imageLocation) {
        this.message = message;
        this.by = by;
        this.timestamp = timestamp;
        this.imageURL = imageURL;
        this.imageLocation = imageLocation;

    }

    public Message(){

    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }
    public String getBy(){
        return by;
    }
    public void setBy(String by){
        this.by=by;
    }
    public Date getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(Date timestamp){
        this.timestamp=timestamp;
    }
    public void setImageURL(String imageURL){this.imageURL = imageURL;}
    public String getImageURL() {return imageURL;}
    public void setImageLocation(String imageLocation){this.imageLocation = imageLocation;}
    public String getImageLocation() {return imageLocation;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
