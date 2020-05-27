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
 * Adds a random greeting to the page.
 */
function addRandomFunFact() {
  const funfacts =
      ['I love hiking, biking, and spending time outdoors.', 
      'At Columbia, I am part of the Pops Orchestra, an ensemble dedicated to arranging and playing pop pieces for orchestra.',
       'I have two pet cats.',
        'My favorite movie is The Matrix.', 
        'I am a middle child - I have an older brother and two younger sisters.', 
        'My favorite food is garbanzo beans.', 
        'Currently, I am attempting to grow potatoes outside.',
        'My favorite animal is a panda.'];

  // Pick a random greeting.
  const funfact = funfacts[Math.floor(Math.random() * funfacts.length)];

  // Add it to the page in a talbe.
  var funFactTable = document.getElementById('funfact-table');
  const funFactRowText = '<tr><td>' + funfact + '</td></tr>';

  if(!tableText.includes(funFactRowText)){
        tableText += '<tr><td>' + funfact + '</td></tr>';
        countTableEntries++;
        document.getElementById('funfact-message').innerHTML = "";
  }else if(countTableEntries < funfacts.length){
      document.getElementById('funfact-message').innerHTML = "Repeat fact - try again!";
  }else{
      //need to deal with edge case that the table has all the entries more elegantly
      document.getElementById('funfact-message').innerHTML = "Congrats - you've gone through all of the fun facts!";
  }
  
  funFactTable.innerHTML = tableText + '</table>';
}

//outside of function because we want to add to it each time addRandomFunFact is called
var tableText = `<table style="width:100%"> 
            <tr> 
                <th>Fun Fact</th> 
            </tr>`;

var countTableEntries = 0;
