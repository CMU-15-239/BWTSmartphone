var path = require('path');
var express = require('express');
var http = require('http');
var mongoose = require('mongoose');
var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var words = require('./routes/words');

function init(){
    var app = express();
    configureExpress(app);

    var User = initPassportUser();

    mongoose.connect('mongodb://localhost/myApp');
    checkForAndCreateRootUser(User);

    require('./loginRoutes')(app);
    require('./appRoutes')(app);

    http.createServer(app).listen(3000, function() {
        console.log("Express server listening on port %d", 3000);
    });
}

init();

function configureExpress(app){
    app.configure(function(){
        app.use(express.bodyParser());
        app.use(express.methodOverride());

        app.use(express.cookieParser('your secret here'));
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
    User.findOne({username : "root" }, function(err, existingUser) {
        if (err || existingUser) return;
        var user = new User({ username : "root" });
        user.superuser = true;
        user.registeredTimestamp = new Date();
        user.setPassword("SECRET", function(err) {
            if (err) return;
            user.save(function(err) { });
        });  
    });
}
// var express = require('express'),
//     words   = require('./routes/words');

// var app = express();

// app.configure(function(){
//   app.use(express.logger('dev'));
//   app.use(express.bodyParser());
// });

// app.use(express.static(__dirname + '/public'));


// // words CRUD
// app.get('/words', words.findAll);
// app.get('/words/:id', words.findById);
// app.post('/words', words.addWord);
// app.put('/words/:id', words.updateWord);
// app.delete('/words/:id', words.deleteWord);

// app.get('/',express.static(__dirname + '/frontend'));


// app.listen(3000);
// console.log('Listening on port 3000...');