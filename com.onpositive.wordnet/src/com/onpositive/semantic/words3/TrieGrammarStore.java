package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words3.hds.StringTrie;

public class TrieGrammarStore extends StringTrie<GrammarRelation[]>{
	
	@Override
	protected byte[] encodeValue(GrammarRelation[] associatedData2) {
		return ReadOnlyWordNet.encodeRelations((AbstractRelation<?>[]) associatedData2);
	}

	@Override
	protected GrammarRelation[] decodeValue(int i) {
		return ReadOnlyWordNet.decodeGrammarRelations(i, byteBuffer);
	}

	@Override
	protected int getDataSize(int i) {
		return ReadOnlyWordNet.estimateRelationsLength(i, byteBuffer);
	}
}
