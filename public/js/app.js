// Imma write my own javascript. What you think I am, 3? 
$('body').off('.data-api');


$('.assignments').typeahead({
  'source' : ['Assignment 1', 'Assignment 2']
});



/****************************
  Data population stuff.
****************************/
(function(){

  // Some sample data to make sure population works. 
  var sampleWords = [
    {word: 'Hello', def: 'An exclamatory greeting'},
    {word: 'World', def: 'The earth, together with all of its countries, peoples, and natural features.'}
  ];

  var sampleAssignments = [
    {assn: "Assignment1"},
    {assn: "Assignment2"},
    {assn: "Assignment3"},
    {assn: "Assignment4"}
  ];

  // Handlebars templates
  var wordRow = Handlebars.compile(
    "<tr>" +
      "<td class='tbl-no'>{{number}}</td>" +
      "<td class='tbl-word'>{{word}}</td>" +
      "<td class='tbl-def'>{{def}}</td>" +
      "<td class='tbl-edit'>" +
          "<button class='btn btn-mini btn-primary'>Save</button>" +
          "<button class='btn btn-mini btn-danger'>Delete</button>" +
      "</td>" +
    "</tr>");

  var assnRow = Handlebars.compile(
    "<li{{#if active}} class='active'{{/if}}><a href='#'>{{assn}}</a></li>"
    );


  // Takes an array of word items and injects them into the DOM
  function populateWordList(data){
    // Do at least some sort of type checking.
    if(typeof data != "object" || !(data instanceof Array)){
      console.error("Passed invalid data: ", data);
    }

    for(var i in data){
      data[i].number = i;
      $(".word-list").append(wordRow(data[i]));
    }
  }

  // Takes an array of assignments (as strings) and injects them into the DOM
  function populateAssignmentList(data){
    // Do at least some sort of type checking.
    if(typeof data != "object" || !(data instanceof Array)){
      console.error("Passed invalid data: ", data);
    }

    for(var i in data){
      if(i == 0)
        data[i].active = true;

      console.log("appending ", assnRow(data[i]), data[i]);

      $("#assn-list").append(assnRow(data[i]));
    }
  }

  // Run on sample data;
  populateWordList(sampleWords);
  populateAssignmentList(sampleAssignments);
})();

/****************************
  Table editing stuff.
****************************/
$(".tbl-word, .tbl-def").click(function(){

  if($(this).find(".temp-input").length != 0){
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

  });

  $("temp-input").focus();
});

/****************************
  Assignment selecting stuff.
****************************/
$("#assn-list li").click(function(){
  $(this).siblings().removeClass("active");
  $(this).addClass("active");
});