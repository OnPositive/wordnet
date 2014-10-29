package com.onpositive.semantic.words3;

import com.onpositive.semantic.wordnet.GrammarRelation;

public class GrammarRelationVisitingEncoder extends GrammarRelationEncoder {
	
	private final IGrammarVisitor visitor;

	public GrammarRelationVisitingEncoder(IGrammarVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public GrammarRelation[] decodeValue(byte[] buffer, int i) {
		CodingUtils.visitGrammarRelations(i, buffer, visitor);
		return null;
	}


}
