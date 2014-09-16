/*global cnrutils, person, model, args, argsM, search, url, people, status, utils, json, logger*/
function main() {
  "use strict";
  if ((json.isNull("userName")) || (json.get("userName").length() === 0)) {
    status.setCode(status.STATUS_BAD_REQUEST, "User name missing when creating person");
    return;
  }
  if ((json.isNull("firstName")) || (json.get("firstName").length() === 0)) {
    status.setCode(status.STATUS_BAD_REQUEST, "First name missing when creating person");
    return;
  }
  if ((json.isNull("email")) || (json.get("email").length() === 0)) {
    status.setCode(status.STATUS_BAD_REQUEST, "Email missing when creating person");
    return;
  }
  var password = "password", userName, enableAccount, person, jsonKeys = json.keys(), nextKey, quota,
    bannerField = ['userName', 'immutability', 'enabled', 'disableAccount', 'groups', 'capabilities', 'map', 'password'];
  if (json.has("password")) {
    password = json.get("password");
  }
  // Create the person with the supplied user name
  userName = json.get("userName");
  enableAccount = ((json.has("disableAccount") && json.get("disableAccount")) === false);
  person = people.createPerson(userName, json.get("firstName"), json.get("lastName"), json.get("email"), password, enableAccount);
  // return error message if a person with that user name could not be created
  if (person === null) {
    status.setCode(status.STATUS_CONFLICT, "User name already exists: " + userName);
    return;
  }
  person.addAspect("cnrperson:metadati");
  while (jsonKeys.hasNext()) {
    nextKey = jsonKeys.next();
    if (bannerField.indexOf(String(nextKey)) < 0) {
      if (String(json.get(nextKey)) === "") {
        person.properties[nextKey] = null;
      } else {
        person.properties[nextKey] = json.get(nextKey);
      }
    }
  }
  person.save();
  // set quota if any - note that only Admin can set this and will be ignored otherwise
  quota = (json.has("quota") ? json.get("quota") : -1);
  people.setQuota(person, quota.toString());
  // Put the created person into the model
  model.person = person;
}
main();