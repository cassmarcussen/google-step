package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns global warming sentiment data as a JSON object, e.g. {"2017": 52, "2018": 34}] */
@WebServlet("/global-warming-sentiment-data")
public class GlobalWarmingDataServlet extends HttpServlet {

  private LinkedHashMap<String, Integer> globalWarmingSentiment = new LinkedHashMap<>();

  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(
        "/WEB-INF/global-warming-sentiment-data.csv"));
    
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String tweet = (String) cells[0];
      Integer existence = 0;
      if(((String) cells[1]).equals("Yes")){
          existence = 1;
      }else if (((String) cells[1]).equals("No")){
          existence = 0;
      }else{
          //Go to the next iteration of the while loop, because we don't want to add in values such as N/A.
          continue;
      }

      globalWarmingSentiment.put(tweet, existence);
    }
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