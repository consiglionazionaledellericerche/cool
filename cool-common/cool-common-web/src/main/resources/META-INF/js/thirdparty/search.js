/*global $, stemmer*/
/*jslint plusplus:true, vars: true, browser:true*/
/* Javascript Search Engine */
var Search = function (options, dataset, callback) {

  "use strict";

  var dictionary = {};
  var keys = [];
  var chars = /[^a-zA-Z0-9]/g;
  var settings = {
    allowed: function (key) {
      return true;
    }
  };

  var log = function (msg) {
    if (settings.logger && window.console) {
      window.console.log(msg);
    }
  };

  // intersection of sorted arrays
  var intersect_safe = function (a, b) {
    var ai = 0, bi = 0;
    var result = [];

    while (ai < a.length && bi < b.length) {
      if (a[ai] < b[bi]) {
        ai++;
      } else if (a[ai] > b[bi]) {
        bi++;
      } else {
        /* they're equal */
        result.push(a[ai]);
        ai++;
        bi++;
      }
    }
    return result;
  };

  // union of sorted arrays
  var union = function (a, b) {
    var i = 0, j = 0;
    var c = [];
    var el;

    while (i < a.length || j < b.length) {

      if (i === a.length || a[i] >= b[j]) {
        el = b[j];
        ++j;
      } else {
        el = a[i];
        ++i;
      }

      if (c[c.length - 1] !== el) {
        c.push(el);
      }
    }
    return c;
  };

  //binary search - look for prefix "find" in array of strings
  var binarySearch = function (array, find) {
    var i;
    var low = 0;
    var high = array.length - 1;
    var re = new RegExp("^" + find, "g");

    var mid, mids, tmp, str;

    while (low <= high) {
      mid = parseInt((low + high) / 2, 10);
      mids = array[mid];
      if (mids.match(re)) {
        if (low === high) {
          tmp = [];
          for (i = mid; i < array.length; i++) {
            str = array[i];
            if (str.match(re)) {
              tmp.push(str);
            } else {
              break;
            }
          }
          return tmp;
        } else {
          high = mid;
        }
      } else {
        if (mids > find) {
          high = mid - 1;
        } else {
          low = mid + 1;
        }
      }
    }
    return -1;
  };

  // check if string "s" is prefix of one of the terms in array "terms"
  var isPrefix = function (s, terms) {
    var re = new RegExp("^" + s, "g");
    var ret = false;
    $.each(terms, function (id, el) {
      if (el.match(re)) {
        ret = true;
        return false;
      }
    });
    return ret;
  };

  // remove from array keywords to be ignored - string less than minLength characters OR stop word
  var removeEmpty = function (arr) {
    var cl = [];
    $.each(arr, function (id, t) {
      if (t.length >= settings.minLength && !isPrefix(t, settings.stopWords)) {
        cl.push(t);
      }
    });

    return cl;
  };

  // search for "query" in dictionary. Then invokes "callback".
  var search = function (query, callback, qta) {
    var t = new Date().getTime();
    query = query.toLowerCase();
    var keywords = removeEmpty(query.split(chars));
    var temp = [];

    if (keywords.length > 0) {
      $.each(keywords, function (id, keyword) {
        var result = binarySearch(keys, settings.stem ? stemmer(keyword) : keyword, 1, 1);
        var x = [];

        $.each(result, function (id) {
          x = union(x, dictionary[result[id]]);
        });

        if (id === 0) {
          temp = x;
        } else {
          temp = intersect_safe(temp, x);
        }
      });
    }

    log("search for query '" + query + "' completed in " + (new Date().getTime() - t) + " msec.");
    callback = callback || function (a) {
      return ("result: " + JSON.stringify(a));
    };
    callback(temp, qta);
  };

  var flatten = function (o) {
    var i, s;
    if (typeof o === "string" || typeof o === "number") {
      s = o;
    } else if (typeof o === "array") {
      s = "";
      for (i = 0; i < o.length; i++) {
        s += flatten(o[i]) + " ";
      }
    } else if (typeof o === "object" && o) {
      s = $.map(o, function (value, key) {
        if (settings.allowed(key)) {
          return flatten(value);
        }
      }).join(' ') + ' ';

    }
    return s;
  };

  // create inverted index (dictionary) and lexicon (keys) using dataset
  var createIndex = function (options, dataset, callback) {
    settings = options;
    var t = new Date().getTime();
    var termIdx;

    dictionary = {};
    keys = [];

    var count = 0;
    var terms, term;

    $.each(dataset, function (key, value) {
      log((++count / Object.keys(dataset).length * 100).toFixed() + "%");
      terms = flatten(value).split(chars);

      $.each(terms, function (termIdx, term) {

        term = term.toLowerCase();
        term = settings.stem ? stemmer(term) : term;
        if (term !== "" && term.length >= settings.minLength) {
          if (dictionary.hasOwnProperty(term)) {

            //check that dictionary[term] does not already contain key
            if (dictionary[term].indexOf(key) < 0) {
              dictionary[term].push(key);
            }
          } else {
            dictionary[term] = [key];
          }
        }
      });
    });

    $.each(settings.stopWords, function (idx, el) {
      el = settings.stem ? stemmer(el) : el;
      delete dictionary[el];
    });
    // the $.each will fail if the text contains the word "length"
    var id, el;
    var sortFn;
    if (Object.keys(dictionary).length > 0 && typeof dictionary[Object.keys(dictionary)[0]][0] === 'number') {
      sortFn = function (a, b) {
        return a - b;
      };
    }
    for (id in dictionary) {
      if (dictionary.hasOwnProperty(id)) {
        el = dictionary[id];
        dictionary[id] = el.sort(sortFn);
        keys.push(id);
      }
    }

    keys.sort();
    log("index of " + Object.keys(dataset).length + " documents created in " + (new Date().getTime() - t) / 1000 + " sec.");
    if (typeof callback === 'function') {
      callback();
    }
  };

  // init indexes
  createIndex(options, dataset, callback);

  return {
    "search" : search
  };

};