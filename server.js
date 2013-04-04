var express = require('express'),
    words   = require('./routes/words');

var app = express();

app.configure(function(){
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
});

app.use(express.static(__dirname + '/public'));


// words CRUD
app.get('/words', words.findAll);
app.get('/words/:id', words.findById);
app.post('/words', words.addWord);
app.put('/words/:id', words.updateWord);
app.delete('/words/:id', words.deleteWord);

app.get('/',express.static(__dirname + '/frontend'));


app.listen(3000);
console.log('Listening on port 3000...');