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
 * builder regen class for script library  builder
 */
public class ScriptLibraryBuilder extends BaseWebAppControlBuilder implements
		WebAppBuilder {

	private static final String FALSE = "false";
	private static final String HEAD = "HEAD";
	private static final String IS_RUNNING_STANDALONE_METHOD = "isRunningStandalone";
	public static final String INCLUDE_ALWAYS_OPTION = "IncludeAlways";
	public static final String SCRIPT_BUILDER_LIBRARIES_FOLDER = "/samples/script_builder/libraries/";

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
			String script = null;

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
					Constants.ServiceVarName, "serviceRestUrls");
			String includeLibrariesOption = builderInputs.getString(
					Constants.IncludeLibrariesOption, INCLUDE_ALWAYS_OPTION);

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

			// add ServiceConsumer if specified, REST enable it, and create JS
			// variables for the URLs
			if (addServiceProviderSupport
					&& !StringUtil.isEmpty(serviceProvider)) {
				String dataServiceName = "sc";
				ServiceConsumer2 sc = new ServiceConsumer2(builderCall,
						genContext);
				sc.setName(dataServiceName);
				sc.setProviderModel(serviceProvider);
				sc.setUseAllOperations(true);
				sc.invokeBuilder();

				RestServiceEnable rse = new RestServiceEnable(builderCall,
						genContext);
				rse.setDataServiceName(dataServiceName);
				rse.setServiceExecutionMode(RestServiceEnable.BuilderStaticValues.SEMVAL_LocalCall);
				rse.setResultType(RestServiceEnable.BuilderStaticValues.RTVAL_JSON);
				rse.invokeBuilder();

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

				DataService ds = webApp.getDataService(dataServiceName);
				if(ds != null){
					script = "var " + serviceVarName + " = {";
					Iterator<ServiceOperation> iter = ds.getOperations();
					while (iter.hasNext()) {
						ServiceOperation operation = iter.next();
						String varText = operation.getName()
								+ "URL: \"${MethodCall/" + operation.getName()
								+ "GenerateRestUrl}\",\n";
						script += varText;
	
					}
					script += "}";
				}
			}
			if (!allPages && pages == null) {
				builderCall.addMessage(BuilderCall.SEVERITY_ERROR, "no Pages");
				return;
			}

			Set<Page> inclPages = getPagesToProcess(webApp, allPages, pages);

			for (Page page : inclPages) {
				IXml pageIXml = page.getContents();
				if(pageIXml == null)
					continue;
				if(XmlUtil.findElements(pageIXml, "HTML/HEAD").size() == 0){
					IXml html = pageIXml.findElement("HTML");
					if(html == null)
						continue;
					IXml target = html.getFirstChildElement();
					if(target == null)
						html.addChildElement(XmlUtil.create(HEAD));
					else
						html.insertBefore(XmlUtil.create(HEAD),target);
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
					cjs.setPageLocation("Page " + pageName
							+ " XPath HTML/HEAD InsertAfter");
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
				if (script != null) {
					ClientJavaScript cjs = new ClientJavaScript(builderCall,
							genContext);
					cjs.setPageName(pageName);
					cjs.setPageLocationType(ClientJavaScript.BuilderStaticValues.PLTVAL_Implicit);
					cjs.setScriptSourceType(ClientJavaScript.BuilderStaticValues.SSTVAL_Explicit);
					// System.out.println("script: " + script);
					cjs.setScript(script);
					cjs.invokeBuilder();
				}
			}
			// If conditionally adding JS links, add a method to test for
			// running in
			// Portal
			if (!INCLUDE_ALWAYS_OPTION.equals(includeLibrariesOption)) {
				if (webApp.getMethod(IS_RUNNING_STANDALONE_METHOD) == null) {
					CodeFormatter cf = new CodeFormatter();
					cf.addLine("{");
					cf.addLine("javax.servlet.http.HttpServletRequest servletRequest = webAppAccess.getHttpServletRequest();");
					cf.addLine("Object portletRequest = servletRequest.getAttribute(com.bowstreet.adapters.Constants.PORTLET_REQUEST);");
					cf.addLine("return (portletRequest == null) ? true : false;");
					cf.addLine("}");
					Method method = webApp
							.addMethod(IS_RUNNING_STANDALONE_METHOD);
					method.setReturnType("boolean");
					method.setBody(cf.toString());
				}

			}
		}
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
		String libraryPage = libraryName + "_library";
		libraryPage = StringUtil.cleanIdentifier(StringUtil.replace(
				StringUtil.replace(libraryPage, "-", "_"), ".", "_"));
		if (webApp.getPage(libraryPage) == null) {
			ImportedPage ip = new ImportedPage(builderCall, genContext);
			ip.setName(libraryPage);
			// Form complete library filename by adding folder and
			// ".html"
			String libFilename = SCRIPT_BUILDER_LIBRARIES_FOLDER + libraryName
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
		ins.setPageLocation("Page " + pageName + " XPath HTML/HEAD");
		ins.setPage(libraryPage);
		ins.setReplaceTargetElement(false);
		ins.invokeBuilder();

		if (!INCLUDE_ALWAYS_OPTION.equals(includeLibrariesOption)) {
			// Add visibility setter on the entire HTML for the library page
			VisibilitySetter vs = new VisibilitySetter(builderCall, genContext);
			vs.setPageLocation("Page " + libraryPage + " XPath HTML");
			vs.setComparisonType(VisibilitySetter.BuilderStaticValues.CTVAL_HideWhenFalse);
			vs.setFirstValue("${MethodCall/isRunningStandalone}");
			vs.invokeBuilder();
		}
	}

	/**
	 * Interface that has constants for all the builder input names
	 */
	static public interface Constants {
		/* ##GENERATED_BEGIN */
		public static final String Name = "Name";
		public static final String Libraries = "Libraries";
		public static final String AllPages = "AllPages";
		public static final String Pages = "Pages";
		public static final String DisableSmartRefresh = "DisableSmartRefresh";
		public static final String DefaultRDD = "DefaultRDD";
		public static final String ScriptFile = "ScriptFile";
		public static final String CssFile = "CssFile";
		public static final String AddServiceProviderSupport = "AddServiceProviderSupport";
		public static final String ServiceProvider = "ServiceProvider";
		public static final String PageName = "PageName";
		public static final String ServiceVarName = "ServiceVarName";
		public static final String IncludeLibrariesOption = "IncludeLibrariesOption";
		/* ##GENERATED_END */

	}

}
