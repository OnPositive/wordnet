package com.onpositive.semantic.wordnet.edit;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words2.AbstractRelationTarget;
import com.onpositive.semantic.words2.SimpleWordNet;

public class SimpleWordNetEditInterface implements IWordNetEditInterface {

	SimpleWordNet original;

	@Override
	public TextElement registerWord(String word) {
		return original.getOrCreateWord(word.toLowerCase());
	}

	@Override
	public void removeWord(String word) {
		throw new IllegalStateException();
	}

	@Override
	public AbstractWordNet getWordNet() {
		return original;
	}

	@Override
	public void addSemanticRelation(TextElement from, SemanticRelation tt) {
		MeaningElement[] concepts = from.getConcepts();
		AbstractRelationTarget t = (AbstractRelationTarget) concepts[concepts.length - 1];
		t.registerRelation(tt.relation, tt.conceptId);
	}
	
	@Override
	public void removeSemanticRelation(TextElement from, SemanticRelation tt) {
		MeaningElement[] concepts = from.getConcepts();
		AbstractRelationTarget t = (AbstractRelationTarget) concepts[concepts.length - 1];
		t.unregisterRelation(tt.relation, tt.conceptId);
	}

	@Override
	public void addMorphologicalRelation(TextElement to,
			MorphologicalRelation tt) {
		MeaningElement[] concepts = to.getConcepts();
		AbstractRelationTarget t = (AbstractRelationTarget) concepts[concepts.length - 1];
		t.registerRelation(tt.relation, tt.conceptId);
	}

	@Override
	public void removeMorphologicalRelation(TextElement to,
			MorphologicalRelation tt) {
		MeaningElement[] concepts = to.getConcepts();
		AbstractRelationTarget t = (AbstractRelationTarget) concepts[concepts.length - 1];
		t.unregisterRelation(tt.relation, tt.conceptId);
	}

}