package com.onpositive.semantic.wordnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.onpositive.semantic.wordnet.edit.IWordNetEditInterface;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.SimpleWordNetEditInterface;
import com.onpositive.semantic.words2.WordNet;
import com.onpositive.semantic.words2.WordNetContributor;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.semantic.words3.ReadOnlyTrieWordNet;
import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.TrieZippedProvider;

public class WordNetProvider {

	public static final String ENGINE_CONFIG_DIR_PROP = "engineConfigDir";

	private static final String HASHMAP_FILE_NAME = "rwnet.dat";

	private static AbstractWordNet instance;

	public static String DEFAULT_INDEX_FOLDER = "D:/se1";

	static String[] builderNames = new String[] {
			"com.onpositive.semantic.words2.builder.WictionaryParser",
			"com.onpositive.semantic.words2.builder.RuCorpusParser" };
	
	
	public static IWordNetEditInterface editable(AbstractWordNet w){
		if (w instanceof SimpleWordNet){
			return new SimpleWordNetEditInterface((SimpleWordNet) w, getHashNetFile());
		}
		SimpleWordNet copy=new SimpleWordNet(w);
		return new SimpleWordNetEditInterface((SimpleWordNet) copy,getHashNetFile());
	}
	
	
	static File getHashNetFile(){
		String property = System.getProperty(ENGINE_CONFIG_DIR_PROP);
		if (property == null) {
			property = DEFAULT_INDEX_FOLDER;
		}
		File fl = new File(property);
		if (!fl.exists()){
			fl.mkdirs();
		}
		File readOnly = new File(fl,HASHMAP_FILE_NAME);
		return readOnly;
	}

	protected static WordNetContributor[] getContributors() {
		try {
			ArrayList<WordNetContributor> ss = new ArrayList<WordNetContributor>();
			for (String s : builderNames) {
				try {
					WordNetContributor c = (WordNetContributor) Class
							.forName(s).newInstance();
					ss.add(c);
				} catch (ClassNotFoundException e) {
					// TODO: handle exception
				}
			}
			return ss.toArray(new WordNetContributor[ss.size()]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new WordNetContributor[0];
	}

	public static AbstractWordNet getInstance() {
		if (instance == null) {

			String property = System.getProperty(ENGINE_CONFIG_DIR_PROP);
			if (property == null) {
				property = DEFAULT_INDEX_FOLDER;
			}
			File fl = new File(property);
			AbstractWordNet mapWordNet;
			mapWordNet = readHashMap(fl);
			if (mapWordNet != null) {
				return mapWordNet;
			}
			File ind = new File(fl, "wnet.dat");

			if (ind.exists()) {
				try {
					WordNet read = SimpleWordNet.read(ind.getAbsolutePath());
					if (read != null) {
						ReadOnlyWordNet readOnlyWordNet = new ReadOnlyMapWordNet(
								(SimpleWordNet) read);
						readOnlyWordNet.store(new File(fl, HASHMAP_FILE_NAME));
						instance = readOnlyWordNet;
					} else {
						System.err
								.println("Wordnet is corrupted rebuilding...");
					}
				} catch (FileNotFoundException e) {
					System.err.println("Wordnet is corrupted rebuilding...");
				} catch (IOException e) {
					System.err.println("Wordnet is corrupted rebuilding...");
				}
			}
			if (instance == null) {
				String loadFromZip = System.getProperty("buildDictionary");
				if (loadFromZip == null || !Boolean.parseBoolean(loadFromZip)) {
					ReadOnlyTrieWordNet instance2 = TrieZippedProvider
							.getInstance();
					if (instance2 != null) {
						instance = instance2;
						return instance;
					}
				}
				WordNetContributor[] contributors = getContributors();
				SimpleWordNet wnet = new SimpleWordNet();
				for (WordNetContributor c : contributors) {
					c.contribute(fl, wnet);
					System.out.println(wnet);
				}
				wnet.init();
				ReadOnlyWordNet readOnlyWordNet = new ReadOnlyMapWordNet(
						(SimpleWordNet) wnet);
				try {
					// wnet.write(ind.getAbsolutePath());
					readOnlyWordNet.store(new File(fl, HASHMAP_FILE_NAME));
				} catch (FileNotFoundException e) {
					System.err.println("Can not store read only wordnet...");
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Can not store read only wordnet...");
					e.printStackTrace();
				}
				instance = readOnlyWordNet;
				return instance;
			}
		}
		return instance;
	}

	private static AbstractWordNet readHashMap(File fl) {
		File readOnly = new File(fl,HASHMAP_FILE_NAME);
		if (readOnly.exists()) {
			try {
				instance = ReadOnlyMapWordNet.load(readOnly);
				return instance;
			} catch (Exception e) {
				System.err
						.println("Read only wordnet is corrupted rebuilding...");
			}
		}
		return null;
	}

	public static void killDatabase() {
		String property = System.getProperty(ENGINE_CONFIG_DIR_PROP);
		if (property == null) {
			property = DEFAULT_INDEX_FOLDER;
		}
		File fl = new File(property);
		File readOnly = new File(fl, HASHMAP_FILE_NAME);
		if (readOnly.exists()) {
			readOnly.delete();
		}
		File ind = new File(fl, "wnet.dat");
		if (ind.exists()) {
			ind.delete();
		}
	}

	public static void setInstance(AbstractWordNet simpleWordNet) {
		instance=simpleWordNet;
	}

}