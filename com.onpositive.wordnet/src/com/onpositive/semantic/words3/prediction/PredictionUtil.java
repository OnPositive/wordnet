package com.onpositive.semantic.words3.prediction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words3.MultiHashMap;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;

public class PredictionUtil {
	
	public static void rebuildPredictionVocab() {
		try {
			ReadOnlyMapWordNet wordNet = new ReadOnlyMapWordNet(new DataInputStream(new BufferedInputStream(new FileInputStream("D:/tmp/rwnet.dat")))); //XXX absolute path
			String[] grammarKeys = wordNet.getAllGrammarKeys();
			MultiHashMap<String, String> formsMap = new MultiHashMap<String, String>();
			for (String key : grammarKeys) {
				GrammarRelation[] forms = wordNet.getPossibleGrammarForms(key);
				for (GrammarRelation grammarRelation : forms) {
					String basicForm = grammarRelation.getWord().getBasicForm();
					formsMap.put(basicForm, key);
				}
			}
			Set<String> endings = new HashSet<String>();
			StringTrie<Byte> basesTrie = fillBasesTrie(formsMap, endings);
			StringTrie<Byte> endingsTrie = new StringToByteTrie();
			for (String ending : endings) {
				endingsTrie.store(ending, (byte) 1);
			}
			endingsTrie.commit();
			BufferedOutputStream stream = null;
			try {
				stream = new BufferedOutputStream(new FileOutputStream("D:/tmp/prediction.dat")); //XXX
				basesTrie.write(stream);
				endingsTrie.write(stream); 
			} finally {
				if (stream != null) stream.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void rebuildPredictionLists(ReadOnlyWordNet wordNet, List<String> bases, Set<String> endings) {
		String[] grammarKeys = wordNet.getAllGrammarKeys();
		MultiHashMap<String, String> formsMap = new MultiHashMap<String, String>();
		for (String key : grammarKeys) {
			GrammarRelation[] forms = wordNet.getPossibleGrammarForms(key);
			for (GrammarRelation grammarRelation : forms) {
				String basicForm = grammarRelation.getWord().getBasicForm();
				formsMap.put(basicForm, key);
			}
		}
		fillBases(formsMap, bases, endings);
	}
	
	private static StringTrie<Byte> fillBasesTrie(
			MultiHashMap<String, String> formsMap, Set<String> endings) {
		StringToByteTrie trie = new StringToByteTrie();
		StringToByteTrie.TrieBuilder builder = trie.newBuilder();
		Set<?> keys = formsMap.keySet();
		for (Object key: keys) {
			String word = key.toString().replace('ё','е');
			Collection<String> forms = formsMap.get(key);
			int endingStart = 0;
			outer:for (int i = 0; i < word.length(); i++) {
				char current = word.charAt(i);
				for (String string : forms) {
					if (string.length() <= i || string.charAt(i) != current) {
						endingStart = i;
						break outer;
					}
				}
			}
			if (endingStart < 3) {
				continue;
			}
			String base = word.substring(0, endingStart);
			builder.append(PredictionUtil.reverse(base), (byte) 1);
			for (String string : forms) {
				if (string.length() > endingStart) {
					String ending = string.substring(endingStart);
					ending = ending.replace('ё','е');
					endings.add(PredictionUtil.reverse(ending));
				}
			}
		}
		trie.commit(builder);
		return trie;
	}

	private static void fillBases(
			MultiHashMap<String, String> formsMap, List<String> bases, Set<String> endings) {
		Set<String> basesSet = new LinkedHashSet<String>();
		Set<?> keys = formsMap.keySet();
		for (Object key: keys) {
			String word = key.toString().replace('ё','е');
			Collection<String> forms = formsMap.get(key);
			int endingStart = 0;
			outer:for (int i = 0; i < word.length(); i++) {
				char current = word.charAt(i);
				for (String string : forms) {
					if (string.length() <= i || string.charAt(i) != current) {
						endingStart = i;
						break outer;
					}
				}
			}
			if (endingStart < 3) {
				continue;
			}
			String base = word.substring(0, endingStart);
			basesSet.add(reverse(base));
			
			for (String string : forms) {
				if (string.length() > endingStart) {
					String ending = string.substring(endingStart);
					ending = ending.replace('ё','е');
					endings.add(reverse(ending));
				}
			}
		}
		bases.addAll(basesSet);
		Collections.sort(bases);
	}
	
	public static String reverse(String base) {
		StringBuilder builder = new StringBuilder(base.length());
		for (int i = base.length() - 1; i >= 0; i--) {
			builder.append(base.charAt(i));
		}
		return builder.toString();
	}
	
}
