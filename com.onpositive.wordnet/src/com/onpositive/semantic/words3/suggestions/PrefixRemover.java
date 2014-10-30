package com.onpositive.semantic.words3.suggestions;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.TrieZippedProvider;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.semantic.words3.ReadOnlyTrieWordNet;
import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.TrieGrammarStore;
import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;
import com.onpositive.semantic.words3.hds.StringTrie.TrieBuilder;


public class PrefixRemover {

	private static final int COUNT = 2000000;

	public static void main(String[] args) {
//		test1();
//		test3();
//		test4();
//		test5();
//		test6();
//		test7();
		test8();
		
//		globalTest();
//		timeTest();
	}

	@SuppressWarnings("unchecked")
	private static void globalTest() {
		ReadOnlyWordNet loaded;
		try {
			loaded = ReadOnlyMapWordNet.load("rwnet.dat");
//			ReadOnlyTrieWordNet trieWordNet = ReadOnlyTrieWordNet.load(new ZipFile("russian.dict"));
			String[] keys = loaded.getAllGrammarKeys();
//			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			int n = keys.length;
			for (int i = 0; i < n; i++) {
//				Byte b = (byte)(Math.random() * 100);
//				dataMap.put(keys[i], b);
				newBuilder.append(keys[i], loaded.getPossibleGrammarForms(keys[i]));
			}
			trieGrammarStore.commit(newBuilder);
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("dict.dict"));
			ZipEntry entry = new ZipEntry("trie");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			trieGrammarStore.write(baos);
			byte[] input = baos.toByteArray();
			entry.setSize(input.length);
			zos.putNextEntry(entry);
			zos.write(input);
			zos.closeEntry();
			zos.close();
			
			for (int i = 0; i < n; i++) {
				GrammarRelation[] found = trieGrammarStore.get(keys[i]);
				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[i])) ) {
					System.out.println("Error for str " + keys[i] + " idx = " + i);
				}
				found = trieGrammarStore.get(keys[i] + "aaa");
				if (found != null) {
					System.out.println("Non-null for str " + keys[i] + "aaa" + " idx = " + i);
				}
				if (keys[i].length() > 2) {
					String str = keys[i].substring(0,2) + "a" + keys[i].substring(2);
					found = trieGrammarStore.get(str);
					if (found != null) {
						System.out.println("Non-null for str " + str + " idx = " + i);
					}
				}
