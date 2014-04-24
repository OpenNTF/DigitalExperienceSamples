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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bowstreet.BSConfig;
import com.bowstreet.editor.uitools.coordinator.WebAppBaseCoordinator;
import com.bowstreet.generation.DynamicBuilderInputDefinition;
import com.bowstreet.util.IXml;
import com.bowstreet.util.StringUtil;
import com.ibm.wef.samples.builders.ScriptApplicationBuilder.Constants;
import com.ibm.wef.samples.builders.ScriptApplicationBuilder.SharedConstants;

/**
 * Coordinator implementation
 */
public class ScriptApplicationCoordinator extends WebAppBaseCoordinator implements FilenameFilter {

	InputDefinitions defs = new InputDefinitions();

	/**
	 * The initialization method is called each time the builder call is opened.
	 * Here you can set defaults, create dynamic pick lists, show/hide/create
	 * inputs.
	 */
	public String initializeInputs(boolean isNewBuilderCall) {
		/* ##GENERATED_BODY_BEGIN#CoordinatorDefInitCode# */
		// Generated code to initialize all the input definitions
		defs.name = context.findInputDefinition(Constants.Name);
		defs.libraries = context.findInputDefinition(Constants.Libraries);
		defs.htmlFile = context.findInputDefinition(Constants.HtmlFile);
		defs.scriptFile = context.findInputDefinition(Constants.ScriptFile);
		defs.cssFile = context.findInputDefinition(Constants.CssFile);
		defs.addServiceProviderSupport = context.findInputDefinition(Constants.AddServiceProviderSupport);
		defs.serviceProvider = context.findInputDefinition(Constants.ServiceProvider);
		defs.serviceVarName = context.findInputDefinition(Constants.ServiceVarName);
		/* ##GENERATED_BODY_END */

		// All the builder input definitions are now available using the
		// initialized "defs" structure like this:
		// DynamicBuilderInputDefinition myDef = defs.myInput;

		initPickers();

		setVisibility();
		
		return null;
	}

	/*
	 * Set visibility
	 */
	private void setVisibility() {
		boolean addProviderSupport = defs.addServiceProviderSupport.getBoolean();
		defs.serviceVarName.setVisible(addProviderSupport);
		defs.serviceProvider.setVisible(addProviderSupport);
		
	}

	/**
	 * This is called whenever any input is changed. NOTE: This method must
	 * return "true" to have UI updated.
	 */
	public boolean processInputChange(DynamicBuilderInputDefinition changed) {

		if (changed == defs.addServiceProviderSupport) {
			setVisibility();
			return true;
		}

		// don't update UI by default
		return false;
	}

	/*
	 * Initialize the pick list of libraries, by getting a list of HTML files in
	 * a particular folder.
	 */
	private void initPickers() {

		List<String> libChoices = new ArrayList<String>();

		// Build list from the files in /samples/script_builder/libraries/
		try {
			File dir = new File(BSConfig.getHtmlRootDir() + SharedConstants.SCRIPT_BUILDER_LIBRARIES_FOLDER);

			// The accept() method is used to filter the files returned from
			// this method call.
			File[] files = dir.listFiles(this);

			for (int x = 0; x < files.length; x++) {
				String libName = StringUtil.replace(files[x].getName(), ".html", "");

				libChoices.add(libName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("fieldChoices: " + fieldChoices);
		updateCellEditor(defs.libraries, 0, "ListData", StringUtil.buildDelimitedString(libChoices, ','));
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

	/*
	 * Updates the specified argument for the cellEditors of a list widget
	 * editor.
	 * 
	 * @param inputDef The InputDefinition that contains the list widget
	 * 
	 * @param index The index if the editor to modify.
	 * 
	 * @param argumentName The name of the argument to modify (e.g. listData).
	 * 
	 * @param argumentData The new argument value (e.g. A,B,C).
	 */
	private static void updateCellEditor(DynamicBuilderInputDefinition inputDef, int index, String argumentName,
			String argumentData) {
		if (inputDef == null || argumentName == null || argumentData == null)
			return;
		@SuppressWarnings("unchecked")
		Map<String, IXml> m = inputDef.getArguments();
		IXml editors = m.get("cellEditors"); //$NON-NLS-1$
		if (editors != null) {
			editors = editors.cloneElement();

			IXml editor = (IXml) editors.getChildren().get(index);

			IXml listData = editor.findElement(StringUtil.strcat("//Argument[@name=", argumentName, "]")); //$NON-NLS-1$ //$NON-NLS-2$
			if (listData == null)
				return;

			listData.setText(argumentData);
			inputDef.setArgument("cellEditors", editors); //$NON-NLS-1$
		}
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
		DynamicBuilderInputDefinition htmlFile;
		DynamicBuilderInputDefinition scriptFile;
		DynamicBuilderInputDefinition cssFile;
		DynamicBuilderInputDefinition addServiceProviderSupport;
		DynamicBuilderInputDefinition serviceProvider;
		DynamicBuilderInputDefinition serviceVarName;
		/* ##GENERATED_END */

	}

}
