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

import java.util.Iterator;

import com.bowstreet.builders.webapp.api.ClientJavaScript;
import com.bowstreet.builders.webapp.api.ImportedPage;
import com.bowstreet.builders.webapp.api.InsertedPage;
import com.bowstreet.builders.webapp.api.RestServiceEnable;
import com.bowstreet.builders.webapp.api.ServiceConsumer2;
import com.bowstreet.builders.webapp.api.StyleSheet;
import com.bowstreet.builders.webapp.api.Theme;
import com.bowstreet.builders.webapp.api.VisibilitySetter;
import com.bowstreet.builders.webapp.foundation.WebAppBuilder;
import com.bowstreet.builderutilities.CodeFormatter;
import com.bowstreet.generation.BuilderCall;
import com.bowstreet.generation.BuilderInputs;
import com.bowstreet.generation.GenContext;
import com.bowstreet.util.IXml;
import com.bowstreet.util.StringUtil;
import com.bowstreet.util.XmlUtil;
import com.bowstreet.webapp.DataService;
import com.bowstreet.webapp.Method;
import com.bowstreet.webapp.Page;
import com.bowstreet.webapp.ServiceOperation;
import com.bowstreet.webapp.WebApp;
import com.bowstreet.webapp.WebAppObject;

/**
 * Builder regen class for Script Application builder
 */
public class ScriptApplicationBuilder implements WebAppBuilder {
	
	protected static final String FALSE = "false"; //$NON-NLS-1$


