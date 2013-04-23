var path = require('path');
var express = require('express');
var http = require('http');
var mongoose = require('mongoose');
var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;

var mongo = require('mongodb');

/* Connect to the local instance of mongo. */ 
var mongoUri = process.env.MONGOLAB_URI || process.env.MONGOHQ_URL || 'mongodb://localhost/myApp';
var mongoOptions = { db: { safe: true }};
mongoose.connect(mongoUri, mongoOptions, function (err, res) {
  if (err) {
    console.log ('ERROR connecting to: ' + mongoUri + '. ' + err);
  } else {
    console.log ('Succeeded connected to: ' + mongoUri);
  }
});

var portNumber = process.env.PORT || 3000;

/* Initialize the node app, include express and passport.
 */
function init(){
  var app = express();
  configureExpress(app);

  var User = initPassportUser();
  var Word = require('./Words');

  checkForAndCreateRootUser(User);
  checkDefaultWords(Word);

  require('./loginRoutes')(app);
  require('./authRoutes')(app);

  http.createServer(app).listen(3000, function() {
    console.log("Express server listening on port %d", portNumber);
  });
}

init();

/* Configure express and passport.
 */
function configureExpress(app){
  app.configure(function(){
    app.use(express.bodyParser());
    app.use(express.methodOverride());

    app.use(express.cookieParser('i am a really creative secret'));
    app.use(express.session());

    app.use(passport.initialize());
    app.use(passport.session());

    app.use(app.router);
    app.use(express.static(path.join(__dirname, 'public')));
  });
}

/* Start up passport.
 */
function initPassportUser(){
  var User = require('./User');

  passport.use(new LocalStrategy(User.authenticate()));

  passport.serializeUser(User.serializeUser());
  passport.deserializeUser(User.deserializeUser());

  return User;
}

/* Creates a root user if it doesn't exist. 
 * Used for debugging.
 */
function checkForAndCreateRootUser(User){
  User.findOne({username : "admin" }, function(err, existingUser) {
    if (err || existingUser) return;
    var user = new User({ username : "admin" });
    user.superuser = true;
    user.registeredTimestamp = new Date();
    user.setPassword("admin", function(err) {
      if (err) return;
      user.save(function(err) { });
    });
  });
}

/* Checks to see if there are a few default words, 
 * and populates the db if it doesn't have them.
 * Used for debugging.
 */
function checkDefaultWords(Word) {
  Word.findOne({word : "fog" }, function(err, existingWord) {
    if (err || existingWord) return;
    var word = new Word({ word : "fog" });
    word.def = 'a cloud on the ground';
    word.pos = 'noun';
    word.assns = ['Level 1'];
    word.save(function(err) {console.log(err);});
  });

  Word.findOne({word : "predict" }, function(err, existingWord) {
    if (err || existingWord) return;
    var word = new Word({ word : "predict" });
    word.def = 'to tell the future';
    word.pos = 'verb';
    word.assns = ['Level 1'];
    word.save(function(err) {console.log(err);});
  });

  Word.findOne({word : "snowflakes" }, function(err, existingWord) {
    if (err || existingWord) return;
    var word = new Word({ word : "snowflakes" });
    word.def = 'frozen water droplets';
    word.pos = 'noun';
    word.assns = ['Level 1'];
    word.save(function(err) {console.log(err);});
  });
   Word.findOne({word : "humidity" }, function(err, existingWord) {
    if (err || existingWord) return;
    var word = new Word({ word : "humidity" });
    word.def = 'moisture in the air';
    word.pos = 'noun';
    word.assns = ['Level 1'];
    word.save(function(err) {console.log(err);});
  });
}
