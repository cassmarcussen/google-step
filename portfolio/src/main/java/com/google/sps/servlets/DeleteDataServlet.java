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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for deleting tasks. */
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      
    String queryType = request.getParameter("location");
    Location loc = Location.valueOf(queryType);
    System.out.println("delete queryType: " + queryType);
    Query query = new Query(queryType);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
       datastore.delete(entity.getKey());
    }

    // Redirect back to the HTML page.
    redirectPage(loc, response);
  }

  /*Location is an enum that keeps a record of all of the locations from which a request can be made. 
    Each Location is representative of a different comment section. For example, Comments is the 
    comments section in the Comments project of the STEP Projects page. Week1 and Week2 are the comments sections 
    for my Week 1 and Week 2 reflections in the STEP Internship page, correspondingly.
    */
   enum Location {
        Comments,
        Week1,
        Week2, 
        Week3
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
        default: 
            //default is redirect to index
            response.sendRedirect("/index.html");
            break;
    }
  }

}