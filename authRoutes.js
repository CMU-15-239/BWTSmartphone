
var User = require('./User');
var Word = require('./Words');


// ******************************

module.exports = function (app) {

  app.post('/words', function(req, res) {
        if (req.user) {
            var word = new Word();
            for (var key in req.body) {
                console.log(req.body);
                console.log(key, req.body[key]);
               var value=req.body[key];
               word[key]=value;
            }
            word.save(function(err) {
                if (err) {
                    console.log(err, 'ruhroh');
                    return res.send({'err': err});
                }
                //console.log(student);
                return res.send(word);
            });
        }
        console.log(word);
    });

    app.get('/words', function(req, res){
         console.log('im loading user info');
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
         console.log('im loading user info');
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


  app.put('/words/:word', function(req, res) {
    var word = req.params.word;
    console.log('Updating word: ' + word);
    console.log(JSON.stringify(word));
    if ((req.user)){
      Word.find({'word': word}, null, function(err, success){
          if (err) {
              console.log(err);
              //throw err;
          }
          else {
              Word.remove(success);
              Word.create(req.body);
              console.log('successful');
              res.send(req.body);
          }});
    }


});

  app.delete('/words/:word' , function(req,res) {
    var word = req.params.word;
    console.log('Updating word: ' + word);
    if ((req.user)){
      Word.remove({'word' : word}, function(err){
        if(err){
          console.log(err);
          res.send({
            'result':'error',
            'message':err
          });
        }
        else{
          res.send({
            'result':'success'
          });
        }
      });

    }
  });
};
