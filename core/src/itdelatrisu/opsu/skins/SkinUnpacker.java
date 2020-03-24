/*
 * opsu! - an open-source osu! client
 * Copyright (C) 2014-2017 Jeffrey Han
 *
 * opsu! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!.  If not, see <http://www.gnu.org/licenses/>.
 */

package itdelatrisu.opsu.skins;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import fluddokt.opsu.fake.File;
import itdelatrisu.opsu.Utils;
import itdelatrisu.opsu.ui.UI;

//import java.io.File;

/**
 * Unpacker for OSK (ZIP) archives.
 */
public class SkinUnpacker {
	/** The index of the current file being unpacked. */
	private static int fileIndex = -1;

	/** The total number of files to unpack. */
	private static File[] files;

	// This class should not be instantiated.
	private SkinUnpacker() {}

	private static final String fileEnding=".osk";
	/**
	 * Invokes the unpacker for each OSK archive in a root directory.
	 * @param root the root directory
	 * @param dest the destination directory
	 * @return an array containing the new (unpacked) directories
	 */
	public static File[] unpackAllFiles(File root, File dest) {
		List<File> dirs = new ArrayList<File>();
		// find all OSK files
		files = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(java.io.File dir, String name) {
//				return name.toLowerCase().endsWith(".osk");
				if(name.length()<fileEnding.length())
					return false;
				else
					for(int i=1;i<=fileEnding.length();i++){
						if(Character.toLowerCase(name.charAt(name.length()-i))!=fileEnding.charAt(fileEnding.length()-i))
							return false;
					}
				return true;
			}
		});
		if (files == null || files.length < 1) {
			files = null;
			return new File[0];
		}
		// unpack OSKs
		for (File file : files) {
			fileIndex++;
			String dirName = file.getName().substring(0, file.getName().lastIndexOf('.'));
			File skinDir = new File(dest, dirName);
			if (!skinDir.isDirectory()) {
				skinDir.mkdir();
				Utils.unzip(file, skinDir);
				if(skinDir.listFiles().length==1){
					deleteDirectory(skinDir);
					Utils.unzip(file, dest);
				}

				file.delete();  // delete the OSK when finished
				dirs.add(skinDir);
			}
		}

		fileIndex = -1;
		files = null;

		if (!dirs.isEmpty()) {
			String text = String.format("Imported %d new skin%s.", dirs.size(), dirs.size() == 1 ? "" : "s");
			UI.getNotificationManager().sendNotification(text);
		}

		return dirs.toArray(new File[dirs.size()]);
	}
	public static void deleteDirectory(File file) {
		if( file.exists() ) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
			file.delete();
		}
	}
	/**
	 * Returns the name of the current file being unpacked, or null if none.
	 */
	public static String getCurrentFileName() {
		if (files == null || fileIndex == -1)
			return null;

		return files[fileIndex].getName();
	}

	/**
	 * Returns the progress of file unpacking, or -1 if not unpacking.
	 * @return the completion percent [0, 100] or -1
	 */
	public static int getUnpackerProgress() {
		if (files == null || fileIndex == -1)
			return -1;

		return (fileIndex + 1) * 100 / files.length;
	}
}
