/*global cnrutils, person, model, args, argsM, search, url, people, status, utils, json, logger*/
// Get the person details and ensure they exist for update
function main() {
  "use strict";
  var userName = url.extension, person = people.getPerson(userName);
  if (person === null) {
    status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
    return;
  }
  people.deletePerson(userName);
}
main();