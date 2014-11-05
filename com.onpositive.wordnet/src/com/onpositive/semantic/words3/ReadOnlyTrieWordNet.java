package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.IOException;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.hds.StringStorage;
import com.onpositive.semantic.words3.hds.StringTrie;

public class ReadOnlyTrieWordNet extends ReadOnlyWordNet {
	
	public ReadOnlyTrieWordNet(DataInputStream is) throws IOException {
		super(is);
	}
	
	public ReadOnlyTrieWordNet(SimpleWordNet original) {
		super(original);
	}

	@Override
	protected void initRelations(SimpleWordNet original) {
		super.initRelations(original);
		((StringTrie<GrammarRelation[]>) relations).commit();
	}

	@Override
	protected StringStorage<GrammarRelation[]> createStorage(int size) {
		return new TrieGrammarStore();
	}
	
}
