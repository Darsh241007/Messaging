package com.darsh.messaging;


public class Friend {
 private String Name;
 private String phone;

 public Friend(String Name,String phone){
     this.Name=Name;
     this.phone=phone;
 }

 public Friend(){

 }

    public String getName(){
        return Name;
    }
    public void setName(String Name){ this.Name = Name; }

    String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
}
