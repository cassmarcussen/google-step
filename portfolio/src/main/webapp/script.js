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

/**
 * Adds a random fun fact to the page.
 */
function addRandomFunFact() {

  // Add it to the page in a table.
  var funFactTable = document.getElementById('funfact-table');

  if (unusedFunFacts.length > 0) {
        // Pick a random greeting.
        var randomIndex = Math.floor(Math.random() * unusedFunFacts.length);
        const funfact = unusedFunFacts[randomIndex];

        //Swap elt at index 0 and index randomIndex so that the "shift" method can be used to remove the used funfact
        const firstFunFact = unusedFunFacts[0];
        unusedFunFacts[0] = funfact;
        unusedFunFacts[randomIndex] = firstFunFact;
        unusedFunFacts.shift();

        factTableText += '<tr><td>' + funfact + '</td></tr>';
        document.getElementById('funfact-message').innerHTML = "";
        
        funFactTable.innerHTML = factTableText + '</table>';

  } else {
      document.getElementById('funfact-message').innerHTML = "Congrats - you've gone through all of the fun facts!";
  }

}

/**
 * Fetches the comments from the server and adds it to the DOM.
 Based on week-3-server/random-quotes/src/webapp/script.js from the Week-3-Server tutorial
 */
function getComments(myLocation) {

  // First, reset by updating num comments, which also hides comments
  refreshNumComments(myLocation);
  
  // Toggle between hide and display
  if (shouldDisplay) {

    console.log('Fetching comments.');
    document.getElementById(myLocation + "-get-button").innerHTML = "Hide Comments";

    setLocation(myLocation);

    // The fetch() function returns a Promise because the request is asynchronous.
    const responsePromise = fetch('/comments?location=' + myLocation, {
        location: myLocation
    });

    // When the request is complete, pass the response into handleResponse().
    responsePromise.then(handleResponse);

    /* For displaying comments, shouldDisplay controls whether the comments should be displayed or hidden.
    The reason for the existence of this variable is my decision to have the Display Comments button alternate between 
    "Display Comments" and "Hide Comments". I like the UI and display for this alternating button, which is why I have 
    implemented it in this way. We set shouldDisplay = false to indicate that, the next time we click our 'get Comments' 
    button, we should hide our comments, i.e. should not display them.c
    */
    shouldDisplay = false;
  } else {
    hideComments(myLocation);
  }
}

// Used in getComments to establish which location the comments should be loaded in
function setLocation(myLocation){
    currLocation = myLocation;
}

function getLocation(){
    return currLocation;
}

/**
 * Handles response by converting it to text and passing the result to
 * addCommentsToDom().
 */
function handleResponse(response) {

  console.log('Handling the response.');

  // response.text() returns a Promise, because the response is a stream of
  // content and not a simple variable.
  const textPromise = response.text();

  // When the response is converted to text, pass the result into the
  // addCommentsToDom() function.
  textPromise.then(addCommentsToDom);
}

/** Adds comments to the DOM. */
function addCommentsToDom(comments) {

    console.log('Adding comments to dom: ' + comments);

    const commentsContainer = document.getElementById('comments-container-' + getLocation());

    // Perhaps check that commments is a json array first...
    var commentArr = JSON.parse(comments);

    for (var i=0; i<commentArr.length; i++){

        var comment = commentArr[i];
        var commentName = commentArr[i].name;
        var commentMessage = commentArr[i].message;

        commentTableText += '<tr class="comment-table-row"><td class="comment-table-entry">' 
            + '<div class="commenter-name">' + commentName + '</div>'
            + '<div class="commenter-message">' + commentMessage + '</div>'
            + '</td></tr>';
    }

    commentTableText += '</table>';

    commentsContainer.innerHTML = commentTableText;
}

