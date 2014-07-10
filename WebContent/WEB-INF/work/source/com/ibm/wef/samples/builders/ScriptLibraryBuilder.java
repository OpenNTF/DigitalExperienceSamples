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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.bowstreet.builders.webapp.api.ClientJavaScript;
import com.bowstreet.builders.webapp.api.ImportedPage;
import com.bowstreet.builders.webapp.api.InsertedPage;
import com.bowstreet.builders.webapp.api.RestServiceEnable;
import com.bowstreet.builders.webapp.api.ServiceConsumer2;
import com.bowstreet.builders.webapp.api.StyleSheet;
import com.bowstreet.builders.webapp.api.Theme;
import com.bowstreet.builders.webapp.api.VisibilitySetter;
import com.bowstreet.builders.webapp.foundation.BaseWebAppControlBuilder;
import com.bowstreet.builders.webapp.foundation.WebAppBuilder;
import com.bowstreet.generation.BuilderCall;
import com.bowstreet.generation.BuilderInputs;
import com.bowstreet.generation.GenContext;
import com.bowstreet.util.IXml;
import com.bowstreet.util.StringUtil;
import com.bowstreet.util.XmlUtil;
import com.bowstreet.webapp.DataService;
import com.bowstreet.webapp.Page;
import com.bowstreet.webapp.WebApp;
import com.bowstreet.webapp.WebAppObject;
import com.ibm.wef.samples.builders.ScriptApplicationBuilder.SharedConstants;
/**
 * builder regen class for script library  builder
 */
