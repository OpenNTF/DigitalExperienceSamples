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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.bowstreet.BSConfig;
import com.bowstreet.util.StringUtil;

public class TemplateUtil {

	public void fixTemplating(AppFolderInfo appInfo, String sTemplateIn, String sTemplateOut, String templateScript, String templateScriptFile ) {
		
		String[] templateIn = sTemplateIn.split("\\s*,\\s*");
		String[] templateOut = sTemplateOut.split("\\s*,\\s*");
		// Get HTML and find BODY tag
		List<String> htmlFiles = appInfo.getHtmlFiles();
		for (Iterator<String> iterator = htmlFiles.iterator(); iterator
				.hasNext();) {
			String fName = iterator.next();

			String mainHtml;
			try {
				mainHtml = appInfo.getFileAsString(fName);
				if(!StringUtil.isEmpty(mainHtml)){
					String html = updateTemplatesInHtml(
								mainHtml,
								  appInfo, templateIn, templateOut); 
					appInfo.updateFile(fName, html.toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		try {
				String templateFile = appInfo.findFile(templateScript);
				if(!StringUtil.isEmpty(templateFile)){
					String sourceFile = BSConfig.getHtmlRootDir()	+ templateScriptFile;
					File jsFile = new File(sourceFile);
					if(jsFile.exists()){
						String js = appInfo.getFileAsString(jsFile);
						if(!StringUtil.isEmpty(js))
							appInfo.updateFile(templateFile, js);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private String updateTemplatesInHtml(String  inHtml, 
			 AppFolderInfo appInfo, String[] templateIn,
			String[] templateOut) throws IOException {
		String rHtml = inHtml;
		boolean changed = false;
		String lowerHtml = inHtml.toLowerCase();
		int bodyStart = lowerHtml.indexOf("<body>");
		int bodyEnd = lowerHtml.indexOf("</body>");
		if(bodyEnd != -1 && bodyStart != -1){
			String content = inHtml.substring(bodyStart, bodyEnd);
			for (int i = 0; i < templateIn.length; i++) {
					String newContent = content.replaceAll(templateIn[i],
							templateOut[i]);
					if (!newContent.equals(content)) {
						changed = true;
						content = newContent;
					}
			}
			if (changed) {
				rHtml = inHtml.substring(0,bodyStart) + content + inHtml.substring(bodyEnd);
			}
		}
	return rHtml;
	}
}
