package com.onpositive.semantic.words3.suggestions;

import com.onpositive.semantic.words3.hds.StringToByteTrie;
import com.onpositive.semantic.words3.hds.StringTrie;


public class PrefixRemover {

	public static void main(String[] args) {
		//String[] allGrammarKeys = instance.getAllGrammarKeys();
		StringToByteTrie trieGrammarStore = new StringToByteTrie();
		StringTrie<Byte>.TrieBuilder newBuilder = trieGrammarStore.newBuilder();
		newBuilder.append("aaaaa", Byte.valueOf((byte) 34));
		newBuilder.append("aaabb", Byte.valueOf((byte) 31));
		trieGrammarStore.commit(newBuilder);
		Byte find = trieGrammarStore.find("aaaaa");
		Byte find1 = trieGrammarStore.find("aaabb");
		System.out.println(find);
		System.out.println(find1);
	}

}
