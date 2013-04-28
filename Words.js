var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var passportLocalMongoose = require('passport-local-mongoose');
var mongoUri = process.env.MONGOLAB_URI || process.env.MONGOHQ_URL || 'mongodb://localhost/myApp';
var mongoOptions = { db: { safe: true }};


var Word = new Schema({
    word: String,
    def: String,
    pos: String,
    assns: Array
});

module.exports = mongoose.model('Word', Word);

