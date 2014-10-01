package com.onpositive.wordnet.tests;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;

public class PrefixTest extends TestCase{
	
	public void test01() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		newBuilder.append("aa", Byte.valueOf((byte) 34));
		trieGrammarStore.commit(newBuilder);
		Byte found = trieGrammarStore.find("aa");
		assertEquals(34, found.byteValue());
	}
	
	public void test02() {
		findTest(MapBuilder.create().entryOf("aa",(byte) 34).entryOf("ba",(byte) 31).get());
	}
	
	public void test03() {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		newBuilder.append("aa", Byte.valueOf((byte) 34));
		newBuilder.append("ab", Byte.valueOf((byte) 31));
		trieGrammarStore.commit(newBuilder);
		Byte found = trieGrammarStore.find("aa");
		assertEquals(34, found.byteValue());
		found = trieGrammarStore.find("ab");
		assertEquals(31, found.byteValue());
		found = trieGrammarStore.find("a");
		assertNull(found);
	}
	
	public void test04() {
		findTest(MapBuilder.create().entryOf("aaabb",(byte) 7).entryOf("aaaaa",(byte) 49).get());
	}
	
	private void findTest(Map<String, Byte> data) {
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		for (String key : data.keySet()) {
			newBuilder.append(key, data.get(key));
		}
		trieGrammarStore.commit(newBuilder);
		for (String key : data.keySet()) {
			Byte found = trieGrammarStore.find(key);
			assertEquals(((Byte)data.get(key)), Byte.valueOf(found.byteValue()));
		}
	}
	
	private static class MapBuilder {
		
		private Map<String, Byte> map = new HashMap<String, Byte>();
		
		private MapBuilder() {
			
		}
		
		public static MapBuilder create() {
			return new MapBuilder();
		}
		
		public MapBuilder entryOf(String key, Byte value) {
			map.put(key, value);
			return this;
		}
		
		public Map<String, Byte> get() {
			return map;
		}
		
	}
	
}
