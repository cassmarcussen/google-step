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

  //first, reset by updating num comments, which also hides comments
  refreshNumComments(myLocation);
  
  //toggle between hide and display
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

    if (commentsAdded == 0) {
        console.log('Adding comments to dom: ' + comments);

        const commentsContainer = document.getElementById('comments-container-' + getLocation());
        console.log('comments-container-' + location);

        //perhaps check that commments is a json array first...
        var commentArr = JSON.parse(comments);
        console.log('Comment arr: ' + commentArr);

        for (var i=0; i<commentArr.length; i++){

            var comment = commentArr[i];
            var commentName = commentArr[i].name;
            var commentMessage = commentArr[i].message;

            console.log('Comment: ' + commentName + ', ' + commentMessage);

            commentTableText += '<tr class="comment-table-row"><td class="comment-table-entry">' 
                + '<div class="commenter-name">' + commentName + '</div>'
                + '<div class="commenter-message">' + commentMessage + '</div>'
                + '</td></tr>';
        }

        commentTableText += '</table>';

        commentsContainer.innerHTML = commentTableText;

        commentsAdded = 1;
    }
}

function displayCommentBoxes(sectionId){
    //if visible, hide and change button to "show"
    //if hidden, show and change button to "hide"
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

//Used in getComments, to make sure the num comments is up to date
function refreshNumComments(myLocation){
   
    var globalNumComments = document.getElementById("num-comments-" + myLocation).value;

    const updateNumComments = fetch('/comments?num-comments=' + globalNumComments + "&location=" + myLocation, { method: 'PUT'});
}

//Used in updating num comments, when the number should change and the comments should be hidden.
function updateNumComments(myLocation){

    refreshNumComments(myLocation);

    hideComments(myLocation);

}

function hideComments(myLocation){
    document.getElementById('comments-container-' + myLocation).innerText = "";
    //Indicate that the comments are no longer added
    commentsAdded = 0;
    commentTableText = `<table id="comment-table"> 
            <tr> 
                <th>Comments</th> 
            </tr>`;
    
    //Change innerHTML of the get button here because hideComments is called in updating the number of comments
    //to display, and we want to hide comments after changing the number of comments
    document.getElementById(myLocation + "-get-button").innerHTML = "Display Comments";
    shouldDisplay = true;

}

function deleteComments(myLocation){
    console.log('Deleting comments.');
    
    //Show a pop-up confirm box so the user doesn't accidentally delete all comments without confirmation
    if (confirm("By clicking 'OK', you will delete all comments.")) {
        // The fetch() function returns a Promise because the request is asynchronous.
        const responseDeletePromise = fetch('/delete-data?location=' + myLocation, { method: 'POST'});

        //Call the function to fetch comments from the server so that the now-deleted comments are removed from the page
        responseDeletePromise.then(getComments);

        location.reload();
    }

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

var commentsAdded = 0;

 /*
  globalNumComments (for the client) stores the number of comments the user has selected to display for the particular section they are displaying/hiding/posting to.
  The reason this variable has a default value of 10 is:
  - 10 is a reasonable amount of comments to want to display on the page. 10 comments do not take up too much space on the page while still giving the user a 
  good picture of what people are commenting.
  - It is optional for the user to select the number of comments to display on the page, so we need a default value in case the user does not choose 
  to select the number of comments to display.
 */
var globalNumComments = 10;

