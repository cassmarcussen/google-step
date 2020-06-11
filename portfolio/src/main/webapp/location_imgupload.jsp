
<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/location-img-upload"); %>

<!DOCTYPE html>
<html>
<head>
  <title>Cassandra's Portfolio</title>
  <script src="script.js"></script>
  <link rel="stylesheet" href="style.css">
  <link href="https://fonts.googleapis.com/css2?family=B612&display=swap" rel="stylesheet">
</head>
  <body>
    <div class="navbar">
        <a href="locations.html">Return to Locations</a>
    </div>
    <div id="content">
        <div class="boxed-div-colorful">
            <h1>Submit a photo and caption of your favorite location</h1>
            <p>Type a message, upload an image, and click submit below.</p>
        </div>
        <div class="boxed-div">
            <form method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
            <p>Caption your favorite location:</p>
            <textarea name="message"></textarea>
            <br/>
            <p>Upload an image:</p>
            <input type="file" name="image">
            <br/><br/>
            <button>Submit</button>
            </form>
        </div>
    </div>
  </body>
</html>