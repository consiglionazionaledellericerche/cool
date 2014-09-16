/*global cnrutils, person, model, args, argsM, search*/

var filters = argsM.filter,
	i = 0;

if (filters.length === 0) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify at least one filter");
}

var luceneQuery = "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" ";

for (i; i < filters.length; i++) {
	var field = filters[i].split(":");
	luceneQuery = luceneQuery + " AND ((@cm\\:" + field[0] + ":\"" + field[1] + "\") OR (@cnrperson\\:" + field[0] + ":\"" + field[1] + "\"))";
}

// Get the collection of people
var peopleCollection = search.luceneSearch(luceneQuery);
// Pass the queried sites to the template
model.peoplelist = peopleCollection;