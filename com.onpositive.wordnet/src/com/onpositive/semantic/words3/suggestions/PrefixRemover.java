package com.onpositive.semantic.words3.suggestions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.onpositive.semantic.words3.ReadOnlyWordNet;
import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;


public class PrefixRemover {

	private static final int COUNT = 2000000;

	public static void main(String[] args) {
//		test3();
		globalTest();
//		test1();
//		test4();
	}

	private static void globalTest() {
		ReadOnlyWordNet loaded;
		try {
			loaded = ReadOnlyWordNet.load("rwnet.dat");
			String[] keys = loaded.getAllGrammarKeys();
			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			StringToByteTrie trieGrammarStore = new StringToByteTrie();
			StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			for (int i = 0; i < keys.length; i++) {
				Byte b = (byte)(Math.random() * 100);
				dataMap.put(keys[i], b);
				newBuilder.append(keys[i], b);
			}
			trieGrammarStore.commit(newBuilder);
			for (int i = 0; i < keys.length; i++) {
				Byte found = trieGrammarStore.find(keys[i]);
				Byte data = dataMap.get(keys[i]);
				if (!data.equals(found)) {
					System.out.println("Error for str " + keys[i] + " idx = " + i);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void test4() {
		int lowBound = 31667;
		int highBound = COUNT;
		int start = lowBound;
		ReadOnlyWordNet loaded;
		String[] keys;
		try {
			loaded = ReadOnlyWordNet.load("rwnet.dat");
			keys = loaded.getAllGrammarKeys();
			int step = (highBound - lowBound) / 2;
			while (step > 2) {
				if (broken(keys, lowBound, highBound)) {
					lowBound += step;
				} else {
					lowBound -= step;
				}
				step /= 2;
			}
			while (!broken(keys, lowBound, highBound)) {
				lowBound--;
			}
			step = (highBound - lowBound) / 2;
			while (step > 2) {
				if (broken(keys, lowBound, highBound)) {
					highBound -= step;
				} else {
					highBound += step;
				}
				step /= 2;
			}
			while (!broken(keys, lowBound, highBound)) {
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
			loaded = ReadOnlyWordNet.load("rwnet.dat");
			String[] keys = loaded.getAllGrammarKeys();
			int start = 57778;
			int end = 90198;
			ArrayList<Integer> included = new ArrayList<Integer>();
			for (int i = start; i < end; i++) {
				included.add(i);
			}
			for (int i = 0; i < included.size(); i++) {
				Integer removed = included.remove(i);
				if (!broken(keys, included)) {
					included.add(i,removed);
				} else {
					i--;
				}
			}
			
			boolean a = broken(keys, included);
			
			for (int i = 0; i < included.size(); i++) {
				Integer removed = included.remove(i);
				if (!broken(keys, included)) {
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
	
	private static boolean broken(String[] keys, int start, int end) {
		Map<String, Byte> dataMap = new HashMap<String, Byte>();
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		for (int i = start; i < end; i++) {
			Byte b = (byte)(Math.random() * 100);
			dataMap.put(keys[i], b);
			newBuilder.append(keys[i], b);
		}
		trieGrammarStore.commit(newBuilder);
		for (int i = start; i < end; i++) {
			Byte found = trieGrammarStore.find(keys[i]);
			Byte data = dataMap.get(keys[i]);
			if (!data.equals(found)) {
				return true;
			}
			
		}
		return false;
	}
	
	private static boolean broken(String[] keys, ArrayList<Integer> included) {
		try {
			Map<String, Byte> dataMap = new HashMap<String, Byte>();
			StringToByteTrie trieGrammarStore = new StringToByteTrie();
			StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				Byte b = (byte)(Math.random() * 100);
				dataMap.put(keys[index], b);
				newBuilder.append(keys[index], b);
			}
			trieGrammarStore.commit(newBuilder);
			for (int i = 0; i < included.size(); i++) {
				int index = included.get(i);
				Byte found = trieGrammarStore.find(keys[index]);
				Byte data = dataMap.get(keys[index]);
				if (!data.equals(found)) {
					return true;
				}
				
			}
		} catch (ArrayIndexOutOfBoundsException e) {
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
			Byte find = trieGrammarStore.find(string);
			System.out.println(string + " = " + find);
		}
	}

}
