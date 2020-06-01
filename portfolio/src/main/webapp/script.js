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

        const funFactRowText = '<tr><td>' + funfact + '</td></tr>';
        tableText += '<tr><td>' + funfact + '</td></tr>';
        countTableEntries++;
        document.getElementById('funfact-message').innerHTML = "";
        
        funFactTable.innerHTML = tableText + '</table>';

  } else {
      document.getElementById('funfact-message').innerHTML = "Congrats - you've gone through all of the fun facts!";
  }

}

/**
 * Fetches a "Hello Cassandra" statement from the server and adds it to the DOM.
 Based on week-3-server/random-quotes/src/webapp/script.js from the Week-3-Server tutorial
 */
function getHello() {
  console.log('Fetching a Hello statement.');

  // The fetch() function returns a Promise because the request is asynchronous.
  const responsePromise = fetch('/hello-response');

  // When the request is complete, pass the response into handleResponse().
  responsePromise.then(handleResponse);
}

/**
 * Handles response by converting it to text and passing the result to
 * addHelloToDom().
 */
function handleResponse(response) {
  console.log('Handling the response.');

  // response.text() returns a Promise, because the response is a stream of
  // content and not a simple variable.
  const textPromise = response.text();

  // When the response is converted to text, pass the result into the
  // addHelloToDom() function.
  textPromise.then(addHelloToDom);
}

/** Adds a random quote to the DOM. */
function addHelloToDom(helloStatement) {
  console.log('Adding "Hello Cassandra" to dom: ' + helloStatement);

  const helloContainer = document.getElementById('hello-fetch-container');
  helloContainer.innerText = helloStatement;
}

//outside of function because we want to add to it each time addRandomFunFact is called
var tableText = `<table style="width:100%"> 
            <tr> 
                <th>Fun Fact</th> 
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

var countTableEntries = 0;
