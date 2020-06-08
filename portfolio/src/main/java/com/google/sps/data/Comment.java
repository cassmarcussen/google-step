package com.google.sps.servlets;

/*
Class for a comment to add to the website.
Contains name and message private fields.
*/
public class Comment {

    private String name;
    private String message;

    public Comment(String newName, String newComment){
        name = newName;
        message = newComment;
    }

    public String getName(){
        return name;
    }

    public void setName(String newName){
        name = newName;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String newMessage){
        message = newMessage;
    }

}