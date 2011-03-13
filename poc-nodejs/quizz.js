var connect = require('connect');
var myJson = require('./json-minified.js');



var poolModule = require('generic-pool');

var pool = poolModule.Pool({
    name     : 'mongodb',
    create   : function(callback) {
        var mongoose = require('mongoose').Mongoose;
		mongoose.model('User', {properties:['firstname', 'lastname', 'password', 'mail'], indexes: ['mail']});
		var db = mongoose.connect('mongodb://localhost/users');
		var User = db.model('User');
		callback(User);
    },
    destroy  : function(client) { client.halt(); },
    max      : 50,
    idleTimeoutMillis : 30000,
    log : true
});






//var users = {};

function checkProperty(obj, prop){
	if(obj.hasOwnProperty(prop)){
		return true;
	}
	return false;
}

function validateUser(usr){
	var res = true;
	res = res && checkProperty(usr, 'firstname');
	res = res && checkProperty(usr, 'lastname');
	res = res && checkProperty(usr, 'password');
	res = res && checkProperty(usr, 'mail');
	
	return res;
}

function main(app){
	app.get('/', function(req, res){
        var examples = [
            '/api/',
            '/api/user (to create a user)',
            '/api/game (to create a game)',
        ];
        var body = 'Visit one of the following: <ul>'
            + examples.map(function(str){ return '<li>' + str + '</li>'; }).join('\n')
            + '</ul>';
        res.writeHead(200, {
            'Content-Type': 'text/html',
            'Content-Length': body.length
        });
        res.end(body, 'utf8');
    });
	
	app.post('/api/login',function (req, res){
		req.content= '';
		req.addListener("data", function(chunk) {
			req.content += chunk;
		});
	 
		req.addListener("end", function() {
			//parse req.content and do stuff with it
			console.log('content received : '+req.content);
			req.usr = JSON.parse (req.content);
		//	console.log('User is '+ JSON.stringify(req.usr));
			if (!checkProperty(req.usr, 'mail')|| !checkProperty(req.usr, 'password')){
				var body = 'Bad login format : missing mail or password property ! ';
				res.writeHead(400, {
		            'Content-Type': 'text/html',
		            'Content-Length': body.length
		        });
				res.end(body, 'utf8');
				return;
			}
			if (req.session.mail){
				body = 'User already logged in !';
				res.writeHead(400, {
		            'Content-Type': 'text/html',
		            'Content-Length': body.length
		        });
				res.end(body, 'utf8');
			}
			
			
			pool.acquire(function (User){
			
				User.find({'mail':req.usr.mail}).first(function(result){
					if (result.password == req.usr.password){
						req.session.regenerate(function(err){
							var body = 'OK : User logged in :)';
							var mail = req.session.mail =req.usr.mail;
							res.writeHead(200, {
								'Content-Type': 'text/html',
								'Content-Length': body.length
							});
							res.end(body, 'utf8');
						});
					}
					else {
						var body = 'KO : Authentication failed';
						res.writeHead(400, {
							'Content-Type': 'text/html',
							'Content-Length': body.length
						});
						res.end(body, 'utf8');
					}
				
				});
				pool.release(User);
	        });
		});
	});
		
	
	app.post('/api/user', function (req, res){
		req.content= '';
		req.addListener("data", function(chunk) {
			req.content += chunk;
		});
	 
		req.addListener("end", function() {
			//parse req.content and do stuff with it
		//	console.log('content received : '+req.content);
			req.usr = JSON.parse (req.content);
		//	console.log('User is '+ JSON.stringify(req.usr));
			if (!validateUser(req.usr)){
				var body = 'Bad user format !';
				res.writeHead(400, {
		            'Content-Type': 'text/html',
		            'Content-Length': body.length
		        });
				 res.end(body, 'utf8');
				return;
			}
			 
			pool.acquire(function(client) {

				client.find({mail:req.usr.mail}).first(function(result){
					if (result != null){
						var body = 'User\'s mail already exist !';
						res.writeHead(400, {
							'Content-Type': 'text/html',
							'Content-Length': body.length
						});
						res.end(body, 'utf8');
					}else {
						var u = new client(req.usr);
						u.save();
						var body = 'OK : CREATED 201';
						res.writeHead(201, {
							'Content-Type': 'text/html',
							'Content-Length': body.length
						});
						res.end(body, 'utf8');
					}
				});
				pool.release(client);
			});
	       
		});
		
	});
}

var server = connect.createServer(
//		connect.bodyDecoder(),
		connect.cookieDecoder(),
//		connect.session({key: 'session_key', secret: '#NodeQuizz%'}),
		connect.logger({ format: ':method :url :response-time' }),
		connect.errorHandler({ showStack: true, dumpExceptions: true }));

//server.use("/api/user", connect.router(user));
server.use(connect.router(main));
server.listen(3000);
console.log('Connect server listening on port 3000');

