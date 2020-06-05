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

 var display = true;
function getComments(myLocation) {

  //first, reset by updating num comments, which also hides comments
  updateNumComments(myLocation);
  
  //toggle between hide and display
  if (display) {

    console.log('Fetching comments.');
    document.getElementById(myLocation + "-get-button").innerHTML = "Hide Comments";

    setLocation(myLocation);

    // The fetch() function returns a Promise because the request is asynchronous.
    const responsePromise = fetch('/comments?location=' + myLocation, {
        location: myLocation
    });

    // When the request is complete, pass the response into handleResponse().
    responsePromise.then(handleResponse);

    display = false;
  } else {
    document.getElementById(myLocation + "-get-button").innerHTML = "Display Comments";
    display = true;
  }
}

var currLocation = "";
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

function getNumComments(){

    numComments = document.getElementById("num-comments").value;

    return numComments;

}

function updateNumComments(myLocation){

    var globalNumComments = document.getElementById("num-comments-" + myLocation).value;

    const updateNumComments = fetch('/comments?num-comments=' + globalNumComments + "&location=" + myLocation, { method: 'POST'});

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
    
    document.getElementById(myLocation + "-get-button").innerHTML = "Display Comments";
    display = true;
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

//outside of function because we want to add to it each time addRandomFunFact is called
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
var globalNumComments = 10;

