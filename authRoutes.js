
var User = require('./User');
var Word = require('./Words')

// var mongo = require('mongodb');
// var Server = mongo.Server;
// var Db = mongo.Db;
// var BSON = mongo.BSONPure;


// var server = new Server('localhost', 27017, {auto_reconnect : true});
// db = new Db('wordsdb', server, {safe : true});

// db.open(function(err,db) {
//   if(!err) {
//     console.log("Connected to 'wordsdb' database");
//     db.collection('words', {safe:true}, function(err, collection) {
//       if(err) {
//         console.log("The 'words' collection doesn't exist. Creating it with sample data...");
//         populateDB();
//       }
//     });
//   }
// });

// var populateDB = function(){
//   var words = [
//     {
//       word: "hello",
//       def: "A simple greeting",
//       pos: "exclamation",
//       assns: []
//     },
//     {
//       word: "world",
//       def: "The earth, together with all of its countries, peoples, and natural features.",
//       pos: "noun",
//       assns: []
//     }];

//     db.collection('words', function(err, collection){
//       collection.insert(words, {safe:true}, function(err, result) {});
//     });
// };
 
// ******************************

module.exports = function (app) {

  app.post('/words', function(req, res) {
    var word = req.body;
    console.log('Adding word: ' + JSON.stringify(word));
    db.collection('words', function(err, collection) {
      collection.insert(word, {safe : true}, function(err, result) {
        if(err) {
          res.send({'error' : 'An error has occurred'});
        }
        else {
          console.log('Success: ' + JSON.stringify(result[0]));
          res.send(result[0]);
        }
      });
    });
  });

  app.get('words', function(req, res) {
    db.collection('words', function(err, collection) {
      collection.find().toArray(function(err, items) {
        res.send(items);
      });
    });
  });

  app.get('/words/:id', function(req, res) {
    var id = req.params.word;
    console.log('Retrieving word: ' + id);
    db.collection('words', function(err, collection) {
      collection.findOne({'_id':new BSON.ObjectID(id)}, function(err, item) {
        res.send(item);
      });
    });
  });

  app.put('/words/:id', function(req, res) {
    var id = req.params.word;
    var word = req.body;
    console.log('Updating word: ' + id);
    console.log(JSON.stringify(word));
    db.collection('words', function(err, collection) {
      collection.update({'_id':new BSON.ObjectID(id)}, wine, {safe:true}, function(err, result){
        if (err) {
          console.log('Error updating word: ' + err);
        }
        else {
          console.log('' + result + 'document(s) updated');
          res.send(word);
        }
      });
    });
  });

  app.delete('/words/:id' , function(req,res) {
    var id = req.params.id;
    console.log('Deleting word: ' + id);
    db.collection('words', function(err,collection) {
      collection.remove({'_id':new BSON.ObjectID(id)}, {safe:true}, function(err, result) {
        if (err) {
          res.send({'error' : 'An error has occurred - ' + err});
        }
        else {
          console.log('' + result + ' document(s) deleted');
          res.send(req.body);
        }
      });
    });
  });

};
