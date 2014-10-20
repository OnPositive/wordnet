package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words3.hds.StringTrie;

public class TrieGrammarStore extends StringTrie<GrammarRelation[]> {
	
	public TrieGrammarStore() {
		setEncoder(new GrammarRelationEncoder());
	}
	
}
