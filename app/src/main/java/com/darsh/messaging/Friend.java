package com.darsh.messaging;


public class Friend {
 private String Name;
 private String Phone;

 public Friend(String Name,String Phone){
     this.Name=Name;
     this.Phone=Phone;
 }

 public Friend(){

 }

    public String getName(){
        return Name;
    }
    public void setName(String Name){ this.Name = Name; }

    String getPhone(){
        return Phone;
    }
    public void setPhone(String Phone){
        this.Phone = Phone;
    }
}
