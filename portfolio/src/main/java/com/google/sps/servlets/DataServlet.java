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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.*;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    
    String queryType = request.getParameter("location");
    System.err.println("queryType: " + queryType);
    Query query = new Query(queryType);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> messages = new ArrayList<Comment>();

    int numIterations = 0;

    for (Entity entity : results.asIterable()) {
      if (numIterations >= globalNumComments) {
          break;
      }
      numIterations++;

      String name = (String) entity.getProperty("name");
      String message = (String) entity.getProperty("message");

      Comment newComment = new Comment(name, message);
      
      messages.add(newComment);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(messages));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("Add new message to Comments section");

    String name = request.getParameter("commenter-name");
    String newComment = request.getParameter("text-input");
    String entityType = request.getParameter("location");
    System.out.println("entityType: " + entityType);

    Location loc = Location.valueOf(entityType);

    Entity taskEntity = new Entity(entityType);
    taskEntity.setProperty("name", name);
    taskEntity.setProperty("message", newComment);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    globalNumComments = getNumComments(request);
    System.out.println("globalNumComments: " + globalNumComments);

    // Redirect back to the HTML page, using Location enum
    redirectPage(loc, response);

  }

  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String location = request.getParameter("location");
    Location loc = Location.valueOf(location);
    System.out.println("PUT location: " + location);

    globalNumComments = getNumComments(request);
    System.out.println("globalNumComments: " + globalNumComments);

    // Redirect back to the HTML page, using Location enum
    redirectPage(loc, response);
  }

  /** Returns the choice entered by the player, or -1 if the choice was invalid. */
  private int getNumComments(HttpServletRequest request) {

    String numCommentsString = request.getParameter("num-comments");

    // Convert the input to an int.
    int numComments = 10;
    try {
      numComments = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numCommentsString);
      numComments = 10;
    }
    
    if (numComments < 0) {
      System.err.println("Number of comments requested is out of range (must be nonnegative): " + numComments);
      numComments = 10;
    }
    
    return numComments;
  }

  // Redirect back to the HTML page, using Location enum
  private void redirectPage(Location loc, HttpServletResponse response) throws IOException {
    switch(loc) {
        case Comments: 
            response.sendRedirect("/step_projects.html");
            break;
        case Week1:
            response.sendRedirect("/step.html");
            break;
        case Week2:
            response.sendRedirect("/step.html");
            break;
        case Week3:
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
            //default is redirect to index
            response.sendRedirect("/index.html");
            break;
    }
  }

 /*
  globalNumComments (for the servlet) stores the number of comments the user has selected to display for the particular section they are displaying/hiding/posting to.
  The reason this variable has a default value of 10 is:
  - 10 is a reasonable amount of comments to want to display on the page. 10 comments do not take up too much space on the page while still giving the user a 
  good picture of what people are commenting.
  - It is optional for the user to select the number of comments to display on the page, so we need a default value in case the user does not choose 
  to select the number of comments to display.
 */
  int globalNumComments = 10;

    /*Location is an enum that keeps a record of all of the locations from which a request can be made. 
    Each Location is representative of a different comment section. For example, Comments is the 
    comments section in the Comments project of the STEP Projects page. Week1 and Week2 are the comments sections 
    for my Week 1 and Week 2 reflections in the STEP Internship page, correspondingly.
    */
   enum Location {
        Comments,
        Week1,
        Week2, 
        Week3,
        Challenges,
        Goals, 
        Funfacts, 
        Hobbies
   }
  
}
