/*global jsonUtils, requestbody, people, model*/

var json = jsonUtils.toObject(requestbody.content),
  userName = json.userName,
  disable = json.disableUser,
  status = "no change";

if (disable === true) {
  people.disableAccount(userName);
  status = "disabled";
} else if (disable === false) {
  people.enableAccount(userName);
  status = "enabled";
}

model.status = status;