function displayCommentBoxes(sectionId){
    // If visible, hide and change button to "show"
    // If hidden, show and change button to "hide"
    if(document.getElementById("forms-wrapper-" + sectionId).style.display == "block"){
        document.getElementById("forms-wrapper-" + sectionId).style.display = "none";
        document.getElementById(sectionId + "-display-button").innerHTML = "Post a Comment";
    }else if(document.getElementById("forms-wrapper-" + sectionId).style.display != "block"){
        document.getElementById("forms-wrapper-" + sectionId).style.display = "block";
        document.getElementById(sectionId + "-display-button").innerHTML = "Hide Post Comment Boxes";
    }

}

function getNumComments(myLocation){

    numComments = document.getElementById("num-comments-" + myLocation).value;

    return numComments;

}

// Used in getComments, to make sure the num comments is up to date
function refreshNumComments(myLocation){
   
    var globalNumComments = document.getElementById("num-comments-" + myLocation).value;

    const updateNumComments = fetch('/comments?num-comments=' + globalNumComments + "&location=" + myLocation, { method: 'PUT'});
}

// Used in updating num comments, when the number should change and the comments should be hidden.
function updateNumComments(myLocation){

    refreshNumComments(myLocation);

    hideComments(myLocation);

}

function hideComments(myLocation){
    document.getElementById('comments-container-' + myLocation).innerText = "";

    commentTableText = `<table id="comment-table"> 
            <tr> 
                <th>Comments</th> 
            </tr>`;
    
    // Change innerHTML of the get button here because hideComments is called in updating the number of comments
    // to display, and we want to hide comments after changing the number of comments
    document.getElementById(myLocation + "-get-button").innerHTML = "Display Comments";
    shouldDisplay = true;

}

function deleteComments(myLocation){
    console.log('Deleting comments.');
    
    // Show a pop-up confirm box so the user doesn't accidentally delete all comments without confirmation
    if (confirm("By clicking 'OK', you will delete all comments.")) {
        // The fetch() function returns a Promise because the request is asynchronous.
        const responseDeletePromise = fetch('/delete-data?location=' + myLocation, { method: 'POST'});

        // Call the function to fetch comments from the server so that the now-deleted comments are removed from the page
        responseDeletePromise.then(getComments);

        /* location.reload() is a predefined JS function for a predefined JS class named location. */
        location.reload();
    }

}

/* Google Maps integration - Week 4 Libraries project */
/** Creates a map and adds it to the page. */
function createColumbiaMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 40.8075, lng: -73.9626}, zoom: 16});

  addFavoriteSpot(
      map, 40.8093798, -73.9599676, 'Mudd (CS Building)',
      'The CS building! You can often find me coding away here...')

  addFavoriteSpot(
      map, 40.806427, -73.961655, 'Hartley Hall',
      'My dorm room from Sophomore year! I lived in a suite with a couple friends, and across the hall from my girlfriend!');

  addFavoriteSpot(
      map, 40.808345, -73.960949, 'Avery',
      'My favorite library: Avery');

  addFavoriteSpot(
      map, 40.807928, -73.964338, 'Pret',
      'Pret - the best place to grab some espresso');

  addFavoriteSpot(
      map, 40.807854, -73.960917, 'Postcrypt Coffeehouse',
      'Postcrypt is one of the most special places on campus. It is a rather hidden music venue in the postcrypt of the chapel on campus, where student musicians can perform their pieces.');

  addFavoriteSpot(
      map, 40.806403, -73.96322, 'Butler Library',
      `Butler Library is an enormous, beautiful library on campus. To be honest, I spend most of my time there, at any time from the morning to midnight. 
      I have really fond memories of grabbing espresso and chips at Blue Java, the coffee shop inside the library, and expanding my set of knowledge while 
      reading and studying for hours on end. It's really fun when you bring friends along to study!`);

   addFavoriteSpot(
      map, 40.807845, -73.962125, 'Low Steps',
      `Low Steps (also known as Low Beach when it is sunny outside, due to the crowds of people who sit on the steps) is one of the most well-known places 
      on campus. It's an iconic place to take a photo if you are visiting campus. For me, when it's sunny outside, I love to study on the steps. This place is 
      especially special to me because it's one of the places my girlfriend and I spent our first date.`);

}

