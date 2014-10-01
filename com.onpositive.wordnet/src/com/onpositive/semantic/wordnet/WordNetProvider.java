package com.onpositive.semantic.wordnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.WordNet;
import com.onpositive.semantic.words2.WordNetContributor;
import com.onpositive.semantic.words3.ReadOnlyWordNet;


public class WordNetProvider {

	private static AbstractWordNet instance;
	
	public static final String DEFAULT_INDEX_FOLDER = "D:/se1";

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
			File readOnly=new File(fl,"rwnet.dat");
			if (readOnly.exists()){
				try{
				instance=ReadOnlyWordNet.load(readOnly);
				return instance;
				}catch (Exception e) {
					System.err.println("Read only wordnet is corrupted rebuilding...");
				}
			}
			File ind=new File(fl,"wnet.dat");
			
			if (ind.exists())
			{
				try {
					WordNet read = SimpleWordNet.read(ind.getAbsolutePath());
					if(read!=null){
					ReadOnlyWordNet readOnlyWordNet = new ReadOnlyWordNet((SimpleWordNet) read);
					readOnlyWordNet.store(readOnly);
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
				ReadOnlyWordNet readOnlyWordNet = new ReadOnlyWordNet((SimpleWordNet) wnet);
				try {
					//wnet.write(ind.getAbsolutePath());
					readOnlyWordNet.store(readOnly);
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

	public static void killDatabase() {
		String property = System.getProperty("engineConfigDir");
		if (property==null){
			property=DEFAULT_INDEX_FOLDER;
		}
		File fl=new File(property);
		File readOnly=new File(fl,"rwnet.dat");
		if (readOnly.exists()){
			readOnly.delete();
		}
		File ind=new File(fl,"wnet.dat");
		if (ind.exists()){
			ind.delete();
		}
	}
}