//				Byte data = dataMap.get(keys[i]);
//				if (!data.equals(found)) {
//				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void test4() {
		int lowBound = 31667;
		int highBound = 1000000;
		int start = lowBound;
		ReadOnlyWordNet loaded;
		String[] keys;
		try {
			loaded = ReadOnlyMapWordNet.load("rwnet.dat");
			keys = loaded.getAllGrammarKeys();
			int step = (highBound - lowBound) / 2;
			while (step > 2) {
				if (broken(keys, lowBound, highBound, loaded)) {
					lowBound += step;
				} else {
					lowBound -= step;
				}
				step /= 2;
			}
			while (!broken(keys, lowBound, highBound, loaded)) {
				lowBound--;
			}
			step = (highBound - lowBound) / 2;
			while (step > 2) {
				if (broken(keys, lowBound, highBound, loaded)) {
					highBound -= step;
				} else {
					highBound += step;
				}
				step /= 2;
			}
			while (!broken(keys, lowBound, highBound, loaded)) {
				highBound++;
			}

			System.out.println(String.format("[ %d, %d ]", lowBound, highBound));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private static void test3() {
		ReadOnlyWordNet loaded;
		try {
			loaded = ReadOnlyMapWordNet.load("rwnet.dat");
			String[] keys = loaded.getAllGrammarKeys();
			int start = 809063;
			int end = 809078;
			ArrayList<Integer> included = new ArrayList<Integer>();
			for (int i = start; i < end; i++) {
				included.add(i);
			}
			for (int i = 0; i < included.size(); i++) {
				Integer removed = included.remove(i);
				if (!broken(keys, included, loaded)) {
					included.add(i,removed);
				} else {
					i--;
				}
			}
			
			boolean a = broken(keys, included, loaded);
			System.out.println("test3() is broken: " + a);
			
			for (int i = 0; i < included.size(); i++) {
				Integer removed = included.remove(i);
				if (!broken(keys, included, loaded)) {
					included.add(i,removed);
				} else {
					i--;
				}
			}
			
//			while (broken(keys, start, COUNT)) {
//				start++;
//			}
			for (Integer integer : included) {
				System.out.println(keys[integer]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static boolean broken(String[] keys, int start, int end, ReadOnlyWordNet loaded) {
//		Map<String, Byte> dataMap = new HashMap<String, Byte>();
		StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
		TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		for (int i = start; i < end; i++) {
			Byte b = (byte)(Math.random() * 100);
//			dataMap.put(keys[i], b);
			newBuilder.append(keys[i], loaded.getPossibleGrammarForms(keys[i]));
		}
		trieGrammarStore.commit(newBuilder);
		try {
			for (int i = start; i < end; i++) {
				GrammarRelation[] found = trieGrammarStore.get(keys[i]);
				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[i]))) {
					return true;
				}
				found = trieGrammarStore.get(keys[i] + "aaa");
				if (found != null) {
					return true;
				}
			}
		}
		catch (Throwable e) {
			return true;
		}
		return false;
	}
	
	private static boolean broken(String[] keys, ArrayList<Integer> included, ReadOnlyWordNet loaded) {
		try {
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				newBuilder.append(keys[index], loaded.getPossibleGrammarForms(keys[index]));
			}
			trieGrammarStore.commit(newBuilder);
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				GrammarRelation[] found = trieGrammarStore.get(keys[index]);
				if (!Arrays.equals(found, loaded.getPossibleGrammarForms(keys[index]))) {
					return true;
				}
				found = trieGrammarStore.get(keys[index] + "aaa");
				if (found != null) {
					return true;
				}
			}
		} catch (Throwable e) {
			return true;
		}
		return false;
	}

	private static void test1() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
		String[] tst = {"надгрызает",
						"надгрызала",
						"надгрызали",
						"надгрызало",
						"надгрызать",
						"надгрызают",
						"надгрызена",
						"надгрызено",
						"надгрызены",
						"надгрызёте",
						"надгрызёшь",
						"надгрызите",
						"надгрызаетесь",
						"надгрызшая",
						"надгрызшее",
						"надгрызшей",
						"надгрызшем",
						"надгрызшею",
						"надгрызшие",
						"надгрызший",
						"надгрызшим",
						"надгрызших",
						"надгрызшую",
						"надгрыз"};
		
		for (String string : tst) {
			newBuilder.append(string, Byte.valueOf((byte)i++));
		}
		
		trieGrammarStore.commit(newBuilder);
        
		for (String string : tst) {
			Byte find = trieGrammarStore.get(string);
			System.out.println(string + " = " + find);
		}
	}
	
	private static void test5() {
		StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
		TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
//		String[] tst = {"с",
//						"станичного"};
		String[] tst = {"{{-}}"};

		
		try {
			ReadOnlyWordNet loaded = ReadOnlyMapWordNet.load("rwnet.dat");
			GrammarRelation[] forms = loaded.getPossibleGrammarForms(tst[0]);
			forms = loaded.getPossibleGrammarForms(tst[0]);
			for (String string : tst) {
				newBuilder.append(string, forms);
			}
			
			trieGrammarStore.commit(newBuilder);
			
			for (String string : tst) {
				forms = loaded.getPossibleGrammarForms(string);
				GrammarRelation[] found = trieGrammarStore.get(string);
				System.out.println(string + " = " + found);
				found = trieGrammarStore.get(string + "aaa");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		
	}
	
	private static void test6() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		int i = 35;
		
		String[] tst = {"{{-}}"};
		
		for (String string : tst) {
			newBuilder.append(string, Byte.valueOf((byte)i++));
		}
		
		trieGrammarStore.commit(newBuilder);
        
		Byte fond = trieGrammarStore.get("{{-}}aaa");
		System.out.println("PrefixRemover.test6() " + fond);
	}
	
	private static void test7() {
		try {
			ReadOnlyTrieWordNet.load(new ZipFile("russian.dict"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void test8() {
		TrieZippedProvider.getInstance();		
	}
	
	private static void timeTest() {
		ReadOnlyWordNet loaded;
		try {
			loaded = ReadOnlyMapWordNet.load("rwnet.dat");
			String[] keys = loaded.getAllGrammarKeys();
//			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			List<String> testList = new ArrayList<String>();
			
			for (int i = 0; i < 500000; i++) {
				testList.add(keys[(int) (Math.random() * (keys.length - 1))]);
			}
			
			
			StringTrie<GrammarRelation[]> trieGrammarStore = new TrieGrammarStore();
			TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			Map<String, GrammarRelation[]> relationsMap = new HashMap<String, GrammarRelation[]>();
			int n = testList.size();
			for (int i = 0; i < n; i++) {
//				Byte b = (byte)(Math.random() * 100);
//				dataMap.put(keys[i], b);
				String word = testList.get(i);
				GrammarRelation[] forms = loaded.getPossibleGrammarForms(word);
				relationsMap.put(word, forms);
				newBuilder.append(word, forms);
			}
			trieGrammarStore.commit(newBuilder);
			long n1 = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				GrammarRelation[] found = trieGrammarStore.get(testList.get(i));
			}
			System.out.println("Trie: " + (System.currentTimeMillis() - n1));
			
			long n2 = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				GrammarRelation[] found = relationsMap.get(testList.get(i));
			}
			System.out.println("HashMap: " + (System.currentTimeMillis() - n2));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
