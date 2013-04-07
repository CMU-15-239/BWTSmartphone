var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var passportLocalMongoose = require('passport-local-mongoose');
var mongoUri = process.env.MONGOLAB_URI || process.env.MONGOHQ_URL || 'mongodb://localhost/myApp';
var mongoOptions = { db: { safe: true }};
mongoose.connect(mongoUri, mongoOptions, function (err, res) {
  if (err) { 
    console.log ('ERROR connecting to: ' + mongoUri + '. ' + err);
  } else {
    console.log ('Succeeded connected to: ' + mongoUri);
  }
});

var Word = new Schema({
    word: String,
    def: String,
    pos: String,
    assns: Array
});

module.exports = mongoose.model('Word', Word);





// var mongoose = require('mongoose');
// var Schema = mongoose.Schema;
// var passportLocalMongoose = require('passport-local-mongoose');

// var User = new Schema({
//     registeredTimestamp: Date,
//     lastLoginTimestamp: Date,
//     lastIp: String,
//     lastHost: String,
//     lastUserAgent: String,
//     lastMsgTimestamp: Date,
//     superuser: Boolean,
// });

// User.plugin(passportLocalMongoose); //adds username, password to schema

// module.exports = mongoose.model('User', User);
