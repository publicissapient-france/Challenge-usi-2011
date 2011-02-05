var http = require('request');


var user ={ 
		"firstname" : "string", 
		"lastname" : "string", 
		"mail" : "string",
		"password" : "string"
		};



var loop =1;
var loopInt = 1000;
var total = 0;
var sum =0;

function hit ( ){
		var i = 0;
	while (i <= loopInt){
		user.mail="user-"+(total);
		
	//console.log('OBJECT: ' + JSON.stringify(user));
	var google = http({uri:'http://127.0.0.1:8080/api/user',
							   method: 'POST',
							   body:JSON.stringify(user)}, 
							   function (err, response, body) {
								   
								   if (err){
									console.log("error : "+ JSON.stringify(err));   
								   } else {
								   console.log('STATUS: ' + response.statusCode);
							//	   console.log('HEADERS: ' + JSON.stringify(response.headers));
								   console.log('BODY: ' + body);
								   }
								   sum --;
							   });
	i++;
	total++;
	sum++;

	}
	
	loop--;
	if (loop > 0){
		setTimeout(hit, 4);
	}else {
		loopInt = -1;
		
		
		if (sum > 0){
			setTimeout(hit, 4);
		}else {
			console.log('Total users injected : '+ total);	
		}
	}
}

hit();