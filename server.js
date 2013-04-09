var path = require('path');
var express = require('express');
var http = require('http');
var mongoose = require('mongoose');
var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;

var mongo = require('mongodb');

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

function initPassportUser(){
    var User = require('./User');

    passport.use(new LocalStrategy(User.authenticate()));

    passport.serializeUser(User.serializeUser());
    passport.deserializeUser(User.deserializeUser());

    return User;
}

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
function checkDefaultWords(Word) {
    Word.findOne({word : "hello" }, function(err, existingWord) {
        if (err || existingWord) return;
        var word = new Word({ word : "hello" });
        word.def = 'An exclamatory greeting';
        word.pos = 'exlamation';
        word.assns = ['Level 1', 'Level 2', 'Level 3'];
        word.save(function(err) {console.log(err);});
    });
}
