
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
        if (req.user) {
            var word = new Word();
            for (var key in req.body) {
                console.log(req.body)
                console.log(key, req.body[key]);
               var value=req.body[key];
               word[key]=value;
            };
            word.save(function(err) {
                if (err) {
                    console.log(err, 'ruhroh');
                    return res.send({'err': err});
                }
                //console.log(student);
                return res.send(word);
            });  
        };
        console.log(word)
    });

    app.get('/words', function(req, res){
         console.log('im loading user info')
        if (!req.user){
            console.log('401');
            res.status(401);
        }
        if ((req.user)){
            Word.find({}, function (err, words){
                if (err) {
                    console.log(err, 'tryin to find the username');
                    res.send(err);
                }
                else {
                    res.send(words);
                }});
        }
    });

  app.get('/words/:id', function(req, res) {
         console.log('im loading user info')
         var id = req.params.word;
        if (!req.user){
            console.log('401');
            res.status(401);
        }
        if ((req.user)){
            Word.findOne({'_id':new BSON.ObjectID(id)}, function (err, user){
                if (err) {
                    console.log(err, 'tryin to find the username');
                    res.send(err);
                }
                else {
                    res.send(user);
                }});
        }
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