	/**
	 * This is the method that's called during generation of the WebApp.
	 */
	public void doBuilderCall(GenContext genContext, WebApp webApp, BuilderCall builderCall, BuilderInputs builderInputs) {
		// System.out.println("builderInputs: " + builderInputs);

		/* ##GENERATED_BODY_BEGIN#InputAccessorCode# */
		// Generated code to get all the builder inputs
		IXml libraries = builderInputs.getXml(Constants.Libraries, null);
		String htmlFile = builderInputs.getString(Constants.HtmlFile, null);
		String scriptFile = builderInputs.getString(Constants.ScriptFile, null);
		String cssFile = builderInputs.getString(Constants.CssFile, null);
		boolean addServiceProviderSupport = builderInputs.getBoolean(Constants.AddServiceProviderSupport, false);
		String serviceProvider = builderInputs.getString(Constants.ServiceProvider, null);
		String pageName = builderInputs.getString(Constants.PageName, "main");  //$NON-NLS-1$
		String serviceVarName = builderInputs.getString(Constants.ServiceVarName, "serviceRestUrls");   //$NON-NLS-1$
		String includeLibrariesOption = builderInputs.getString(Constants.IncludeLibrariesOption, SharedConstants.INCLUDE_ALWAYS_OPTION);
		boolean disableSmartRefresh = builderInputs.getBoolean(
				Constants.DisableSmartRefresh, true);
		String defaultRDD = builderInputs.getString(
				Constants.DefaultRDD, null);
		/* ##GENERATED_BODY_END */
		if (disableSmartRefresh || !StringUtil.isEmpty(defaultRDD)) {
			Theme theme = new Theme(builderCall, genContext);
			theme.setOverrideThemeProperties(true);
			if(disableSmartRefresh){
				theme.setUse_Smart_Refresh(FALSE);
				theme.setUse_Smart_Refresh_Contained(FALSE);
			}
			if(!StringUtil.isEmpty(defaultRDD))
				theme.setDefaultRDD(defaultRDD);
			theme.invokeBuilder();
			builderCall.clearMessage(BuilderCall.SEVERITY_WARNING);
		}


		// set readable name to the page name
		builderInputs.setString(BuilderCall.RESERVEDINPUT_DEFAULTNAME, pageName);
		
		// Call Imported Page for html file
		if (!StringUtil.isEmpty(htmlFile)) {
			ImportedPage ip = new ImportedPage(builderCall, genContext);
			ip.setName(pageName);
			ip.setURL(htmlFile);
			ip.setAbsoluteURLs(true);
			ip.setUseJSPCode(true);
			ip.setURLModification(ImportedPage.BuilderStaticValues.URLMVAL_Relative);
			ip.invokeBuilder();
		}
		// Add each library by importing a page then inserting it
		if (libraries != null) {
			for (@SuppressWarnings("rawtypes")  //$NON-NLS-1$
			Iterator iterator = libraries.getChildren().iterator(); iterator.hasNext();) {
				IXml libraryEntry = (IXml) iterator.next();
				String libraryName = libraryEntry.getText("Library");  //$NON-NLS-1$
				if (libraryName != null) {
					addLibraryReference(genContext, webApp, builderCall, pageName, libraryName, includeLibrariesOption);
				}
			}
		}
		// Add CSS and JS, if specified
		if (!StringUtil.isEmpty(scriptFile)) {
			ClientJavaScript cjs = new ClientJavaScript(builderCall, genContext);
			cjs.setPageName(pageName);
			cjs.setPageLocationType(ClientJavaScript.BuilderStaticValues.PLTVAL_Explicit);
			cjs.setScriptSourceType(ClientJavaScript.BuilderStaticValues.SSTVAL_Link);
			cjs.setScriptExternalLocation(scriptFile);
			cjs.setPageLocation("Page " + pageName + " XPath HTML/HEAD InsertAfter");  //$NON-NLS-1$  //$NON-NLS-2$
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

		// add ServiceConsumer if specified, REST enable it, and create JS
		// variables for the URLs
		if (addServiceProviderSupport && !StringUtil.isEmpty(serviceProvider)) {
			String dataServiceName = webApp.generateUniqueName("sc");  //$NON-NLS-1$
			if (webApp.getDataService(dataServiceName) == null) {
				ServiceConsumer2 sc = new ServiceConsumer2(builderCall, genContext);
				sc.setName(dataServiceName);
				sc.setProviderModel(serviceProvider);
				sc.setUseAllOperations(true);
				sc.invokeBuilder();
			}
			DataService ds = webApp.getDataService(dataServiceName);
			
			if(ds != null){
				// If RestServiceEnable methods aren't already present, call RestServiceEnable builder
				ServiceOperation op = ds.getOperations().next();
				if (op != null && (webApp.getMethod(op.getName() + "GenerateRestUrl")) == null) {  //$NON-NLS-1$
					RestServiceEnable rse = new RestServiceEnable(builderCall, genContext);
					rse.setDataServiceName(dataServiceName);
					rse.setServiceExecutionMode(RestServiceEnable.BuilderStaticValues.SEMVAL_LocalCall);
					rse.setResultType(RestServiceEnable.BuilderStaticValues.RTVAL_JSON);
					rse.invokeBuilder();
				}
				String script = getScript(serviceVarName, ds);
				ClientJavaScript cjs = new ClientJavaScript(builderCall, genContext);
				cjs.setPageName(pageName);
				cjs.setPageLocationType(ClientJavaScript.BuilderStaticValues.PLTVAL_Implicit);
				cjs.setScriptSourceType(ClientJavaScript.BuilderStaticValues.SSTVAL_Explicit);
				cjs.setScript(script);
				cjs.invokeBuilder();
			}
			else
				builderCall.addMessage(BuilderCall.SEVERITY_ERROR, "Data service not found");  //$NON-NLS-1$
		}
		// If conditionally adding JS links, add a method to test for running in Portal
		if (!SharedConstants.INCLUDE_ALWAYS_OPTION.equals(includeLibrariesOption)) {
			getConditionalJs(webApp);
		}
	}

	protected static String getScript(String serviceVarName, DataService ds) {
		// Add JS variable
		/*
		 * Form a script similar to this: var
		 * jqueryImportedJSOrdersSampleRESTURLs = { getOrdersURL:
		 * "${MethodCall/getOrdersGenerateRestUrl}", getOneOrderURL:
		 * "${MethodCall/getOneOrderGenerateRestUrl}", deleteOrderURL:
		 * "${MethodCall/deleteOrderGenerateRestUrl}", createOrderURL:
		 * "${MethodCall/createOrderGenerateRestUrl}", updateOrderURL:
		 * "${MethodCall/updateOrderGenerateRestUrl}" }
		 */
		String script = "var " + serviceVarName + " = {";

		Iterator<ServiceOperation> iter = ds.getOperations();
		while (iter.hasNext()) {
			ServiceOperation operation = iter.next();
			String varText = operation.getName() + "URL: \"${MethodCall/" + operation.getName()
					+ "GenerateRestUrl}\",\n";
			script += varText;
		}
		script += "}";  //$NON-NLS-1$
		return script;
	}

	protected static void getConditionalJs(WebApp webApp) {
		if (webApp.getMethod(SharedConstants.IS_RUNNING_STANDALONE_METHOD) == null) {
			CodeFormatter cf = new CodeFormatter();
			cf.addLine("{"); //$NON-NLS-1$
			cf.addLine("javax.servlet.http.HttpServletRequest servletRequest = webAppAccess.getHttpServletRequest();"); //$NON-NLS-1$
			cf.addLine("Object portletRequest = servletRequest.getAttribute(com.bowstreet.adapters.SharedConstants.PORTLET_REQUEST);"); //$NON-NLS-1$
			cf.addLine("return (portletRequest == null) ? true : false;"); //$NON-NLS-1$
			cf.addLine("}"); //$NON-NLS-1$
			Method method = webApp
					.addMethod(SharedConstants.IS_RUNNING_STANDALONE_METHOD);
			method.setReturnType("boolean"); //$NON-NLS-1$
			method.setBody(cf.toString());
		}
	}

	/*
	 * Add an imported page for the library and insert into HEAD of page
	 */
	private void addLibraryReference(GenContext genContext, WebApp webApp,
			BuilderCall builderCall, String pageName, String libraryName, String includeLibrariesOption) {
		String libraryPage = libraryName + "_library";  //$NON-NLS-1$
		libraryPage = StringUtil.cleanIdentifier(StringUtil.replace(
				StringUtil.replace(libraryPage, "-", "_"), ".", "_"));  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$  //$NON-NLS-4$
		// See if page is already there
		if (webApp.getPage(libraryPage) == null) {
			ImportedPage ip = new ImportedPage(builderCall, genContext);
			ip.setName(libraryPage);
			// Form complete library filename by adding folder and
			// ".html"
			String libFilename = SharedConstants.SCRIPT_BUILDER_LIBRARIES_FOLDER + libraryName + ".html";  //$NON-NLS-1$
			ip.setURL(libFilename);
			ip.setAbsoluteURLs(true);
			ip.setUseJSPCode(true);
			ip.setURLModification(ImportedPage.BuilderStaticValues.URLMVAL_Relative);
			ip.invokeBuilder();
			Page page = webApp.getPage(libraryPage);
			if(page != null)
				page.setVisibility(WebAppObject.NEVER_VISIBLE);
		}
	
		makeSurePageHasHead(webApp,pageName);
		// insert page into HEAD of main page
		InsertedPage ins = new InsertedPage(builderCall, genContext);
		// Location like this: Page main XPath HTML/HEAD
		ins.setPageLocation("Page " + pageName + " XPath HTML/HEAD");  //$NON-NLS-1$  //$NON-NLS-2$
		ins.setPage(libraryPage);
		ins.setReplaceTargetElement(false);
		ins.invokeBuilder();

		if (!SharedConstants.INCLUDE_ALWAYS_OPTION.equals(includeLibrariesOption)) {
			// Add visibility setter on the entire HTML for the library page
			VisibilitySetter vs = new VisibilitySetter(builderCall, genContext);
			vs.setPageLocation("Page " + libraryPage + " XPath HTML");  //$NON-NLS-1$  //$NON-NLS-2$
			vs.setComparisonType(VisibilitySetter.BuilderStaticValues.CTVAL_HideWhenFalse);
			vs.setFirstValue("${MethodCall/isRunningStandalone}");  //$NON-NLS-1$
			vs.invokeBuilder();
		}
	}

	private void makeSurePageHasHead(WebApp webApp, String pageName) {
		Page page = webApp.getPage(pageName);
		if(page != null){
			IXml pageIXml = page.getContents();
			if(pageIXml != null){
				if(XmlUtil.findElements(pageIXml, SharedConstants.HTML_HEAD).size() == 0){
					IXml html = pageIXml.findElement(SharedConstants.HTML);
					if(html != null){
						IXml target = html.getFirstChildElement();
						if(target == null)
							html.addChildElement(XmlUtil.create(SharedConstants.HEAD));
						else
							html.insertBefore(XmlUtil.create(SharedConstants.HEAD),target);
					}
				}
			}
		}		
	}

	/**
	 * Interface that has constants for all the builder input names
	 */
	static public interface Constants {
		/* ##GENERATED_BEGIN */
		public static final String Name = "Name"; //$NON-NLS-1$
		public static final String Libraries = "Libraries";  //$NON-NLS-1$
		public static final String HtmlFile = "HtmlFile";  //$NON-NLS-1$
		public static final String ScriptFile = "ScriptFile";  //$NON-NLS-1$
		public static final String CssFile = "CssFile";  //$NON-NLS-1$
		public static final String AddServiceProviderSupport = "AddServiceProviderSupport";  //$NON-NLS-1$
		public static final String ServiceProvider = "ServiceProvider";  //$NON-NLS-1$
		public static final String PageName = "PageName";  //$NON-NLS-1$
		public static final String ServiceVarName = "ServiceVarName";  //$NON-NLS-1$
		public static final String IncludeLibrariesOption = "IncludeLibrariesOption";  //$NON-NLS-1$
		public static final String DisableSmartRefresh = "DisableSmartRefresh"; //$NON-NLS-1$
		public static final String DefaultRDD = "DefaultRDD"; //$NON-NLS-1$
		/* ##GENERATED_END */
	}
	static public interface SharedConstants {
		public static final String HTML_HEAD = "HTML/HEAD"; //$NON-NLS-1$
		public static final String IS_RUNNING_STANDALONE_METHOD = "isRunningStandalone"; //$NON-NLS-1$
		public static final String INCLUDE_ALWAYS_OPTION = "IncludeAlways"; //$NON-NLS-1$
		public static final String SCRIPT_BUILDER_LIBRARIES_FOLDER = "/samples/script_builder/libraries/"; //$NON-NLS-1$
		public static final String HEAD = "HEAD"; //$NON-NLS-1$
		public static final String HTML = "HTML"; //$NON-NLS-1$
	}
}
