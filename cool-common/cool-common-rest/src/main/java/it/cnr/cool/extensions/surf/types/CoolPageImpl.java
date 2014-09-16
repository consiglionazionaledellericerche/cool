package it.cnr.cool.extensions.surf.types;

import org.dom4j.Document;
import org.springframework.extensions.surf.ModelPersisterInfo;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.surf.types.PageImpl;

public class CoolPageImpl extends PageImpl implements CoolPage{
	private static final long serialVersionUID = 1L;

    /**
     * Enumeration of "required" Authentication level
     */
    public enum ExtendedRequiredAuthentication
    {
        none,
        guest,
        user,
        admin,
        coordinator,
        collaborator,
        contributor,
        editor,
        consumer
    }
	
	public CoolPageImpl(String id, ModelPersisterInfo key, Document document) {
		super(id, key, document);
	}
	
    public ExtendedRequiredAuthentication getExtendedAuthentication()
    {
    	ExtendedRequiredAuthentication authentication = ExtendedRequiredAuthentication.none;
        
        String auth = this.getProperty(PROP_AUTHENTICATION);
        if (auth != null)
        {
            try
            {
               authentication = ExtendedRequiredAuthentication.valueOf(auth.toLowerCase());
            }
            catch (IllegalArgumentException enumErr)
            {
               throw new PlatformRuntimeException(
                     "Invalid page <authentication> element value: " + auth);
            }
        }
        return authentication;
    }
	
}
