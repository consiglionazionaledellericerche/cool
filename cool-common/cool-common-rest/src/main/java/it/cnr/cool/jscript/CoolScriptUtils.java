package it.cnr.cool.jscript;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;

/**
 * 
 * Spring Surf javascript processor extension, provides general utility
 * functions to be used in Cool Platform
 * 
 * @see it.cnr.si.repo.jscript.ScriptUtils
 * 
 * @author Francesco Uliana
 * 
 */
public class CoolScriptUtils extends BaseProcessorExtension{

	@Autowired
	private ApplicationContext applicationContext;

	public Object getBeanFromClass(String className) throws BeansException,
			ClassNotFoundException {
		return applicationContext.getBean(Class.forName(className));
	}

	public Object getBean(String beanName) {
		return applicationContext.getBean(beanName);
	}

	public String[] getBeanNamesForType(String className)
			throws ClassNotFoundException {
		Class<?> myClass = Class.forName(className);
		return applicationContext.getBeanNamesForType(myClass);
	}

	/* reflection utility methods */

	/**
	 * 
	 * Class.forName wrapper
	 * 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> getClass(String className) throws ClassNotFoundException {
		return Class.forName(className);
	}

}
