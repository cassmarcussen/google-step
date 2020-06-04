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

  if (unusedFunFacts.length > 0){
        // Pick a random greeting.
        var randomIndex = Math.floor(Math.random() * unusedFunFacts.length);
        const funfact = unusedFunFacts[randomIndex];

        //Swap elt at index 0 and index randomIndex so that the "shift" method can be used to remove the used funfact
        const firstFunFact = unusedFunFacts[0];
        unusedFunFacts[0] = funfact;
        unusedFunFacts[randomIndex] = firstFunFact;
        unusedFunFacts.shift();

        //const funFactRowText = '<tr><td>' + funfact + '</td></tr>';
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
  console.log('Fetching comments.');

  // The fetch() function returns a Promise because the request is asynchronous.
  const responsePromise = fetch('/comments', {
      location: myLocation
  });

  // When the request is complete, pass the response into handleResponse().
  responsePromise.then(handleResponse);
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

    if(commentsAdded == 0){
        console.log('Adding comments to dom: ' + comments);

        const commentsContainer = document.getElementById('comments-container');

        //should I check that commments is a json array first?
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

        //globalNumComments = getNumComments();
        
        document.getElementById("num-comments").value = globalNumComments;
       // console.log("numComments: " + numComments);
        document.getElementById("curr-num-comments").innerText = globalNumComments;

        commentTableText += '</table>';

        commentsContainer.innerHTML = commentTableText;

        commentsAdded = 1;
    }
}

function getNumComments(){

   /* var nuCommentsArr = window.location.search.split("?num-comments=");
    var numComments = 10;
    if(numComments.length > 1){
            //works only b/c one query param... change to be extensible
        numComments =  numCommentsArr[1];
    }*/

    numComments = document.getElementById("num-comments").value;

    return numComments;

}

function updateNumComments(){

    var globalNumComments = document.getElementById("num-comments").value;

    document.getElementById("curr-num-comments").innerHTML = globalNumComments;

    const updateNumComments = fetch('/comments?num-comments=' + globalNumComments, { method: 'POST'});

    //updateNumComments.then(location.reload());

}

/*function setNumComments(){
    var numComments = document.getElementById('num-comments');
    var searchParams = new URLSearchParams(window.location.href);
    searchParams.set("num-comments", numComments);
    window.location.href = searchParams.toString();
}*/

function hideComments(){
    document.getElementById('comments-container').innerText = "";
    //Indicate that the comments are no longer added
    commentsAdded = 0;
    commentTableText = `<table id="comment-table"> 
            <tr> 
                <th>Comments</th> 
            </tr>`;
}

function deleteComments(){
    console.log('Deleting comments.');
    
    //Show a pop-up confirm box so the user doesn't accidentally delete all comments without confirmation
    if (confirm("By clicking 'OK', you will delete all comments.")) {
        // The fetch() function returns a Promise because the request is asynchronous.
        const responseDeletePromise = fetch('/delete-data', { method: 'POST'});

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

