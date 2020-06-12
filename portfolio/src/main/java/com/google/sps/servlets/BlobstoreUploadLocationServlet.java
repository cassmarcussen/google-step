package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the fetch() function requests the /blobstore-upload-url URL, the content of the response is
 * the URL that allows a user to upload a file to Blobstore. If this sounds confusing, try running a
 * dev server and navigating to /blobstore-upload-url to see the Blobstore URL.
 */
@WebServlet("/location-img-upload")
public class BlobstoreUploadLocationServlet extends HttpServlet {

  //called whenever Locations loads (in its body load)
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    System.out.println("doGet");
    Query query = new Query("LocationImgs");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<LocationImg> locationImgs = new ArrayList<LocationImg>();

    for (Entity entity : results.asIterable()) {

      String url = "/serve?blobkey=" + (String) entity.getProperty("imageUrl");
      String message = (String) entity.getProperty("message");
      long id = entity.getKey().getId();

      if (url != null && !url.contains("undefined")) {
        
        LocationImg img = new LocationImg(url, message, id);

        locationImgs.add(img);
      }
    
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");

    response.getWriter().println(gson.toJson(locationImgs));
    
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    System.out.println("doPost");

    // Get the message entered by the user.
    String message = request.getParameter("message");

    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");
    
    //Check for null, do not do post request if null url
    if (imageUrl == null || imageUrl.contains("undefined")) {
        response.sendRedirect("location_imgupload.jsp");
        return;
    }

    Entity entity = new Entity("LocationImgs");
    entity.setProperty("message", message);
    entity.setProperty("imageUrl", imageUrl);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);

    response.sendRedirect("/locations.html");

  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    BlobKey blobKey = blobKeys.get(0);
    return blobKey.getKeyString();
    
  }
}
