package com.onpositive.semantic.words3;

public interface IGrammarVisitor {

	void relation(int word, int relatedWord);

	void startVisit();
	
	void endVisit();

	boolean oneWord(int word);

}
