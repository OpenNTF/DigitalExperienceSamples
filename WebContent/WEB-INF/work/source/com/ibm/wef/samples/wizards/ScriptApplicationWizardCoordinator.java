/*
  * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.wef.samples.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bowstreet.BSConfig;
import com.bowstreet.editor.uitools.coordinator.WebAppBaseCoordinator;
import com.bowstreet.generation.DynamicBuilderInputDefinition;
import com.bowstreet.util.StringUtil;
import com.ibm.wef.samples.AppFolderInfo;
import com.ibm.wef.samples.TemplateUtil;
import com.ibm.wef.samples.ZipUtil;
import com.ibm.wef.samples.builders.ScriptApplicationBuilder.SharedConstants;

/**
 * Coordinator implementation
 */
public class ScriptApplicationWizardCoordinator extends WebAppBaseCoordinator
		implements FilenameFilter {

	private static final String USE_FILE = "UseFile"; //$NON-NLS-1$
	private static final String INDEX_HTML = "index.html"; //$NON-NLS-1$
	private static final String SAMPLES_SCRIPTING_WIZARD_PAGES = "/samples/script_builder/wizard_templates"; //$NON-NLS-1$
	private static final String SAMPLES_SCRIPTING = "/samples/apps/"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$
	private static final String TEMPLATE_DEFAULT = "base-script-app"; //$NON-NLS-1$
	private static final String HTML = ".html"; //$NON-NLS-1$
	private static final String CSS = ".css"; //$NON-NLS-1$
	private static final String JS = ".js"; //$NON-NLS-1$
	private static final int tHtml = 0;
	private static final int tCss = 1;
	private static final int tJs = 2;
	InputDefinitions defs = new InputDefinitions();
	private HashMap<String, String> htmlChoices;
	private HashMap<String, String> cssChoices;
	private HashMap<String, String> scriptChoices;

	/**
	 * The initialization method is called each time the builder call is opened.
	 * Here you can set defaults, create dynamic pick lists, show/hide/create
	 * inputs.
	 */
	public String initializeInputs(boolean isNewBuilderCall) {
		defs.name = context.findInputDefinition(Constants.Name);
		defs.portletTitle = context.findInputDefinition(Constants.PortletTitle);
		defs.singleFileAPP = context
				.findInputDefinition(Constants.SingleFileAPP);
		defs.dispLibraryInstruction = context
				.findInputDefinition(Constants.DispLibraryInstruction);
		defs.cssFile = context.findInputDefinition(Constants.CssFile);
		defs.htmlFile = context.findInputDefinition(Constants.HtmlFile);
		defs.scriptFile = context.findInputDefinition(Constants.ScriptFile);
		defs.htmlChoices = context.findInputDefinition(Constants.HtmlChoices);
		defs.includeLibrariesOption = context
				.findInputDefinition(Constants.IncludeLibrariesOption);
		defs.libraries = context.findInputDefinition(Constants.Libraries);
		defs.portletAdapter_BuilderCallEnabled = context
				.findInputDefinition(Constants.PortletAdapter_BuilderCallEnabled);
		defs.addServiceProviderSupport = context
				.findInputDefinition(Constants.AddServiceProviderSupport);
		defs.serviceProvider = context
				.findInputDefinition(Constants.ServiceProvider);
		defs.importFromZip = context
				.findInputDefinition(Constants.ImportFromZip);
		defs.zipFile = context.findInputDefinition(Constants.ZipFile);
		defs.deleteZip = context.findInputDefinition(Constants.DeleteZip);
		defs.templating = context.findInputDefinition(Constants.Templating);
		defs.templateFile = context.findInputDefinition(Constants.TemplateFile);
		defs.oldTemplating = context.findInputDefinition(Constants.OldTemplating);
		defs.newTemplating = context.findInputDefinition(Constants.NewTemplating);
		defs.scriptToReplace = context.findInputDefinition(Constants.ScriptToReplace);
		defs.portletAdapter_BuilderCallEnabled
				.setBoolean(true);
		defs.singleFileAPP.setString(SharedConstants.ADDLIBRARIES);
		initPickers();
		defs.htmlChoices.setString(TEMPLATE_DEFAULT);
		showDeploymentRelatedFields();

		super.initializeInputs(isNewBuilderCall);

		return null;
	}

	private void showDeploymentRelatedFields() {
		boolean vis = defs.portletAdapter_BuilderCallEnabled.getBoolean();
		defs.portletTitle.setVisible(vis);
		defs.includeLibrariesOption.setVisible(vis);
	}

	/**
	 * This is called whenever any input is changed. NOTE: This method must
	 * return "true" to have UI updated.
	 */
	public boolean processInputChange(DynamicBuilderInputDefinition changed) {
		String changedName = changed.getName().intern();
		if (changedName == Constants.Name) {
			if (!checkFolder(defs.name.getString())) {
				context.getDesigner()
						.openError(
								"Application Name", "Application folder already exisits"); //$NON-NLS-1$ //$NON-NLS-2$
				defs.name.setString("");
			}
			return true;
		} else if (changed == defs.portletAdapter_BuilderCallEnabled) {
			showDeploymentRelatedFields();
			return true;
		} else if (changed == defs.singleFileAPP) {
			showLibrayRelatedFields();
			return true;
		} else if (changed == defs.importFromZip) {
			showZipRelatedFields();
			return true;
		} else if (changed == defs.templating) {
			showTemplateRelatedFields();
			return true;
		}
		return false;
	}

	private void showZipRelatedFields() {
		boolean importFromZip = !USE_FILE
				.equals(defs.importFromZip.getString());
		defs.deleteZip.setVisible(importFromZip);
		defs.zipFile.setVisible(importFromZip);
		defs.htmlFile.setVisible(!importFromZip);
		showTemplateRelatedFields();
	}

	private void showTemplateRelatedFields() {
		boolean importFromZip = !USE_FILE
				.equals(defs.importFromZip.getString());
		defs.templating.setVisible(importFromZip);
		boolean templating = defs.templating.getBoolean();
		if(!importFromZip || !templating){
			defs.oldTemplating.setVisible(false);
			defs.newTemplating.setVisible(false);
			defs.scriptToReplace.setVisible(false);
			defs.templateFile.setVisible(false);
		}else{
			defs.oldTemplating.setVisible(true);
			defs.newTemplating.setVisible(true);
			defs.scriptToReplace.setVisible(true);
			defs.templateFile.setVisible(true);
		}
			
			
	}

	private void showLibrayRelatedFields() {
		boolean addLibraries = SharedConstants.ADDLIBRARIES
				.equals(defs.singleFileAPP.getString());
		defs.dispLibraryInstruction.setVisible(addLibraries);
		defs.libraries.setVisible(addLibraries);
		defs.htmlChoices.setVisible(addLibraries);
		defs.includeLibrariesOption.setVisible(addLibraries);
		defs.htmlFile.setVisible(!addLibraries);
		defs.importFromZip.setVisible(!addLibraries);
		defs.importFromZip.setString(USE_FILE);
		defs.zipFile.setVisible(false);
		defs.deleteZip.setVisible(false);
	}

	private boolean checkFolder(String name) {
		String destFileDir = BSConfig.getHtmlRootDir() + SAMPLES_SCRIPTING
				+ '/' + name;
		File destFolder = new File(destFileDir);
		// name is no good if the folder already exists
		return !destFolder.exists();
	}

	public void terminate() {
		String fileName = defs.zipFile.getString();
		String name = defs.name.getString();
		if (SharedConstants.ADDLIBRARIES.equals(defs.singleFileAPP.getString())) {
			String htmlChoice = defs.htmlChoices.getString();
			String htmlFile = getNewFile(htmlChoices.get(htmlChoice), name,
					tHtml);
			defs.htmlFile.setString(htmlFile);
			if (htmlFile != null) {
				String cssFile = getNewFile(cssChoices.get(htmlChoice), name,
						tCss);
				defs.cssFile.setString(cssFile);

				String scriptFile = getNewFile(scriptChoices.get(htmlChoice),
						name, tJs);
				defs.scriptFile.setString(scriptFile);
			}
		}
		if (!StringUtil.isEmpty(fileName)) {
			defs.htmlFile.setString(unzipFile(fileName, name,
					defs.deleteZip.getBoolean()));
		}
	}

	private String unzipFile(String fileName, String name, boolean bRemove) {
		String rootDir = BSConfig.getHtmlRootDir();
		String sourceFile = rootDir + fileName;
		File zipFile = new File(sourceFile);
		String destFileDir = rootDir + SAMPLES_SCRIPTING;
		String rVal = SAMPLES_SCRIPTING + name + '/';
		File appsFolder = new File(destFileDir);
		appsFolder.mkdir();
		destFileDir += name;
		File destFolder = new File(destFileDir);
		try {
			ZipUtil.unzipFile(zipFile, destFolder); //$NON-NLS-1$
			// remove the imported file
			if (bRemove)
				ZipUtil.remove(zipFile);
			AppFolderInfo appInfo = new AppFolderInfo(destFileDir);
			String hFile = appInfo.findFile(INDEX_HTML);
			if(hFile != null)
				rVal += hFile;
			else{
				List<String> files = appInfo.getHtmlFiles();
				if(!files.isEmpty())
					rVal += files.get(0);
			}
			if(defs.templating.getBoolean()){
				TemplateUtil templateUtil = new TemplateUtil();
				templateUtil.fixTemplating(appInfo, defs.oldTemplating.getString(),defs.newTemplating.getString(),defs.scriptToReplace.getString(),defs.templateFile.getString() );
			}
		} catch (IOException e) {
		}
		return rVal;
	}

	private String getNewFile(String fileName, String name, int type) {
		String sourceFileDir = BSConfig.getHtmlRootDir()
				+ SAMPLES_SCRIPTING_WIZARD_PAGES + '/';
		String destFileDir = BSConfig.getHtmlRootDir() + SAMPLES_SCRIPTING;
		File appsFolder = new File(destFileDir);
		appsFolder.mkdir();
		destFileDir += name;
		String childFolder = null;
		File destFolder = new File(destFileDir);
		// if new html file create the folder it shouldn't exist
		if (type == tHtml) {
			destFolder.mkdir();
		}
		// if css file create the folder it shouldn't exist
		if (type == tCss) {
			childFolder = '/' + "css"; //$NON-NLS-1$
			destFileDir += childFolder;
			destFolder = new File(destFileDir);
			destFolder.mkdir();
		} else
		// if js file create the folder it shouldn't exist
		if (type == tJs) {
			childFolder = '/' + "js"; //$NON-NLS-1$
			destFileDir += childFolder;
			destFolder = new File(destFileDir);
			destFolder.mkdir();
		}
		if (fileName != null) {
			File srcFile = new File(sourceFileDir + fileName);
			if (type == tHtml)
				// html page is always index.html so set dest to index.html
				fileName = INDEX_HTML;
			else {
				if (fileName.startsWith("base-script-app")) {
					if (type == tCss)
						fileName = "base.css"; //$NON-NLS-1$
					else
						fileName = "base.js"; //$NON-NLS-1$
				}
			}

			String fName = destFileDir + '/' + fileName;
			File destFile = new File(fName);
			try {
				if (destFile.createNewFile()) {
					copyFileUsingStream(srcFile, destFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (childFolder != null)
				return SAMPLES_SCRIPTING + name + childFolder + '/' + fileName;
			else
				return SAMPLES_SCRIPTING + name + '/' + fileName;
		}
		return null;
	}

	private void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	static class InputDefinitions {
		DynamicBuilderInputDefinition name;
		DynamicBuilderInputDefinition portletAdapter_BuilderCallEnabled;
		DynamicBuilderInputDefinition portletTitle;
		DynamicBuilderInputDefinition dispLibraryInstruction;
		DynamicBuilderInputDefinition libraries;
		DynamicBuilderInputDefinition includeLibrariesOption;
		DynamicBuilderInputDefinition htmlFile;
		DynamicBuilderInputDefinition htmlChoices;
		DynamicBuilderInputDefinition scriptFile;
		DynamicBuilderInputDefinition cssFile;
		DynamicBuilderInputDefinition addServiceProviderSupport;
		DynamicBuilderInputDefinition serviceProvider;
		DynamicBuilderInputDefinition singleFileAPP;
		DynamicBuilderInputDefinition importFromZip;
		DynamicBuilderInputDefinition zipFile;
		DynamicBuilderInputDefinition deleteZip;
		DynamicBuilderInputDefinition templating;
		DynamicBuilderInputDefinition templateFile;
		DynamicBuilderInputDefinition scriptToReplace;
		DynamicBuilderInputDefinition oldTemplating;
		DynamicBuilderInputDefinition newTemplating;
	}

	static public interface Constants {
		public static final String Name = "Name"; //$NON-NLS-1$
		public static final String PortletAdapter_BuilderCallEnabled = "PortletAdapter_BuilderCallEnabled"; //$NON-NLS-1$
		public static final String PortletTitle = "PortletTitle"; //$NON-NLS-1$
		public static final String SingleFileAPP = "SingleFileAPP"; //$NON-NLS-1$
		public static final String DispLibraryInstruction = "DispLibraryInstruction"; //$NON-NLS-1$
		public static final String Libraries = "Libraries"; //$NON-NLS-1$
		public static final String IncludeLibrariesOption = "IncludeLibrariesOption"; //$NON-NLS-1$
		public static final String HtmlChoices = "HtmlChoices"; //$NON-NLS-1$
		public static final String HtmlFile = "HtmlFile"; //$NON-NLS-1$
		public static final String ScriptFile = "ScriptFile"; //$NON-NLS-1$
		public static final String CssFile = "CssFile"; //$NON-NLS-1$
		public static final String AddServiceProviderSupport = "AddServiceProviderSupport"; //$NON-NLS-1$
		public static final String ServiceProvider = "ServiceProvider"; //$NON-NLS-1$
		public static final String ImportFromZip = "ImportFromZip"; //$NON-NLS-1$
		public static final String ZipFile = "ZipFile"; //$NON-NLS-1$
		public static final String DeleteZip = "DeleteZip"; //$NON-NLS-1$
		public static final String NewTemplating = "NewTemplating"; //$NON-NLS-1$
		public static final String OldTemplating = "OldTemplating"; //$NON-NLS-1$
		public static final String ScriptToReplace = "ScriptToReplace"; //$NON-NLS-1$
		public static final String TemplateFile = "TemplateFile"; //$NON-NLS-1$
		public static final String Templating = "Templating"; //$NON-NLS-1$
	}

	/*
	 * Initialize the pick list of libraries, by getting a list of HTML files in
	 * a particular folder.
	 */
	private void initPickers() {

		htmlChoices = new HashMap<String, String>();
		cssChoices = new HashMap<String, String>();
		scriptChoices = new HashMap<String, String>();

		// Build list from the files in /samples/script_builder/libraries/
		try {
			File dir = new File(BSConfig.getHtmlRootDir()
					+ SAMPLES_SCRIPTING_WIZARD_PAGES);

			// The accept() method is used to filter the files returned from
			// this method call.
			File[] files = dir.listFiles(this);

			for (int x = 0; x < files.length; x++) {
				if (files[x].getName().endsWith(HTML)) {
					String name = StringUtil.replace(files[x].getName(), HTML,
							EMPTY);
					htmlChoices.put(name, files[x].getName());
				} else if (files[x].getName().endsWith(CSS)) {
					String name = StringUtil.replace(files[x].getName(), CSS,
							EMPTY);
					cssChoices.put(name, files[x].getName());
				} else if (files[x].getName().endsWith(JS)) {
					String name = StringUtil.replace(files[x].getName(), JS,
							EMPTY);
					scriptChoices.put(name, files[x].getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("fieldChoices: " + fieldChoices);
		ArrayList<String> inputList = new ArrayList<String>(
				htmlChoices.keySet());
		Collections.sort(inputList);
		updateCombobox(defs.htmlChoices, "listData",
				StringUtil.buildDelimitedString(inputList, ','));
	}

	/*
	 * Updates the specified argument for the Editor of a combo widget editor.
	 * 
	 * @param inputDef The InputDefinition that contains the combo widget
	 * 
	 * @param argumentName The name of the argument to modify (e.g. listData).
	 * 
	 * @param argumentData The new argument value (e.g. A,B,C).
	 */
	private static void updateCombobox(DynamicBuilderInputDefinition inputDef,
			String argumentName, String argumentData) {
		if (inputDef == null || argumentName == null || argumentData == null)
			return;
		@SuppressWarnings("unchecked")
		Map<String, String> m = inputDef.getArguments();
		String listData = m.get(argumentName); //$NON-NLS-1$
		if (listData == null)
			return;
		if (argumentData != null) {
			m.put(argumentName, argumentData);
			int cIndex = argumentData.indexOf(',');
			if (cIndex != -1)
				inputDef.setString(argumentData.substring(0, cIndex));
			else
				inputDef.setString(argumentData);
		}
	}

	@Override
	public boolean accept(File file, String name) {
		if (name.endsWith(HTML) || name.endsWith(CSS) || name.endsWith(JS))
			return true;
		return false;
	}
}
