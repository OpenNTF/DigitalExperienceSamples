/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
*/
package com.ibm.wef.samples.builders;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

import com.bowstreet.editor.uitools.coordinator.WebAppBaseCoordinator;
import com.bowstreet.generation.DynamicBuilderInputDefinition;
import com.bowstreet.util.IXml;
import com.bowstreet.util.XmlUtil;
import com.bowstreet.webapp.Page;
import com.bowstreet.webapp.WebApp;
import com.bowstreet.webapp.WebAppObject;
import com.ibm.wef.samples.builders.ScriptLibraryBuilder.Constants;

/**
 * Coordinator implementation
 */
public class ScriptLibraryCoordinator extends WebAppBaseCoordinator implements FilenameFilter {

	InputDefinitions defs = new InputDefinitions();
	private WebApp webApp;
	 private String lastPages = null;
	  
	/**
	 * The initialization method is called each time the builder call is opened.
	 * Here you can set defaults, create dynamic pick lists, show/hide/create
	 * inputs.
	 */
	public String initializeInputs(boolean isNewBuilderCall) {
        webApp = getWebApp();
		/* ##GENERATED_BODY_BEGIN#CoordinatorDefInitCode# */
		// Generated code to initialize all the input definitions
		defs.name = context.findInputDefinition(Constants.Name);
		defs.libraries = context.findInputDefinition(Constants.Libraries);
		defs.allPages = context.findInputDefinition(Constants.AllPages);
		defs.pages = context.findInputDefinition(Constants.Pages);
		defs.scriptFile = context.findInputDefinition(Constants.ScriptFile);
		defs.cssFile = context.findInputDefinition(Constants.CssFile);
		defs.addServiceProviderSupport = context.findInputDefinition(Constants.AddServiceProviderSupport);
		defs.serviceProvider = context.findInputDefinition(Constants.ServiceProvider);
		defs.serviceVarName = context.findInputDefinition(Constants.ServiceVarName);
		setVisibility();
		
		return null;
	}

	/*
	 * Set visibility
	 */
	private void setVisibility() {
		boolean allPages = defs.allPages.getBoolean();
        defs.pages.setVisible(!allPages);
	}

	/**
	 * This is called whenever any input is changed. NOTE: This method must
	 * return "true" to have UI updated.
	 */
	public boolean processInputChange(DynamicBuilderInputDefinition changed) {

		if(changed == defs.allPages){
			boolean allPages = defs.allPages.getBoolean();
	        defs.pages.setVisible(!allPages);
	        if (!allPages)
	        {
	            IXml pagesData = defs.pages.getXml();
	            if (pagesData == null)
	            {
	                pagesData = XmlUtil.create("data"); //$NON-NLS-1$
	                defs.pages.setXml(pagesData);
	            }
	     	    StringBuffer pages = new StringBuffer();
	            Iterator<?> iterator = webApp.getPages();
	            String sep = ""; //$NON-NLS-1$
	            while (iterator.hasNext())
	            {
	                Page page = (Page) iterator.next();
					if(page.getVisibility() != WebAppObject.ALWAYS_VISIBLE)
						continue;
                    pages.append(sep).append(page.getName());
                    sep = ","; //$NON-NLS-1$
	            }
	            // It's expensive to change this, so don't overdo it.
	            String pagesStr = pages.toString();
	            if (!pagesStr.equals(lastPages))
	            {
	            	defs.pages.setArgument("ListData", pagesStr); //$NON-NLS-1$
	            	lastPages = pagesStr;
	                defs.pages.setXml(pagesData);
	            }	        
	        }
			return true;
		}
		// don't update UI by default
		return false;
	}

	/**
	 * Used as the filename filter for schema files.
	 * 
	 * @param dir
	 *            The directory in which the file was found.
	 * @param name
	 *            The name of the file.
	 * 
	 * @return A return value of true indicates that the file should be
	 *         included. A return value of false indicates that the file should
	 *         not be included.
	 */
	public boolean accept(File dir, String name) {
		return name.endsWith(".html"); //$NON-NLS-1$
	}

	/**
	 * This method is called whenever OK or Apply is pressed. NOTE: This method
	 * can be implemented to remove extra inputs, but this should only be done
	 * if the inputs aren't needed any more, since the Builder Call may still
	 * remain open for further editing after Apply is pressed.
	 */
	public void terminate() {
	}

	static class InputDefinitions {
		/* ##GENERATED_BEGIN */
		DynamicBuilderInputDefinition name;
		DynamicBuilderInputDefinition libraries;
		DynamicBuilderInputDefinition allPages;
		DynamicBuilderInputDefinition pages;
		DynamicBuilderInputDefinition disableSmartRefresh;
		DynamicBuilderInputDefinition defaultRDD;
		DynamicBuilderInputDefinition scriptFile;
		DynamicBuilderInputDefinition cssFile;
		DynamicBuilderInputDefinition addServiceProviderSupport;
		DynamicBuilderInputDefinition serviceProvider;
		DynamicBuilderInputDefinition serviceVarName;
		/* ##GENERATED_END */

	}

}
