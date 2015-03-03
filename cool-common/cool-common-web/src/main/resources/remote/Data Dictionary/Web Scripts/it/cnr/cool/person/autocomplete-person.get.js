/*global args, search, model */
// Get the args
var filter = args.filter;
var maxResults = args.maxResults;
var luceneQuery = "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" ";
if (filter !== null) {
	var separatorIndex = filter.indexOf(':');
	if (separatorIndex !== -1) {
	  var field = filter.substring(0, separatorIndex);
	  var filter = filter.substring(separatorIndex + 1);
	  luceneQuery = luceneQuery + "AND (@cm\\:" + field + ":\"" + filter + "\")";
	} else {
	  luceneQuery = luceneQuery + "AND (@cm\\:userName:\"" + filter + "\")";
	}
}
// Get the collection of people
var peopleCollection = search.luceneSearch(luceneQuery, "@cm\\:userName", true, maxResults);
// Pass the queried sites to the template
model.peoplelist = peopleCollection;