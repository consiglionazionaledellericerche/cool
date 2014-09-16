var fs = require('fs');
var request = require('request'); // npm install -g request

var prefixLeft = "http://localhost:8180/cool-jconon/rest/bulkInfo/view"; // BulkInfoCoolSerializer
var prefixRight = "http://localhost:8280/cool-jconon/rest/bulkInfo/view"; // .ftl
var postfix = "v=1.0&ajax=true&guest=true";

fs.mkdir("./accepted");
fs.mkdir("./accepted/left");
fs.mkdir("./accepted/right");
fs.mkdir("./rejected");
fs.mkdir("./rejected/left");
fs.mkdir("./rejected/right");

var timeout = 1;

fs.readFile('../sh/bulkinfo.txt', 'utf8', function(err, data) {
	if(err) {
		return console.log(err);
	}
	console.log(typeof(data), data);

	var richieste = data.split("\n"); // richieste[]

	richieste.forEach(function(richiesta, i) {
		setTimeout(function() {faiLaRichiesta(richiesta, i);}, 500*timeout++);
	})
});

fs.readFile('../sh/bulkInfoWithCmisObject.txt', 'utf8', function(err, data) {
  if(err) {
    return console.log(err);
  }
  console.log(typeof(data), data);

  var richieste = data.split("\n"); // richieste[]

  richieste.forEach(function(richiesta, i) {
    setTimeout(function() {faiLaRichiestaConCmis(richiesta, i);}, 500*timeout++);
  })
});

function faiLaRichiesta(richiesta, i) {
	console.log("Eseguendo la richiesta", richiesta);
	console.log("url = "+ prefixLeft + "/" + richiesta + "?" + postfix);

	request(prefixLeft + "/" + richiesta + "?" + postfix, function(error, response, bodyLeft) {
		if(error) {
			console.log(richiesta, error);
		}
		if(!error && response.statusCode == 200) {
			request(prefixRight + "/" + richiesta + "?" + postfix, function(error, response, bodyRight) {
				if(error) {
					console.log(richiesta, error);
				}
				if(!error && response.statusCode == 200) {
					var jsonLeft = JSON.parse(bodyLeft);
					var jsonRight = JSON.parse(bodyRight);
					if(deepCompare(jsonLeft, jsonRight) ) {
						console.log("Request "+i+" accepted!");
						fs.writeFile("accepted/left/"+i+".json", JSON.stringify(JSON.parse(stringify(jsonLeft)), undefined, 2) );
						fs.writeFile("accepted/left/"+i+".raw", bodyLeft);
						fs.writeFile("accepted/right/"+i+".json", JSON.stringify(JSON.parse(stringify(jsonRight)), undefined, 2) );
						fs.writeFile("accepted/right/"+i+".raw", bodyRight);					
					} else {
						console.log("Request "+i+" rejected!");
						fs.writeFile("rejected/left/"+i+".json", JSON.stringify(JSON.parse(stringify(jsonLeft)), undefined, 2) );
						fs.writeFile("rejected/left/"+i+".raw", bodyLeft);
						fs.writeFile("rejected/right/"+i+".json", JSON.stringify(JSON.parse(stringify(jsonRight)), undefined, 2) );
						fs.writeFile("rejected/right/"+i+".raw", bodyRight);		
					}
				}
			});
		}
	});
}

function faiLaRichiestaConCmis(richiesta, i) {
  console.log("Eseguendo la richiesta", richiesta);
  console.log("url = "+ prefixLeft + "/" + richiesta + "?" + postfix);

  request(prefixLeft + "/" + richiesta, function(error, response, bodyLeft) {
    if(error) {
      console.log(richiesta, error);
    }
    if(!error && response.statusCode == 200) {
      request(prefixRight + "/" + richiesta + "?" + postfix, function(error, response, bodyRight) {
        if(error) {
          console.log(richiesta, error);
        }
        if(!error && response.statusCode == 200) {
          var jsonLeft = JSON.parse(bodyLeft);
          var jsonRight = JSON.parse(bodyRight);
          if(deepCompare(jsonLeft, jsonRight) ) {
            console.log("Request "+i+" accepted!");
            fs.writeFile("accepted/left/CMIS"+i+"left.json", JSON.stringify(JSON.parse(stringify(jsonLeft)), undefined, 2) );
            fs.writeFile("accepted/left/CMIS"+i+"left.raw", bodyLeft);
            fs.writeFile("accepted/right/CMIS"+i+"right.json", JSON.stringify(JSON.parse(stringify(jsonRight)), undefined, 2) );
            fs.writeFile("accepted/right/CMIS"+i+"right.raw", bodyRight);         
          } else {
            console.log("Request "+i+" rejected!");
            fs.writeFile("rejected/left/CMIS"+i+"left.json", JSON.stringify(JSON.parse(stringify(jsonLeft)), undefined, 2) );
            fs.writeFile("rejected/left/CMIS"+i+"left.raw", bodyLeft);
            fs.writeFile("rejected/right/CMIS"+i+"right.json", JSON.stringify(JSON.parse(stringify(jsonRight)), undefined, 2) );
            fs.writeFile("rejected/right/CMIS"+i+"right.raw", bodyRight);   
          }
        }
      });
    }
  });
}

