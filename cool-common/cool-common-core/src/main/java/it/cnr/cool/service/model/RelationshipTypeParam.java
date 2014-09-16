package it.cnr.cool.service.model;

public enum RelationshipTypeParam {
	none,first,cascade,child, parent;
	
	public Boolean isSearchRelationship(){
		if (this.equals(first) || this.equals(cascade))
			return true;
		return false;
	}

	public Boolean isChildRelationship(){
		if (this.equals(child))
			return true;
		return false;
	}

	public Boolean isParentRelationship(){
		if (this.equals(parent))
			return true;
		return false;
	}

}
