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

    Query query = new Query("Comment");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> messages = new ArrayList<Comment>();

    /*globalNumComments = getNumComments(request);
    System.err.println("globalNumComments: " + globalNumComments);*/

    //globalNumComments = 3;

    int numIterations = 0;

    for (Entity entity : results.asIterable()) {
      if(numIterations >= globalNumComments){
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

    if(newComment != null){

        Entity taskEntity = new Entity("Comment");
        taskEntity.setProperty("name", name);
        taskEntity.setProperty("message", newComment);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(taskEntity);

    }

    globalNumComments = getNumComments(request);
    System.out.println("globalNumComments: " + globalNumComments);

    //request.setAttribute("num-comments", globalNumComments);

    // Redirect back to the HTML page.
    response.sendRedirect("/step_projects.html");

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

    // Check that the input is between 1 and 3.
    if (numComments < 0) {
      System.err.println("Number of comments requested is out of range (must be nonnegative): " + numComments);
      numComments = 10;
    }
    
    return numComments;
  }

  int globalNumComments = 10;
  
}

