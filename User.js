var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var passportLocalMongoose = require('passport-local-mongoose');
var mongoUri = process.env.MONGOLAB_URI || process.env.MONGOHQ_URL || 'mongodb://localhost/myApp';
var mongoOptions = { db: { safe: true }};

//user schema has information for both teachers and tutors and admin
//all these require authentication
var User = new Schema({
    registeredTimestamp: {type: Date},
    lastLoginTimestamp: {type: Date},
    lastIp: String,
    lastHost: String,
    lastUserAgent: String,
    lastMsgTimestamp: {type: Date},
    userType: String,
    superuser: Boolean
});

User.plugin(passportLocalMongoose); //adds username, password to schema

module.exports = mongoose.model('User', User);



