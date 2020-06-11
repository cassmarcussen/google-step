package com.google.sps.servlets;

/*LocationImg is one of the files that will be uploaded to the Location page representing 
an image to display on the page. */

public class LocationImg {

    private String imgUrl;
    private String imgMessage;
    private long itemId;

    public LocationImg(String url, String msg, long id) {
        imgUrl = url;
        imgMessage = msg;
        itemId = id;
    }
}