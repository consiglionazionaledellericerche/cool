/*global model, args, search */
// Get the args
var filter = args.filter;
var maxResults = args.maxResults;
var luceneQuery = "TYPE:\"{http://www.alfresco.org/model/content/1.0}authorityContainer\" ";
if (filter) {
	var separatorIndex = filter.indexOf(':');
	if (separatorIndex !== -1) {
	  var field = filter.substring(0, separatorIndex);
	  var filter = filter.substring(separatorIndex + 1);
	  luceneQuery = luceneQuery + "AND (@cm\\:" + field + ":\"" + filter + "\")";
	} else {
	  luceneQuery = luceneQuery + "AND (@cm\\:authorityName:\"" + filter + "\")";
	}
}
// Get the collection of people
var groupCollection = search.luceneSearch(luceneQuery);

var everyone = 'GROUP_EVERYONE';
if (everyone.toLowerCase().indexOf(filter.toLowerCase().replace(/[^a-z0-9_]/g, '')) >= 0) {
	groupCollection.push({
		properties: {
			authorityName: everyone,
			authorityDisplayName: everyone
		}
	});
}

// Pass the queried sites to the template
model.grouplist = groupCollection;
