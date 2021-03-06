/**
 * Entry point for rmpermissions POST data webscript.
 * Applies supplied permissions to a node.
 *
 * @method main
 */
function main()
{
   /**
    * nodeRef input: store_type, store_id and id
    */
   var storeType = url.templateArgs.store_type,
      storeId = url.templateArgs.store_id,
      id = url.templateArgs.id,
      nodeRef = storeType + "://" + storeId + "/" + id,
      node = search.findNode(nodeRef);

   if (node == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
      return null;
   }

   
   if (json.has("permissions") == false)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Permissions value missing from request.");
   }
   
   var permissions = json.getJSONArray("permissions");
   for (var i = 0; i < permissions.length(); i++)
   {
      var perm = permissions.getJSONObject(i);
      
      // collect values for the permission setting
      var authority = perm.getString("authority");
      var role = perm.getString("role");
      var remove = false;
      if (perm.has("remove"))
      {
         remove = perm.getBoolean("remove");
      }
      
      // Apply or remove permission
      if (remove)
      {
         node.removePermission(role, authority);
      }
      else
      {
         node.setPermission(role, authority);
      }
   }

   // Inherited permissions flag
   if (json.has("isInherited"))
   {
      node.setInheritsPermissions(json.getBoolean("isInherited"));
   }
}

main();