function createWildlifeManagementMap() {

    /* Create a map that displays all of Vermont */
    var map = new google.maps.Map(document.getElementById('map'), {
        zoom: 7,
        center: {lat: 43.8306202, lng: -72.309127}
    });

    // Set the stroke width, and fill color for each polygon
    map.data.setStyle({
        fillColor: 'blue',
        strokeWeight: 1
    });

    // NOTE: This uses cross-domain XHR, and may not work on older browsers.
    map.data.loadGeoJson(
        'https://geodata.vermont.gov/datasets/5c9be3e39d9945a0bf1d58e1e812a554_164.geojson');
}

/** Adds a marker that shows an info window when clicked. */
function addFavoriteSpot(map, lat, lng, title, description) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/* Week 4 - Blobstore usage on page accessible from the Locations page via a button (to location_imgupload.jsp). 
The form on the page uses Blobstore to allow users to post pictures of their favorite locations to the page. */

function fetchBlobstoreLocationsUrlAndShowForm() {
  fetch('/location-img-upload', {method: 'GET'})
      .then((response) => {
        return response.text();
      })
      .then((locationImg) => {
        
        const imagesLocationContainer = document.getElementById('location-imgs');

        // Perhaps check that commments is a json array first...
        var imgArr = JSON.parse(locationImg);

        var imgDiv = document.createElement("div");

        //nothing in here yet
        for (var i=0; i<imgArr.length; i++){

            var img = document.createElement("img");
            img.src = imgArr[i].imgUrl;

            if(img.src){

                //for one image
                var singularImageDiv = document.createElement("div");
                singularImageDiv.classList.add("gallery");

                var message = imgArr[i].imgMessage;
                var imgText = document.createElement("p");
                imgText.innerHTML = '<p>' + message + '</p>';
                imgText.classList.add("img-caption");

                singularImageDiv.appendChild(img);
                singularImageDiv.appendChild(imgText);

                imgDiv.appendChild(singularImageDiv);

            }

        }

        imagesLocationContainer.append(imgDiv);

      });
    
}


/* For displaying comments, shouldDisplay controls whether the comments should be displayed or hidden.
The reason for the existence of this variable is my decision to have the Display Comments button alternate between 
"Display Comments" and "Hide Comments". I like the UI and display for this alternating button, which is why I have 
implemented it in this way.
*/
var shouldDisplay = true;

/* currLocation stores the current location that we should display and delete comments from. */
var currLocation = "";

// Outside of function because we want to add to it each time addRandomFunFact is called
var factTableText = `<table style="width:100%"> 
            <tr> 
                <th>Fun Facts</th> 
            </tr>`;

var unusedFunFacts =
    ['I love hiking, biking, and spending time outdoors.', 
    'At Columbia, I am part of the Pops Orchestra, an ensemble dedicated to arranging and playing pop pieces for orchestra.',
    'I have two pet cats.',
    'My favorite movie is The Matrix.', 
    'I am a middle child - I have an older brother and two younger sisters.', 
    'My favorite food is garbanzo beans.', 
    'Currently, I am attempting to grow potatoes outside.',
    'My favorite animal is a panda.'];

var commentTableText = `<table id="comment-table"> 
            <tr> 
                <th>Comments</th> 
            </tr>`;



/*defaultNumComments stores the default number of comments to display on the screen.
It is set as const because it is not and should not be modified throughout the duration of the program running.
The reason this variable has a default value of 10 is:
- 10 is a reasonable amount of comments to want to display on the page. 10 comments do not take up too much space on the page while still giving the user a 
good picture of what people are commenting.
- It is optional for the user to select the number of comments to display on the page, so we need a default value in case the user does not choose 
to select the number of comments to display.
*/
const defaultNumComments = 10;

/*
globalNumComments (for the servlet) stores the number of comments the user has selected to display for the particular section they are displaying/hiding/posting to.
It keeps an updated number of comments, and since it changes but defaultNumComments does not, it needs to be separated from defaultNumComments.
*/
var globalNumComments = defaultNumComments;

