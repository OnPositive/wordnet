package com.onpositive.semantic.wordnet;

import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

/**
 * This class represents semantic meaning in a natural language
 * 
 * @author kor
 * 
 */
public abstract class MeaningElement extends RelationTarget {

	private static final SemanticRelation[] SEMANTIC_RELATIONS = new SemanticRelation[0];
	private static final MorphologicalRelation[] MORPHO_RELATIONS = new MorphologicalRelation[0];

	public MeaningElement(AbstractWordNet owner) {
		super(owner);
	}

	public abstract TextElement getParentTextElement();


	public abstract AbstractRelation<MeaningElement>[] getAllRelations();
	
	public  SemanticRelation[] getSemanticRelations(){
		AbstractRelation<MeaningElement>[] allRelations = getAllRelations();
		int count=0;
		for (AbstractRelation<?>q:allRelations){
			if (q instanceof SemanticRelation){
				count++;
			}
		}
		if (count>0){
			int a=0;
			SemanticRelation[] relations=new SemanticRelation[count];
			for (AbstractRelation<?>q:allRelations){
				if (q instanceof SemanticRelation){
					relations[a++]=(SemanticRelation) q;
				}
			}
			return relations;
		}
		return SEMANTIC_RELATIONS;
	}
	public  MorphologicalRelation[] getMorphologicalRelations(){
		AbstractRelation<MeaningElement>[] allRelations = getAllRelations();
		int count=0;
		for (AbstractRelation<?>q:allRelations){
			if (q instanceof MorphologicalRelation){
				count++;
			}
		}
		if (count>0){
			int a=0;
			MorphologicalRelation[] relations=new MorphologicalRelation[count];
			for (AbstractRelation<?>q:allRelations){
				if (q instanceof MorphologicalRelation){
					relations[a++]=(MorphologicalRelation) q;
				}
			}
			return relations;
		}
		return MORPHO_RELATIONS;
	}
	
	public Set<Grammem>getGrammems(){
		return owner.getGrammemSet(getGrammemCode());
	}

	public abstract short getGrammemCode();

	@Override
	public String toString() {
		return getParentTextElement().getBasicForm() + "(" + encodeKind() + ")";
	}

	private String encodeKind() {
		return getGrammems().toString();
	}

	public int getKind() {
		Set<Grammem> grammems = getGrammems();
		for (Grammem m:grammems){
			if (m instanceof PartOfSpeech){
				return m.intId;
			}
		}
		return 0;
	}

	
}