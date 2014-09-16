var objectId = args.nodeRef,
  version = cmisSession.getObject(objectId).getAllVersions();
model.models = version;
model.totalNumItems = version.size();
model.maxItemsPerPage = 1000;
model.activePage = 0;
model.hasMoreItems = false;