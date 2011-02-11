var http = require('http');


var user ={ 
		"firstname" : "string", 
		"lastname" : "string", 
		"mail" : "string",
		"password" : "string"
		};



var loop =1;
var loopInt = 1;
var total = 0;
var sum =0;

function hit ( ){
		var i = 0;
		
		if (loop > 0)
	while (i < loopInt){
	//	console.log('# LOOP - '+ i +' of '+ loopInt);
		user.mail="user-"+(total);
		sum++;
		var ct = JSON.stringify(user);
		var google = http.createClient(8080, '127.0.0.1');
		var request = google.request('POST', '/api/user',
				{'host': '127.0.0.1',
				 'Content-Length': ''+(ct.length),
				 'Content-Type': 'application/json'});
		request.write(ct);
		request.end();
		request.on('response', function (response) {
			console.log('STATUS: ' + response.statusCode);
			console.log('HEADERS: ' + JSON.stringify(response.headers));
			response.setEncoding('utf8');
			response.on('data', function (chunk) {
				console.log('BODY: ' + chunk);
			});
			sum --;
		});
		
		//console.log('OBJECT: ' + JSON.stringify(user));
		i++;
		total++;
	}
	console.log(sum + ' Running requests :p');
	
	loop--;
	if (loop > 0){
		setTimeout(hit, 20);
	}else {
		loopInt = -1;
		console.log('Waiting for requests done ...');
		
		if (sum > 0){
			setTimeout(hit, 20);
		}else {
			console.log('Total users injected : '+ total);	
		}
	}
}

hit();