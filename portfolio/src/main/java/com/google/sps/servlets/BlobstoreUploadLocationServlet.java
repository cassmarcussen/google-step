package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * When the fetch() function requests the /blobstore-upload-url URL, the content of the response is
 * the URL that allows a user to upload a file to Blobstore. If this sounds confusing, try running a
 * dev server and navigating to /blobstore-upload-url to see the Blobstore URL.
 */
@WebServlet("/blobstore-upload-location-url")
public class BlobstoreUploadLocationServlet extends HttpServlet {

  //called whenever Locations loads (in its body load)
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl("/location-blobstore-form");
    
    System.out.println("doGet");
    Query query = new Query("LocationFiles");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<LocationImg> locationImgs = new ArrayList<LocationImg>();

    LocationImg img2 = new LocationImg("hi", "byeeee", (long)0);

    locationImgs.add(img2);

    for (Entity entity : results.asIterable()) {
      
      String url = (String) entity.getProperty("imageUrl");
      String message = (String) entity.getProperty("message");
      long id = entity.getKey().getId();

      LocationImg img = new LocationImg(url, message, id);

      locationImgs.add(img);
    
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");

    response.getWriter().println(gson.toJson(locationImgs));
    
  }
}
