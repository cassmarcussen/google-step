package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns global warming sentiment data as a JSON object */
@WebServlet("/global-warming-sentiment-data")
public class GlobalWarmingDataServlet extends HttpServlet {

  private LinkedHashMap<String, Integer> globalWarmingSentiment = new LinkedHashMap<>();

  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(
        "/WEB-INF/global-warming-sentiment-data.csv"));
    
    // Initialize the count of "Yes" and "No" seen so far to 0
    int countOfYes = 0;
    int countOfNo = 0;

    // In the while loop, count and increment the number of "Yes" and "No" entries in the dataset
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] tweetAndSentiment = line.split(",");

      /* Yes represents that a person's tweet implies a belief that climate change exists, 
      and No represents that a person's tweet implies a belief that climate change does not exist.
      */

      if (((String) tweetAndSentiment[1]).equals("Yes")) {
          countOfYes++;
      } else if (((String) tweetAndSentiment[1]).equals("No")) {
          countOfNo++;
      } else {
          // Go to the next iteration of the while loop, because we don't want to add in values such as N/A.
          continue;
      }
    
    }

    globalWarmingSentiment.put("Yes", countOfYes);
    globalWarmingSentiment.put("No", countOfNo);

    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(globalWarmingSentiment);
    response.getWriter().println(json);
  }
}