/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.wef.samples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bowstreet.util.StringUtil;

/*
 * Gets lists of files in an app
 */
public class AppFolderInfo {

	private static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator"); //$NON-NLS-1$
	public static final String FILES_ELEMENT_NAME = "files"; //$NON-NLS-1$
	public static final String FILE_ELEMENT_NAME = "file"; //$NON-NLS-1$
	public static final String PATH_ELEMENT_NAME = "path"; //$NON-NLS-1$
	public static final String MAIN_ELEMENT_NAME = "main"; //$NON-NLS-1$

	private static final Set<String> IMAGE_EXTENSIONS = new HashSet<String>();
	static {
		IMAGE_EXTENSIONS.add("jpg"); //$NON-NLS-1$
		IMAGE_EXTENSIONS.add("jpeg"); //$NON-NLS-1$
		IMAGE_EXTENSIONS.add("gif"); //$NON-NLS-1$
		IMAGE_EXTENSIONS.add("png"); //$NON-NLS-1$
		IMAGE_EXTENSIONS.add("bmp"); //$NON-NLS-1$
	}

	private String rootDirectory;
	private String filePath;
	private List<String> htmlFiles = new ArrayList<String>();
	private List<String> jsFiles = new ArrayList<String>();
	private List<String> cssFiles = new ArrayList<String>();
	private List<String> imageFiles = new ArrayList<String>();
	private List<String> otherFiles = new ArrayList<String>();
	private List<String> allFiles = new ArrayList<String>();

	public AppFolderInfo(String rootDirectory) {

		this.rootDirectory = rootDirectory;
		filePath = "."; //$NON-NLS-1$
		getFilesFromFolder(filePath);
	}

	private void getFilesFromFolder(String currentFolder) {
		try {
			File dir = new File(rootDirectory + "/" + currentFolder); //$NON-NLS-1$

			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				String shortName = files[i].getName();
				String name = currentFolder + "/" + shortName; //$NON-NLS-1$
				if (files[i].isDirectory()) {
					getFilesFromFolder(name);
				}
				String relativeName = StringUtil.replace(name,
						filePath + "/", ""); //$NON-NLS-1$ //$NON-NLS-2$
				String ext = StringUtil.substringAfterLast(name, '.');
				if (ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")) { //$NON-NLS-1$ //$NON-NLS-2$
					htmlFiles.add(relativeName);
				} else if (ext.equalsIgnoreCase("js")) { //$NON-NLS-1$
					jsFiles.add(relativeName);
				} else if (ext.equalsIgnoreCase("css")) { //$NON-NLS-1$
					cssFiles.add(relativeName);
				} else if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
					imageFiles.add(relativeName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File getFileFromRelativePath(String relativePath) {
		String fullPath = rootDirectory + "/" + filePath + "/" + relativePath; //$NON-NLS-1$ //$NON-NLS-2$
		File file = new File(fullPath);
		return file;
	}

	/*
	 * Gets the contents of a file as a String relativePath is relative to the
	 * import folder root
	 */
	public String getFileAsString(String relativePath) throws IOException {
		File file = getFileFromRelativePath(relativePath);
		/*
		 * Gets the contents of a file as a String relativePath is relative to
		 * the import folder root
		 */
		return getFileAsString(file);
	}

	public String getFileAsString(File file) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		FileInputStream fis = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis,
				ENCODING));
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(LINE_SEPARATOR);
			}
		} finally {
			reader.close();
		}
		return stringBuilder.toString();
	}

	public List<String> getHtmlFiles() {
		return htmlFiles;
	}

	public List<String> getJsFiles() {
		return jsFiles;
	}

	public List<String> getCssFiles() {
		return cssFiles;
	}

	public List<String> getImageFiles() {
		return imageFiles;
	}

	public List<String> getOtherFiles() {
		return otherFiles;
	}

	public List<String> getAllFiles() {
		if (allFiles.size() == 0) {
			allFiles.addAll(htmlFiles);
			allFiles.addAll(jsFiles);
			allFiles.addAll(cssFiles);
			allFiles.addAll(imageFiles);
			allFiles.addAll(otherFiles);
		}
		return allFiles;
	}

	public void updateFile(String relativePath, String curText) {
		File file = getFileFromRelativePath(relativePath);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			writer.write(curText);

		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}
	}

	public String findFile(String inName) {
		String rFileName = null;
		Iterator<String> files = getAllFiles().iterator();
		while (files.hasNext()) {
			String fName = (String) files.next();
			if (fName.endsWith(inName)) {
				rFileName = fName;
				break;
			}
		}
		return rFileName;
	}

}
