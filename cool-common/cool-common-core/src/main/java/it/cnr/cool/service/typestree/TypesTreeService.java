package it.cnr.cool.service.typestree;

import it.cnr.cool.cmis.service.CMISService;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TypesTreeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypesTreeService.class);

	private static final String FREE_SEARCH_SET_NAME = "default";
	private static final int MAX_DIGITS = 40;

	@Autowired
	private CMISService cmisService;

	public List<Type> getTree(List<String> seeds) {

    	if (LOGGER.isDebugEnabled())
    		LOGGER.debug("Execute getTree " + this.getClass().getName());

		List<Type> tree = new ArrayList<Type>();
		Session adminSession = cmisService.createAdminSession();

		ObjectType rootType = null;
		List<Tree<ObjectType>> typeDescendants = null;
		Type root = null;
		for (String seed: seeds) {
			rootType = adminSession.getTypeDefinition(seed);
			typeDescendants = adminSession.getTypeDescendants(seed, Short.MAX_VALUE, false);
			root = new Type();
			root.setAttr( getAttribute(rootType) );
	      	root.setData( formatDiplayName(rootType.getDisplayName()) );
	      	root.setChildren( formatDescendants(typeDescendants) );
	      	tree.add(root);
		}

		return tree;
	}

	private List<Type> formatDescendants(List<Tree<ObjectType>> typeDescendants) {
		List<Type> types = new ArrayList<Type>();
		Type type = null;

		for (Tree<ObjectType> typeDescendant : typeDescendants) {
			type = new Type();
			type.setAttr( getAttribute(typeDescendant.getItem()) );
			type.setData( formatDiplayName(typeDescendant.getItem().getDisplayName()) );
			if (!typeDescendant.getChildren().isEmpty()) {
				type.setChildren( formatDescendants(typeDescendant.getChildren()) );
			}
			types.add(type);
		}

		return types;
	}

	private Attribute getAttribute(ObjectType objType) {

		Attribute attr = new Attribute();
		attr.setId(objType.getId());
		attr.setDescription(objType.getDescription());
		attr.setQueryName(objType.getQueryName());
		attr.setFreeSearchSetName(FREE_SEARCH_SET_NAME); //XXX: objType.getId() o sempre 'search' ???
		attr.setDisplayName(objType.getDisplayName());

		return attr;
	}

	private String formatDiplayName(String name) {
		return name.length() < MAX_DIGITS ? name : name.substring(0, MAX_DIGITS)+"...";
	}

}