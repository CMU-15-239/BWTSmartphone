var assignments;

$(document).ready(function(){

  // Turn off Bootstrap's html-to-javascript features. 
  $('body').off('.data-api');

  // Enable typeahead.
  $('.assignments').typeahead({
    'source' : ['Assignment 1', 'Assignment 2']
  });

  /****************************
    Data population stuff.
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
    "<li class='{{#if active}}active {{/if}}assn-item' data-assn='{{assn}}'><a href='#'>{{assn}}</a></li>"
    );


  // Takes an array of word items and injects them into the DOM
  function populateWordList(data){
    // Do at least some sort of type checking.
    if(typeof data != "object" || !(data instanceof Array)){
      console.error("Passed invalid data: ", data);
    }

    // console.log("appending ", assnRow(data[i]), data[i]);

    for(var i in data){
      data[i].number = parseInt(i) + 1;
      $(".word-list").append(wordRow(data[i]));
    }
  }

  // Takes an array of assignments (as strings) and injects them into the DOM
  function populateAssignmentList(data){
    // Do at least some sort of type checking.
    if(typeof data != "object" || !(data instanceof Array)){
      console.error("Passed invalid data: ", data);
      return;
    }

    console.log("Populating with " + data);

    // Make the first list item active by default.
    for(var i in data){
      if(i === 0)
        data[i].active = true;
      else
        data[i].active = false;

      console.log("appending ", assnRow(data[i]), data[i]);

      $("#assn-list").append(assnRow(data[i]));
    }
  }


  /****************************
    Table editing stuff.
  ****************************/
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

  function validateRows(){
    var $words =$(".tbl-word");
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
    Assignment selecting stuff.
  ****************************/
  $("#assn-list li").click(function(){
    $(this).siblings().removeClass("active");
    $(this).addClass("active");
  });

  $.get("/words", function(data){
    console.log(data);
    console.log(parseData(data));
  });

  function parseData(data){
    assignments = {};

    for(var i in data){
      for(var assn in data[i].assns){
        if(!assignments[data[i].assns[assn]])
          assignments[data[i].assns[assn]] = [];

        assignments[data[i].assns[assn]].push({
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

    // Get an arbitrary first.
    var first;
    for(first in assignments) break;

    populateWordList(assignments[first]);

    for(var list in assignments){

      var payload = {assn : list};
      if(list === first)
        payload.active = true;
      else
        payload.active = false;

      console.log("Appending", payload);
      $("#assn-list").append(assnRow(payload));
    }

    $("#assn-title").html(first);

    console.log(assignments);
  }

  $(".assn-item").click(function(){
    populateWordList(assignments[$(this).attr("data-assn")]);
  });



});