public class ScriptLibraryBuilder extends BaseWebAppControlBuilder implements
		WebAppBuilder {

	protected String getPreferredPhase() {
		return builderInputs.getString(
				"TargetPhase", GenContext.PHASE_POSTCONSTRUCTION); //$NON-NLS-1$
	}

	/**
	 * This is the method that's called during generation of the WebApp.
	 */
	public void doBuilderCall(GenContext genContext, WebApp webApp,
			BuilderCall builderCall, BuilderInputs builderInputs) {
		// System.out.println("builderInputs: " + builderInputs);
		if (genContext.getGenerationPhase().equals(
				GenContext.PHASE_CONSTRUCTION)) {
			genContext.deferBuilderCall(GenContext.PHASE_POSTCONSTRUCTION,
					builderCall);
		} else {
			String dataService = null;

			/* ##GENERATED_BODY_BEGIN#InputAccessorCode# */
			// Generated code to get all the builder inputs
			IXml libraries = builderInputs.getXml(Constants.Libraries, null);
			boolean allPages = builderInputs.getBoolean(Constants.AllPages,
					true);
			boolean disableSmartRefresh = builderInputs.getBoolean(
					Constants.DisableSmartRefresh, true);
			String defaultRDD = builderInputs.getString(
					Constants.DefaultRDD, null);
			IXml pages = builderInputs.getXml(Constants.Pages, null);
			String scriptFile = builderInputs.getString(Constants.ScriptFile,
					null);
			String cssFile = builderInputs.getString(Constants.CssFile, null);
			boolean addServiceProviderSupport = builderInputs.getBoolean(
					Constants.AddServiceProviderSupport, false);
			String serviceProvider = builderInputs.getString(
					Constants.ServiceProvider, null);
			String serviceVarName = builderInputs.getString(
					Constants.ServiceVarName, "serviceRestUrls"); //$NON-NLS-1$
			String includeLibrariesOption = builderInputs.getString(
					Constants.IncludeLibrariesOption, SharedConstants.INCLUDE_ALWAYS_OPTION);

			if (disableSmartRefresh || !StringUtil.isEmpty(defaultRDD)) {
				Theme theme = new Theme(builderCall, genContext);
				theme.setOverrideThemeProperties(true);
				if(disableSmartRefresh){
					theme.setUse_Smart_Refresh(com.ibm.wef.samples.builders.ScriptApplicationBuilder.FALSE);
					theme.setUse_Smart_Refresh_Contained(com.ibm.wef.samples.builders.ScriptApplicationBuilder.FALSE);
				}
				if(!StringUtil.isEmpty(defaultRDD))
					theme.setDefaultRDD(defaultRDD);
				theme.invokeBuilder();
				builderCall.clearMessage(BuilderCall.SEVERITY_WARNING);
			}

			// add ServiceConsumer if specified, REST enable it, and create JS
			// variables for the URLs
			if (addServiceProviderSupport
					&& !StringUtil.isEmpty(serviceProvider)) {
				String dataServiceName = com.ibm.wef.samples.builders.ScriptApplicationBuilder.DEFAULT_SERVICE_NAME;
				ServiceConsumer2 sc = new ServiceConsumer2(builderCall,
						genContext);
				sc.setName(dataServiceName);
				sc.setProviderModel(serviceProvider);
				sc.setUseAllOperations(true);
				sc.invokeBuilder();
				dataService = dataServiceName;
			}
			if (!allPages && pages == null) {
				builderCall.addMessage(BuilderCall.SEVERITY_ERROR, "no Pages"); //$NON-NLS-1$
				return;
			}

			if (dataService != null) {
				addRestDataServiceToPage(genContext, webApp, builderCall, dataService);
			}
			IXml extraServices = builderInputs.getXml(Constants.ExtraServices, null);
			if (extraServices != null) {
				for (@SuppressWarnings("rawtypes")  //$NON-NLS-1$
				Iterator iterator = extraServices.getChildren().iterator(); iterator.hasNext();) {
					IXml dataServiceEntry = (IXml) iterator.next();
					String serviceName = dataServiceEntry.getText("DataService");  //$NON-NLS-1$
					serviceVarName = dataServiceEntry.getText("VariableName");  //$NON-NLS-1$
					if (serviceName != null && serviceVarName != null) {
						addRestDataServiceToPage(genContext, webApp, builderCall, serviceName);
					}
				}
			}
			Set<Page> inclPages = getPagesToProcess(webApp, allPages, pages);

			for (Page page : inclPages) {
				IXml pageIXml = page.getContents();
				if(pageIXml == null)
					continue;
				if(XmlUtil.findElements(pageIXml, "HTML/HEAD").size() == 0){ //$NON-NLS-1$
					IXml html = pageIXml.findElement(SharedConstants.HTML);
					if(html == null)
						continue;
					IXml target = html.getFirstChildElement();
					if(target == null)
						html.addChildElement(XmlUtil.create(SharedConstants.HEAD));
					else
						html.insertBefore(XmlUtil.create(SharedConstants.HEAD),target);
				}
					
				String pageName = page.getName();
				// Add each library by importing a page then inserting it
				if (libraries != null) {
					for (Iterator<?> iterator = libraries.getChildren()
							.iterator(); iterator.hasNext();) {
						IXml libraryEntry = (IXml) iterator.next();
						String libraryName = libraryEntry.getText("Library");
						if (libraryName != null) {
							addLibraryReference(webApp, genContext,
									builderCall, pageName, libraryName,
									includeLibrariesOption);
						}

					}
				}
				// Add CSS and JS, if specified
				if (!StringUtil.isEmpty(scriptFile)) {
					ClientJavaScript cjs = new ClientJavaScript(builderCall,
							genContext);
					cjs.setPageName(pageName);
					cjs.setPageLocationType(ClientJavaScript.BuilderStaticValues.PLTVAL_Explicit);
					cjs.setScriptSourceType(ClientJavaScript.BuilderStaticValues.SSTVAL_Link);
					cjs.setScriptExternalLocation(scriptFile);
					cjs.setPageLocation("Page " + pageName //$NON-NLS-1$
							+ " XPath HTML/HEAD InsertAfter"); //$NON-NLS-1$
					cjs.invokeBuilder();
				}
				if (!StringUtil.isEmpty(cssFile)) {
					StyleSheet ss = new StyleSheet(builderCall, genContext);
					ss.setExternalLocation(cssFile);
					ss.setPageLocationType(StyleSheet.BuilderStaticValues.PLTVAL_Implicit);
					ss.setPageName(pageName);
					ss.setSourceType(StyleSheet.BuilderStaticValues.STVAL_Link);
					ss.invokeBuilder();
				}
				if (dataService != null) {
					addScriptToPage(genContext, webApp, builderCall, pageName,
							serviceVarName, dataService);
				}
				if (extraServices != null) {
					for (@SuppressWarnings("rawtypes")  //$NON-NLS-1$
					Iterator iterator = extraServices.getChildren().iterator(); iterator.hasNext();) {
						IXml dataServiceEntry = (IXml) iterator.next();
						String serviceName = dataServiceEntry.getText("DataService");  //$NON-NLS-1$
						serviceVarName = dataServiceEntry.getText("VariableName");  //$NON-NLS-1$
						addScriptToPage(genContext, webApp, builderCall, pageName,
									serviceVarName, serviceName);
					}
				}
			}
			// If conditionally adding JS links, add a method to test for
			// running in
			// Portal
			if (!SharedConstants.INCLUDE_ALWAYS_OPTION.equals(includeLibrariesOption)) {
				com.ibm.wef.samples.builders.ScriptApplicationBuilder.getConditionalJs(webApp);

			}
		}
	}

	private void addRestDataServiceToPage(GenContext genContext, WebApp webApp,
			BuilderCall builderCall, String serviceName) {
		if(!StringUtil.isEmpty(serviceName)){
			RestServiceEnable rse = new RestServiceEnable(builderCall,
					genContext);
			rse.setDataServiceName(serviceName);
			rse.setServiceExecutionMode(RestServiceEnable.BuilderStaticValues.SEMVAL_LocalCall);
			rse.setResultType(RestServiceEnable.BuilderStaticValues.RTVAL_JSON);
			rse.invokeBuilder();
		}		
	}

	private void addScriptToPage(GenContext genContext, WebApp webApp,
			BuilderCall builderCall, String pageName, String serviceVarName,
			String serviceName) {
		if(!StringUtil.isEmpty(serviceVarName)){
			if(!StringUtil.isEmpty(serviceName)){
			DataService ds = webApp.getDataService(serviceName);
			if(ds != null){
				String script = com.ibm.wef.samples.builders.ScriptApplicationBuilder.getScript(serviceVarName, ds);
				ClientJavaScript cjs = new ClientJavaScript(builderCall,
						genContext);
				cjs.setPageName(pageName);
				cjs.setPageLocationType(ClientJavaScript.BuilderStaticValues.PLTVAL_Implicit);
				cjs.setScriptSourceType(ClientJavaScript.BuilderStaticValues.SSTVAL_Explicit);
				// System.out.println("script: " + script);
				cjs.setScript(script);
				cjs.invokeBuilder();
			}
			else
				builderCall.addMessage(BuilderCall.SEVERITY_ERROR,  serviceName + " Data service not found");  //$NON-NLS-1$
		}
		else
			builderCall.addMessage(BuilderCall.SEVERITY_WARNING,  " No data service name");  //$NON-NLS-1$
	}
	else
		builderCall.addMessage(BuilderCall.SEVERITY_WARNING,  " No service variable name");  //$NON-NLS-1$
	}

	private Set<Page> getPagesToProcess(WebApp webApp, boolean allPages,
			IXml pages) {
		Set<Page> incl = new HashSet<Page>();
		if (allPages) {
			Iterator<?> iter = webApp.getPages();
			while (iter.hasNext()) {
				Page page = (Page) iter.next();
				if(page.getVisibility() != WebAppObject.ALWAYS_VISIBLE)
					continue;
               	incl.add(page);
			}
		} else {
			if (pages != null) {
				for (IXml node = pages.getFirstChildElement(); node != null; node = node
						.getNextSiblingElement()) {
					String tf = node.getText();
					if (Boolean.TRUE.toString().equalsIgnoreCase(tf)) {
						String name = node.getName();
						Page page = webApp.getPage(name);
						if (page != null) {
							incl.add(page);
						}
					}
				}
			}
		}
		return incl;
	}

	/*
	 * Add an imported page for the library
	 */
	private void addLibraryReference(WebApp webApp, GenContext genContext,
			BuilderCall builderCall, String pageName, String libraryName,
			String includeLibrariesOption) {
		String libraryPage = libraryName + "_library"; //$NON-NLS-1$
		libraryPage = StringUtil.cleanIdentifier(StringUtil.replace(
				StringUtil.replace(libraryPage, "-", "_"), ".", "_")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (webApp.getPage(libraryPage) == null) {
			ImportedPage ip = new ImportedPage(builderCall, genContext);
			ip.setName(libraryPage);
			// Form complete library filename by adding folder and
			// ".html"
			String libFilename = SharedConstants.SCRIPT_BUILDER_LIBRARIES_FOLDER + libraryName
					+ ".html";
			ip.setURL(libFilename);
			ip.setAbsoluteURLs(true);
			ip.setUseJSPCode(true);
			ip.setURLModification(ImportedPage.BuilderStaticValues.URLMVAL_Relative);
			ip.invokeBuilder();
			Page page = webApp.getPage(libraryPage);
			if(page != null)
				page.setVisibility(WebAppObject.NEVER_VISIBLE);

		}
		// insert page into HEAD of main page
		InsertedPage ins = new InsertedPage(builderCall, genContext);
		// Location like this: Page main XPath HTML/HEAD
		ins.setPageLocation("Page " + pageName + " XPath HTML/HEAD"); //$NON-NLS-1$
		ins.setPage(libraryPage);
		ins.setReplaceTargetElement(false);
		ins.invokeBuilder();

		if (!SharedConstants.INCLUDE_ALWAYS_OPTION.equals(includeLibrariesOption)) {
			// Add visibility setter on the entire HTML for the library page
			VisibilitySetter vs = new VisibilitySetter(builderCall, genContext);
			vs.setPageLocation("Page " + libraryPage + " XPath HTML"); //$NON-NLS-1$
			vs.setComparisonType(VisibilitySetter.BuilderStaticValues.CTVAL_HideWhenFalse);
			vs.setFirstValue("${MethodCall/isRunningStandalone}"); //$NON-NLS-1$
			vs.invokeBuilder();
		}
	}

	/**
	 * Interface that has constants for all the builder input names
	 */
	static public interface Constants {
		/* ##GENERATED_BEGIN */
		public static final String Name = "Name"; //$NON-NLS-1$
		public static final String Libraries = "Libraries"; //$NON-NLS-1$
		public static final String AllPages = "AllPages"; //$NON-NLS-1$
		public static final String Pages = "Pages"; //$NON-NLS-1$
		public static final String DisableSmartRefresh = "DisableSmartRefresh"; //$NON-NLS-1$
		public static final String DefaultRDD = "DefaultRDD"; //$NON-NLS-1$
		public static final String ScriptFile = "ScriptFile"; //$NON-NLS-1$
		public static final String CssFile = "CssFile"; //$NON-NLS-1$
		public static final String AddServiceProviderSupport = "AddServiceProviderSupport"; //$NON-NLS-1$
		public static final String ServiceProvider = "ServiceProvider"; //$NON-NLS-1$
		public static final String PageName = "PageName"; //$NON-NLS-1$
		public static final String ServiceVarName = "ServiceVarName"; //$NON-NLS-1$
		public static final String IncludeLibrariesOption = "IncludeLibrariesOption"; //$NON-NLS-1$
		public static final String ExtraServices = "ExtraServices"; ////$NON-NLS-1$
		/* ##GENERATED_END */

	}

}
