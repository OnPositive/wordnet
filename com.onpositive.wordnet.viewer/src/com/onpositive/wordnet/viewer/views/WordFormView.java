package com.onpositive.wordnet.viewer.views;

import java.util.Arrays;
import java.util.List;

import com.onpositive.semantic.ui.workbench.elements.XMLView;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class WordFormView extends XMLView{

	
	public WordFormView(){
		super("dlf/wordview.dlf");
	}
	protected String wordForm;

	public String getWordForm() {
		return wordForm;
	}
	protected GrammarRelation[] relations=new GrammarRelation[0];

	public void setWordForm(String wordForm) {
		this.wordForm = wordForm;
		relations=WordNetProvider.getInstance().getPossibleGrammarFormsWithSuggestions(wordForm.toLowerCase());
		if (relations==null){
			relations=new GrammarRelation[0];
		}
	}
	
	List<GrammarRelation>getRelations(){
		return Arrays.asList(relations);
	}
}