function deepCompare () {
	var leftChain, rightChain;

	function compare2Objects (x, y) {
		var p;

    // remember that NaN === NaN returns false
    // and isNaN(undefined) returns true
    if (isNaN(x) && isNaN(y) && typeof x === 'number' && typeof y === 'number') {
    	return true;
    }

    // Compare primitives and functions.     
    // Check if both arguments link to the same object.
    // Especially useful on step when comparing prototypes
    if (x === y) {
    	return true;
    }

    // Works in case when functions are created in constructor.
    // Comparing dates is a common scenario. Another built-ins?
    // We can even handle functions passed across iframes
    if ((typeof x === 'function' && typeof y === 'function') ||
    	(x instanceof Date && y instanceof Date) ||
    	(x instanceof RegExp && y instanceof RegExp) ||
    	(x instanceof String && y instanceof String) ||
    	(x instanceof Number && y instanceof Number)) {
    	return x.toString() === y.toString();
  }
    
    if ((typeof x === 'boolean' && typeof y === 'string') ||
        (typeof x === 'string' && typeof y === 'boolean') ||
        (typeof x === 'number' && typeof y === 'string') ||
        (typeof x === 'string' && typeof y === 'number')) {
        return x.toString() === y.toString();
    }

    // At last checking prototypes as good a we can
    if (!(x instanceof Object && y instanceof Object)) {
    	return false;
    }

    if (x.isPrototypeOf(y) || y.isPrototypeOf(x)) {
    	return false;
    }

    if (x.constructor !== y.constructor) {
    	return false;
    }

    if (x.prototype !== y.prototype) {
    	return false;
    }

    // check for infinitive linking loops
    if (leftChain.indexOf(x) > -1 || rightChain.indexOf(y) > -1) {
    	return false;
    }

    // Quick checking of one object beeing a subset of another.
    // todo: cache the structure of arguments[0] for performance
    for (p in y) {
    	if (y.hasOwnProperty(p) !== x.hasOwnProperty(p)) {
    		return false;
    	}
    	else if (typeof y[p] !== typeof x[p]) {
        if ((typeof x[p] === 'boolean' && typeof y[p] === 'string') ||
            (typeof x[p] === 'string' && typeof y[p] === 'boolean') ||
            (typeof x[p] === 'number' && typeof y[p] === 'string') ||
            (typeof x[p] === 'string' && typeof y[p] === 'number')) {
            return x[p].toString() === y[p].toString();
        } else {
          console.log("192 Rejecting because types are not the same.", typeof x[p], typeof y[p]);
      		return false;
        }
    	}
    }

    for (p in x) {
    	if (y.hasOwnProperty(p) !== x.hasOwnProperty(p)) {
    		return false;
    	}
    	else if (typeof y[p] !== typeof x[p]) {
    		return false;
    	}

    	switch (typeof (x[p])) {
    		case 'object':
    		case 'function':

    		leftChain.push(x);
    		rightChain.push(y);

    		if (!compare2Objects (x[p], y[p])) {
    			return false;
    		}

    		leftChain.pop();
    		rightChain.pop();
    		break;

    		default:
    		if (x[p] !== y[p]) {
    			return false;
    		}
    		break;
    	}
    }

    return true;
  }

  if (arguments.length < 1) {
    return true; //Die silently? Don't know how to handle such case, please help...
    // throw "Need two or more arguments to compare";
  }

  for (var i = 1, l = arguments.length; i < l; i++) {

      leftChain = []; //todo: this can be cached
      rightChain = [];

      if (!compare2Objects(arguments[0], arguments[i])) {
      	return false;
      }
    }

    return true;
  }

function stringify(obj) {
  var type = Object.prototype.toString.call(obj);

  // IE8 <= 8 does not have array map
  var map = Array.prototype.map || function map(callback) {
    var ret = [];
    for (var i = 0; i < this.length; i++) {
      ret.push(callback(this[i]));
    }
    return ret;
  };

  if (type === '[object Object]') {
    var pairs = [];
    for (var k in obj) {
      if (!obj.hasOwnProperty(k)) continue;
      pairs.push([k, stringify(obj[k])]);
    }
    pairs.sort(function(a, b) { return a[0] < b[0] ? -1 : 1 });
    pairs = map.call(pairs, function(v) { return '"' + v[0] + '":' + v[1] });
    return '{' + pairs + '}';
  }

  if (type === '[object Array]') {
    return '[' + map.call(obj, function(v) { return stringify(v) }) + ']';
  }

  return JSON.stringify(obj);
};

  //SORT WITH STRINGIFICATION

var orderedStringify = function(o, fn) {
    var props = [];
    var res = '{';
    for(var i in o) {
        props.push(i);
    }
    props = props.sort(fn);

    for(var i = 0; i < props.length; i++) {
        var val = o[props[i]];
        var type = types[whatis(val)];
        if(type === 3) {
            val = orderedStringify(val, fn);
        } else if(type === 2) {
            val = arrayStringify(val, fn);
        } else if(type === 1) {
            val = '"'+val+'"';
        }

        if(type !== 4)
            res += '"'+props[i]+'":'+ val+',';
    }

    return res.substring(res, res.lastIndexOf(','))+'}';
};

//orderedStringify for array containing objects
var arrayStringify = function(a, fn) {
    var res = '[';
    for(var i = 0; i < a.length; i++) {
        var val = a[i];
        var type = types[whatis(val)];
        if(type === 3) {
            val = orderedStringify(val, fn);
        } else if(type === 2) {
            val = arrayStringify(val);
        } else if(type === 1) {
            val = '"'+val+'"';
        }

        if(type !== 4)
            res += ''+ val+',';
    }

    return res.substring(res, res.lastIndexOf(','))+']';
}