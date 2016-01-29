var express = require('express');
var bodyParser = require('body-parser');
var morgan = require('morgan');//pretty logout for node.js
var crypto = require('crypto');
var http = require('http')//include for create http server
var app = express();// use express framwork
var mongoose = require('mongoose');
var Account = require('./app/models/Account')(mongoose);// import Account model
app.use(morgan('dev')); // log requests to the console 
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());// to parse json data
mongoose.connect('mongodb://localhost:27017/Server'); // connect to our database
//config route events
var port = process.env.PORT || 8080; // set our port
var router = express.Router();
router.route('/')

	.get(function(req, res){
		res.send('hello world node.js~');
	});

router.route('/register')
	.get(function(req,res){
		res.render('reg', {
        title: 'register'
    });
	})
	.post(function(req, res) {
		console.log(req);	
		console.log(req.body.name);
		
		Account.register(req.body.email, req.body.password, req.body.phone, req.body.name, res);	
	});
app.use('/api', router);// append /api to every route we set
http.createServer(app).listen(port);//ceate a http server and listen to port:8080
console.log('http listen ' + port);