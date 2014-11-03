package com.onpositive.semantic.words3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class TrieZippedProvider {
	
	private static final String TRIE_FILE_NAME = "russian.dat";

	private static final String ZIPPED_FILE_NAME = "russian.zip";

	private static ReadOnlyTrieWordNet instance;
	
	public static synchronized ReadOnlyTrieWordNet getInstance() {
		if (instance == null) {
			File folder = getTempFolder();
			File trieFile = new File(folder,TRIE_FILE_NAME);
			if (trieFile.exists() ) {
				instance = doRead(trieFile);
			} else {
				File archiveFile = getDataZipFile();
				if (archiveFile.exists()) {
					unZip(archiveFile, folder);
					instance = doRead(trieFile);					
				}
			}
		}
		return instance;
	}

	protected static ReadOnlyTrieWordNet doRead(File trieFile) {
		try {
			InputStream trieStream = new BufferedInputStream(new FileInputStream(trieFile));
			return new ReadOnlyTrieWordNet(new DataInputStream(trieStream));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static File getDataZipFile() {
		return new File(ZIPPED_FILE_NAME);
	}

	private static File getTempFolder() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		return new File(tmpDir, "GrammarData");
	}
	
	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public static void unZip(String zipFile, String outputFolder) {
		unZip(new File(zipFile), new File(outputFolder));

	}

	public static void unZip(File zipFile, File outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator
						+ fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
