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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.bowstreet.util.WrappedException;

public class ZipUtil {

	private static final int ZIP_BUFFER_SIZE = 2048;

	public static void remove(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				for (File child : children) {
					remove(child);
				}
			}
			file.delete();
		}
	}

	protected static void extractFile(ZipInputStream zip, File destination, String name) throws IOException {
		byte[] buffer = new byte[ZIP_BUFFER_SIZE];
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(destination, name)));
		int count = -1;
		while ((count = zip.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
		out.flush();
		out.close();
	}

	protected static void makeDirs(File destination, String path) {
		File directory = new File(destination, path);
		if (!directory.isDirectory()) {
			directory.mkdirs();
		}
	}

	protected static String getDirectoryPart(String name) {
		int s = name.lastIndexOf('/');
		return s == -1 ? null : name.substring(0, s);
	}

	public static void unzipFile(File zipFile, File destination) throws IOException {
		remove(destination);
		destination.mkdirs();
		try {
			ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
			try {
				ZipEntry entry;
				String name, dir;
				while ((entry = zip.getNextEntry()) != null) {
					name = entry.getName();
					if (entry.isDirectory()) {
						makeDirs(destination, name);
						continue;
					}
					dir = getDirectoryPart(name);
					if (dir != null) {
						makeDirs(destination, dir);
					}
					extractFile(zip, destination, name);
				}
			} finally {
				zip.close();
			}
		} catch (FileNotFoundException fnfe) {
			throw new WrappedException(fnfe, "Not a valid zip"); //$NON-NLS-1$
		}
	}

}
