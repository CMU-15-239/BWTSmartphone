$('.assignments').typeahead({
  'source' : ['Assignment 1', 'Assignment 2']
});

// Some sample data to make sure population works. 
var sampleWords = [
  {word: 'Hello', def: 'An exclamatory greeting'},
  {word: 'World', def: 'The earth, together with all of its countries, peoples, and natural features.'}
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




// Takes an array of word items and injects them into the DOM
function populateWordList(data){
  // Do at least some sort of type checking.
  if(typeof data != "object" || !(data instanceof Array)){
    console.error("Passed invalid data: ", data);
  }

  for(i in data){
    data[i].number = i;
    $(".word-list").append(wordRow(data[i]));
  }
}

// Run on sample data;
populateWordList(sampleWords);