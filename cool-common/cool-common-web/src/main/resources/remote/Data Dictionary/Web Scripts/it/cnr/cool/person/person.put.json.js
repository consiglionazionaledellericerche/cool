/*global cnrutils, person, model, args, argsM, search, url, people, status, utils, json, logger*/
// Get the person details and ensure they exist for update
function main() {
  "use strict";
  var userName = url.extension, person = people.getPerson(userName),
    disableAccount, jsonKeys = json.keys(), nextKey,
    bannerField = ['userName', 'immutability', 'enabled', 'disableAccount', 'groups', 'capabilities', 'map', 'password'];
  if (person === null) {
    status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
    return;
  }
  while (jsonKeys.hasNext()) {
    nextKey = jsonKeys.next();
    if (bannerField.indexOf(String(nextKey)) < 0) {
      //logger.error(nextKey + " - " + json.get(nextKey));
      if (String(json.get(nextKey)) === "") {
        person.properties[nextKey] = null;
      } else {
        person.properties[nextKey] = json.get(nextKey);
      }
    }
  }
  // Update the person node with the modified details
  person.save();

  // Enable or disable account? - note that only Admin can set this
  if (json.has("disableAccount")) {
    disableAccount = (json.get("disableAccount") === true);
    if (disableAccount && people.isAccountEnabled(userName)) {
      people.disableAccount(userName);
    } else if (!disableAccount && !people.isAccountEnabled(userName)) {
      people.enableAccount(userName);
    }
  }
  model.person = person;
}
main();