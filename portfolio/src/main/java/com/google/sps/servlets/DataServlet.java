// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

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

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  /* doGet gets the comments from DataStore to display them in the current commenting location on the page. 
  This function is executed when the user selects to display comments on the page using the corresponding button "Display Comments".*/
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    
    /* location is a hidden input variable in the HTML for the comments sections. 
    Since my website has multiple comments sections on the different pages, I need to specify which 
    comment section to retrieve the comments for. This is specified by the location parameter. */
    String queryType = request.getParameter("location");

    Query query = new Query(queryType);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> messages = new ArrayList<Comment>();

    for (Entity entity : results.asIterable()) {
      if (messages.size() >= globalNumComments) {
          break;
      }

      String name = (String) entity.getProperty("name");
      String message = (String) entity.getProperty("message");

      Comment newComment = new Comment(name, message);
      
      messages.add(newComment);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(messages));
  }

  /* doPost posts comments to the current commenting location. 
  This function is executed when the user clicks the button to post a new comment to the page in the 
  new comments entry form. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("Add new message to Comments section");

    String name = request.getParameter("commenter-name");
    String newComment = request.getParameter("text-input");

    /* location is a hidden input variable in the HTML for the comments sections. 
    Since my website has multiple comments sections on the different pages, I need to specify which 
    comment section to post a comment to. This is specified by the location parameter. */
    String entityType = request.getParameter("location");

    Location loc;
    /* In writing the comments to the page, we need "location" to be specific to the particular week
     (e.g. Week1, Week2, etc.). But, for the purpose of reloading the page in the Java code, we only need 
     to know we are writing comments to a Week. So, we assign all weeks to Location enum value Week. */
    if (entityType.length() > 4 && entityType.substring(0, 4).equals("Week")) {
        loc = Location.valueOf("Week");
    } else{
        loc = Location.valueOf(entityType);
    }

    Entity taskEntity = new Entity(entityType);
    taskEntity.setProperty("name", name);
    taskEntity.setProperty("message", newComment);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    globalNumComments = getNumComments(request);

    // Redirect back to the HTML page, using Location enum
    redirectPage(loc, response);

  }

  /* doPut updates the number of comments to display for the current commenting location. 
  The reason I am using doPut is because, when updating the number of comments in the POST request, 
  this resulted in content validation on the server side as well as confusing checks. Pulling out 
  updating the number of comments to doPut provides the clarity and functionality we need. 
  This function is executed when the user clicks the button on the page to update the number of comments.
  */
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

    /* location is a hidden input variable in the HTML for the comments sections. 
    Since my website has multiple comments sections on the different pages, I need to specify which 
    comment section to update the number of comments for. This is specified by the location parameter. */
    String location = request.getParameter("location");
    Location loc;

    /* In writing the comments to the page, we need "location" to be specific to the particular week
     (e.g. Week1, Week2, etc.). But, for the purpose of reloading the page in the Java code, we only need 
     to know we are writing comments to a Week. So, we assign all weeks to Location enum value Week. */
    if (location.length() > 4 && location.substring(0, 4).equals("Week")) {
        loc = Location.valueOf("Week");
    } else{
        loc = Location.valueOf(location);
    }

    globalNumComments = getNumComments(request);

    // Redirect back to the HTML page, using Location enum
    redirectPage(loc, response);
  }

  private int getNumComments(HttpServletRequest request) {

    String numCommentsString = request.getParameter("num-comments");

    // Convert the input to an int.
    int numComments = defaultNumComments;
    try {
      numComments = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numCommentsString);
    }
    
    if (numComments < 0) {
      System.err.println("Number of comments requested is out of range (must be nonnegative): " + numComments);
    }
    
    return numComments;
  }

  // Redirect back to the HTML page, using Location enum
  private void redirectPage(Location loc, HttpServletResponse response) throws IOException {
    switch(loc) {
        case Comments: 
            response.sendRedirect("/step_projects.html");
            break;
        case Week:
            response.sendRedirect("/step.html");
            break;
        case Challenges:
            response.sendRedirect("/step.html");
            break;
        case Goals:
            response.sendRedirect("/step.html");
            break;
        case Funfacts:
            response.sendRedirect("/funfacts.html");
            break;
        case Hobbies:
            response.sendRedirect("/hobbies.html");
            break;
        default: 
            // Default is redirect to index
            response.sendRedirect("/index.html");
            break;
    }
  }

/*defaultNumComments stores the default number of comments to display on the screen.
It is set as final because it is not and should not be modified throughout the duration of the program running.
The reason this variable has a default value of 10 is:
- 10 is a reasonable amount of comments to want to display on the page. 10 comments do not take up too much space on the page while still giving the user a 
good picture of what people are commenting.
- It is optional for the user to select the number of comments to display on the page, so we need a default value in case the user does not choose 
to select the number of comments to display.
*/
final int defaultNumComments = 10;

/*
globalNumComments (for the servlet) stores the number of comments the user has selected to display for the particular section they are displaying/hiding/posting to.
It keeps an updated number of comments, and since it changes but defaultNumComments does not, it needs to be separated from defaultNumComments.
*/
int globalNumComments = defaultNumComments;


/*Location is an enum that keeps a record of all of the locations from which a request can be made. 
Each Location is representative of a different comment section. For example, Comments is the 
comments section in the Comments project of the STEP Projects page. Week1 and Week2 are the comments sections 
for my Week 1 and Week 2 reflections in the STEP Internship page, correspondingly.
*/
enum Location {
    Comments,
    Week,
    Challenges,
    Goals, 
    Funfacts, 
    Hobbies
}
  
}
