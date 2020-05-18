package com.darsh.messaging;


import java.util.Date;

public class Message {

    private String message;
    private String by;
    private Date timestamp;
    private String imageLocation;
    private String id;



    public Message(String message, String by, Date timestamp,String imageLocation) {
        this.message = message;
        this.by = by;
        this.timestamp = timestamp;
        this.imageLocation = imageLocation;

    }

    String getMessage(){
        return message;
    }

    String getBy(){
        return by;
    }

    Date getTimestamp(){
        return timestamp;
    }

    String getImageLocation() {return imageLocation;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
