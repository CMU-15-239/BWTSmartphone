var assignments;
var rebind;
var renderWordList;

$(document).ready(function(){

  // Turn off Bootstrap's html-to-javascript features. 
  $('body').off('.data-api');


  /****************************
    Templates
  ****************************/

  // Handlebars template for a word table row.
  var wordRow = Handlebars.compile(
    "<tr>" +
      "<td class='tbl-no'>{{number}}</td>" +
      "<td class='tbl-word'>{{word}}</td>" +
      "<td class='tbl-def'>{{def}}</td>" +
      "<td class='tbl-edit'>" +
          "<button class='btn btn-mini btn-primary save-btn'>Save</button>" +
          "<button class='btn btn-mini btn-danger delete-btn'>Delete</button>" +
      "</td>" +
    "</tr>");

  // Handlebars template for an assignment row.
  var assnRow = Handlebars.compile(
    "<li class='{{#if active}}active {{/if}}assn-item' data-assn='{{assn}}'>"+
      "<a href='#'>{{assn}}</a>"+
    "</li>");


  /****************************
    Populators
  ****************************/

  // Takes an array of word items and injects them into the DOM
  function populateWordList(data){
    // Do at least some sort of type checking.
    if(typeof data != "object" || !(data instanceof Array)){
      console.error("Passed invalid data: ", data);
    }

    // For each item, add its number to its object and add it to the DOM.
    for(var i in data){
      data[i].number = parseInt(i) + 1;
      $(".word-list").append(wordRow(data[i]));
    }
  }



  /****************************
    Listeners
  ****************************/
  // Cause clicking on a table cell to convert it into a textbox.
  function rebindTable(){
    $(".tbl-word, .tbl-def").unbind('click');
    $(".tbl-word, .tbl-def").click(function(){

      // If this cell is already active, do nothing.
      if($(this).find(".temp-input").length !== 0){
        return;
      }

      var oldValue = $(this).html();

      var replacement = $("<input>")
        .attr("value", oldValue)
        .attr("style", "width: 100%")
        .addClass("temp-input");

      $(this).html(replacement);
      $(".temp-input").focus();

      $(".temp-input").blur(function(){

          $(this).after($(this).val());
          $(this).remove();
          validateRows();

      });

      $("temp-input").focus();
    });
  }

  // Repopulate the word list whenever an assignment is picked. 
  function rebindAssignments(){
    $(".assn-item").unbind('click');
    $(".assn-item").click(function(){
      $(this).siblings().removeClass("active");
      $(this).addClass("active");
      renderWordList($(this).attr("data-assn"));
    });
  }


  /****************************
    Validators
  ****************************/

  // Checks to see if any row has a word longer than 8 characters.  
  function validateRows(){
    var $words =$(".tbl-word");

    if($words.length <= 0)
      return;

    for(var i in $words){
      if($words[i].html().length > 8){
        $($words[i]).parent().addClass("error");
      }
      else{
        $($words[i]).parent().removeClass("error");
      }
    }
  }

  /****************************
    Data Parsing
  ****************************/


  function parseData(data){
    assignments = {};

    for(var i in data){
      var _assns = data[i].assns;
      for(var assn in _assns){
        if(!assignments[_assns[assn]])
          assignments[_assns[assn]] = [];

        assignments[_assns[assn]].push({
          word: data[i].word,
          def: data[i].def
        });
      }
    }

    for(var list in assignments){
      assignments[list].sort(function(a,b){
        return a.word.localeCompare(b.word);
      });
    }

    // Get an arbitrary first assignment.
    var first;
    for(first in assignments) break;

    // Display said arbitrary assignment.
    renderWordList(first);


    // Find all assignment and create typeahead
    var typeaheadList = [];

    for(var list in assignments){
      typeaheadList.push(list);
      var payload = {assn : list};

      if(list === first)
        payload.active = true;
      else
        payload.active = false;

      // Add assignments to the assignment list
      $("#assn-list").append(assnRow(payload));
    }

    // Initialize typeahead
    $('.assignments').typeahead({
      'source' : typeaheadList
    });

    // Set active assignment title. 
    $("#assn-title").html(first);


    console.log(assignments);
  }

  // Given an assignment name, populates the table with the associated words.
  function renderWordList(assn){
    // Throw out bad queries.
    if(assignments[assn] === null){
      console.error("Undefined assignment " + assn);
      return;
    }

    // Update table title.
    $("#assn-title").html(assn);
    //Empty the table
    $(".word-list").html("");
    //And populate it with new data
    populateWordList(assignments[assn]);

    rebindTable();
  }


  $.get("/words", function(data){
    console.log(data);
    console.log(parseData(data));
    rebindAssignments();
  });


});
