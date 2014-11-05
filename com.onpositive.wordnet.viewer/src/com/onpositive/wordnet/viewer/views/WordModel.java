package com.onpositive.wordnet.viewer.views;

import java.util.ArrayList;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.SemanticRelation;

public class WordModel {

	protected MeaningElement meaning;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meaning == null) ? 0 : meaning.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordModel other = (WordModel) obj;
		if (meaning == null) {
			if (other.meaning != null)
				return false;
		} else if (!meaning.equals(other.meaning))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	protected AbstractWordNet owner;

	public WordModel(MeaningElement word, AbstractWordNet owner2) {
		this.owner = owner2;
		this.meaning = word;
	}
	@Override
	public String toString() {
		return meaning.getParentTextElement().getBasicForm();
	}
	
	public WordModel[] getSynonims(){
		return getRelated(SemanticRelation.SYNONIM,SemanticRelation.SYNONIM_BACK_LINK);		
	}
	public WordModel[] getGeneralization(){
		return getRelated(SemanticRelation.GENERALIZATION,SemanticRelation.GENERALIZATION_BACK_LINK);		
	}
	public WordModel[] getSpecialization(){
		return getRelated(SemanticRelation.SPECIALIZATION,SemanticRelation.SPECIALIZATION_BACK_LINK);		
	}
	public WordModel[] getMeronims(){
		return getRelated(SemanticRelation.MERONIM,-1);		
	}

	public WordModel[] getRelated(int kind, int synonimBackLink) {
		ArrayList<WordModel> result = new ArrayList<WordModel>();
		SemanticRelation[] semanticRelations = meaning.getSemanticRelations();
		for (SemanticRelation q : semanticRelations) {
			if (q.relation == kind||q.relation == synonimBackLink) {
				MeaningElement word = q.getWord();
				result.add(new WordModel(word, owner));
			}
		}
		return result.toArray(new WordModel[result.size()]);
	}
}
