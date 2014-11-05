package com.onpositive.wordnet.viewer.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.onpositive.semantic.ui.workbench.elements.XMLView;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class WordFormView extends XMLView {

	public WordFormView() {
		super("dlf/wordview.dlf");
	}

	protected String wordForm;

	public String getWordForm() {
		return wordForm;
	}

	protected GrammarRelation[] relations = new GrammarRelation[0];

	public void setWordForm(String wordForm) {
		this.wordForm = wordForm;
		relations = WordNetProvider.getInstance()
				.getPossibleGrammarFormsWithSuggestions(wordForm.toLowerCase());
		if (relations == null) {
			relations = new GrammarRelation[0];
		}
	}

	List<GrammarRelation> getRelations() {
		return Arrays.asList(relations);
	}

	public List<WordModel> models(Object q) {
		if (q == null) {
			return Collections.emptyList();
		}
		TextElement element = null;
		if (q instanceof GrammarRelation) {
			GrammarRelation mm = (GrammarRelation) q;
			element = mm.getWord();
		}
		MeaningElement[] concepts = element.getConcepts();
		ArrayList<WordModel> mdl = new ArrayList<WordModel>();
		for (MeaningElement qa : concepts) {
			if (qa instanceof MeaningElement) {
				mdl.add(new WordModel((MeaningElement) qa, ((MeaningElement) qa)
						.getOwner()));
			}
		}

		/*
		 * for (Object q:l){
		 * 
		 * }
		 */
		return mdl;
	}
}
