package com.onpositive.semantic.wordnet.edit;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;

public interface IWordNetEditInterface {

	TextElement registerWord(String word);

	void removeWord(String word);
	
	AbstractWordNet getWordNet();

	void addSemanticRelation(TextElement from, SemanticRelation tt);

	void addMorphologicalRelation(TextElement to, MorphologicalRelation tt);
	
	void removeSemanticRelation(TextElement from, SemanticRelation tt); 
	
	void removeMorphologicalRelation(TextElement to, MorphologicalRelation tt);
}
