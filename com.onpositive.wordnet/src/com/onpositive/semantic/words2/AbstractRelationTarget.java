package com.onpositive.semantic.words2;

import java.io.Serializable;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.RelationTarget;
import com.onpositive.semantic.wordnet.SemanticRelation;

public abstract class AbstractRelationTarget extends MeaningElement implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unchecked")
	private static final AbstractRelation<MeaningElement>[] NO_RELATIONS = new AbstractRelation[0];

	protected int[] relations;
	protected int id;

	public AbstractRelationTarget(AbstractWordNet owner, int id) {
		super(owner);
		this.id = id;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public AbstractWordNet getOwner() {
		return owner;
	}

	@SuppressWarnings("unchecked")
	public AbstractRelation<MeaningElement>[] getAllRelations() {
		if (relations == null) {
			return NO_RELATIONS;
		}
		AbstractRelation<MeaningElement>[] result = new AbstractRelation[relations.length / 2];
		for (int a = 0; a < relations.length; a += 2) {
			int relationId = relations[a];
			AbstractRelation<MeaningElement> wordRelation = null;
			if (relationId >= MorphologicalRelation.MORPHOLOGICAL_OFFSET) {
				wordRelation = new MorphologicalRelation(null,
						relations[a + 1], relationId);
			} else {
				wordRelation = new SemanticRelation(null, relations[a + 1],
						relationId);
			}
			wordRelation.setOwner(owner);
			result[a / 2] = wordRelation;
		}
		return result;
	}
	public void recodeRelations(IntIntOpenHashMap idrecoder) {
		if (relations!=null){
		for (int a = 0; a < relations.length; a += 2) {
			int relationId = relations[a+1];
			if (idrecoder.containsKey(relationId)){
				relations[a+1]=idrecoder.get(relationId);
			}
		}
		}
	}

	public void registerRelation(int kind, RelationTarget wordRelation) {
		if (relations == null) {
			relations = new int[0];
		}

		int length = relations.length;
		int[] ne = new int[length + 2];
		System.arraycopy(relations, 0, ne, 0, relations.length);
		ne[relations.length] = kind;
		int id2 = wordRelation.id();		
		ne[relations.length + 1] = id2;
		this.relations = ne;
	}

	public boolean isSynonim(AbstractRelationTarget other) {
		int id = other.id();
		if (this.relations != null) {
			for (int a = 0; a < this.relations.length; a += 2) {
				int q = this.relations[a + 1];
				if (q == id) {
					int rel = this.relations[a];
					if (rel == SemanticRelation.SYNONIM
							| rel == SemanticRelation.SYNONIM_BACK_LINK) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isSpecialization(AbstractRelationTarget other) {
		int id = other.id();
		if (this.relations != null) {
			for (int a = 0; a < this.relations.length; a += 2) {
				int q = this.relations[a + 1];
				if (q == id) {
					int rel = this.relations[a];
					if (rel == SemanticRelation.SPECIALIZATION
							| rel == SemanticRelation.SPECIALIZATION_BACK_LINK) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isGeneralization(AbstractRelationTarget other) {
		int id = other.id();
		if (this.relations != null) {
			for (int a = 0; a < this.relations.length; a += 2) {
				int q = this.relations[a + 1];
				if (q == id) {
					int rel = this.relations[a];
					if (rel == SemanticRelation.GENERALIZATION
							| rel == SemanticRelation.GENERALIZATION_BACK_LINK) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isRelated(AbstractRelationTarget r) {
		return isSynonim(r) || isGeneralization(r) || isSpecialization(r);
	}

	public boolean matchRelated(RelationTarget relationTarget) {
		if (this.equals(relationTarget)) {
			return true;
		}
		return isRelated((AbstractRelationTarget) relationTarget);
	}
}