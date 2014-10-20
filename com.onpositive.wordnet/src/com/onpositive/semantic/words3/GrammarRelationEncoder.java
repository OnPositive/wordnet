package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.GrammarRelation;

public class GrammarRelationEncoder implements IEncoder<GrammarRelation[]> {

	@Override
	public byte[] encodeValue(GrammarRelation[] associatedData2) {
		return CodingUtils.encodeRelations((AbstractRelation<?>[]) associatedData2);
	}

	@Override
	public GrammarRelation[] decodeValue(byte[] buffer, int i) {
		return CodingUtils.decodeGrammarRelations(i, buffer);
	}

	@Override
	public int getDataSize(byte[] buffer, int i) {
		return CodingUtils.estimateRelationsLength(i, buffer);
	}

}
