var express = require('express'),
    words   = require('./routes/words');

var app = express();

app.configure(function(){
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
});

// words CRUD
app.get('/words', words.findAll);
app.get('/words/:id', words.findById);
app.post('/words', words.addWord);
app.put('/words/:id', words.updateWord);
app.delete('/words/:id', words.deleteWord);

app.listen(3000);
console.log('Listening on port 3000...');