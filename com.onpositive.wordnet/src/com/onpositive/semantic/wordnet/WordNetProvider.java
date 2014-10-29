package com.onpositive.semantic.wordnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.WordNet;
import com.onpositive.semantic.words2.WordNetContributor;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.semantic.words3.ReadOnlyTrieWordNet;
import com.onpositive.semantic.words3.ReadOnlyWordNet;


public class WordNetProvider {

	private static final String HASMAP_FILE_NAME = "rwnet.dat";
	private static final String ZIPPED_FILE_NAME = "russian.dict";

	private static AbstractWordNet instance;
	
	public static final String DEFAULT_INDEX_FOLDER = "";

	static String[] builderNames=new String[]{
		"com.onpositive.semantic.words2.builder.WictionaryParser",
		"com.onpositive.semantic.words2.builder.RuCorpusParser"
		};
	
	protected static WordNetContributor[] getContributors(){
		try {
			ArrayList<WordNetContributor>ss=new ArrayList<WordNetContributor>();
			for (String s:builderNames){
				try{
				WordNetContributor c=(WordNetContributor) Class.forName(s).newInstance();
				ss.add(c);
				}catch (ClassNotFoundException e) {
					// TODO: handle exception
				}
			}
			return ss.toArray(new WordNetContributor[ss.size()]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new WordNetContributor[0];
	}
	
	public static AbstractWordNet getInstance()
	{
		if (instance==null){
			
			String property = System.getProperty("engineConfigDir");
			if (property==null){
				property=DEFAULT_INDEX_FOLDER;
			}
			File fl=new File(property);
			AbstractWordNet mapWordNet;
			mapWordNet = readHashMap(fl);
			if (mapWordNet != null) {
				return mapWordNet;
			}
			File ind=new File(fl,"wnet.dat");
			
			if (ind.exists())
			{
				try {
					WordNet read = SimpleWordNet.read(ind.getAbsolutePath());
					if(read!=null){
					ReadOnlyWordNet readOnlyWordNet = new ReadOnlyMapWordNet((SimpleWordNet) read);
					readOnlyWordNet.store(new File(fl, HASMAP_FILE_NAME));
					instance=readOnlyWordNet;
					}
					else{
						System.err.println("Wordnet is corrupted rebuilding...");	
					}
				} catch (FileNotFoundException e) {
					System.err.println("Wordnet is corrupted rebuilding...");
				} catch (IOException e) {
					System.err.println("Wordnet is corrupted rebuilding...");
				}
			}
			if (instance==null){
				WordNetContributor[] contributors = getContributors();
				SimpleWordNet wnet=new SimpleWordNet();
				for (WordNetContributor c:contributors){
					c.contribute(fl, wnet);
					System.out.println(wnet);
				}
				wnet.init();
				ReadOnlyWordNet readOnlyWordNet = new ReadOnlyMapWordNet((SimpleWordNet) wnet);
				try {
					//wnet.write(ind.getAbsolutePath());
					readOnlyWordNet.store(new File(fl, HASMAP_FILE_NAME));
				} catch (FileNotFoundException e) {
					System.err.println("Can not store read only wordnet...");
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Can not store read only wordnet...");
					e.printStackTrace();
				}
				instance=readOnlyWordNet;
				return instance;				
			}
		}
		return instance;
	}

	private static AbstractWordNet readZipped(File fl) {
		File file=new File(ZIPPED_FILE_NAME);
		if (file.exists()){
			try{
				ZipFile zipFile = new ZipFile(file);
				instance=ReadOnlyTrieWordNet.load(zipFile);
				return instance;
			}catch (Exception e) {
				System.err.println("Read only wordnet is corrupted rebuilding...");
			}
		}

		return null;
	}

	private static AbstractWordNet readHashMap(File fl) {
		File readOnly=new File(HASMAP_FILE_NAME);
		if (readOnly.exists()){
			try{
				instance=ReadOnlyMapWordNet.load(readOnly);
				return instance;
			}catch (Exception e) {
				System.err.println("Read only wordnet is corrupted rebuilding...");
			}
		}
		return null;
	}

	public static void killDatabase() {
		String property = System.getProperty("engineConfigDir");
		if (property==null){
			property=DEFAULT_INDEX_FOLDER;
		}
		File fl=new File(property);
		File readOnly=new File(fl,HASMAP_FILE_NAME);
		if (readOnly.exists()){
			readOnly.delete();
		}
		File ind=new File(fl,"wnet.dat");
		if (ind.exists()){
			ind.delete();
		}
	}
	
	 /**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public void unZipIt(String zipFile, String outputFolder){
 
     byte[] buffer = new byte[1024];
 
     try{
 
    	//create output directory is not exists
    	File folder = new File(outputFolder);
    	if(!folder.exists()){
    		folder.mkdir();
    	}
 
    	//get the zip file content
    	ZipInputStream zis = 
    		new ZipInputStream(new FileInputStream(zipFile));
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();
 
    	while(ze!=null){
 
    	   String fileName = ze.getName();
           File newFile = new File(outputFolder + File.separator + fileName);
 
           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
 
            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
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
 
    }catch(IOException ex){
       ex.printStackTrace(); 
    }
   }    
}