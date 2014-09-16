package it.cnr.cool.service.frontOffice;

public enum TypeDocument {
	Notice("notice", "notice"), Log("log", "logs"), Faq("faq", "FAQ");

	private final String name;
	private String folder;

	TypeDocument(String name, String folder){
		this.name = name;
		this.folder = folder;
	}
	
	public static TypeDocument fromValue(String name) {
		for (TypeDocument c : TypeDocument.values()) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		throw new IllegalArgumentException("errore con indice = " + name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getFolder() {
		return folder;
	}
}
