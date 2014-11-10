package com.onpositive.semantic.wordnet.edit;

import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;

public class WordNetPatch {

	protected static abstract class AbstractOperation {

		protected abstract void execute(IWordNetEditInterface net);
	}

	public static class WordOperation extends AbstractOperation {
		public final String word;
		boolean removal;

		public WordOperation(String word, boolean removal) {
			super();
			this.word = word;
			this.removal = removal;
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			if (removal) {
				net.removeWord(word);
			} else {
				net.registerWord(word);
			}
		}
	}

	public abstract static class RelationOperation extends AbstractOperation {
		public final String from;
		public final String to;
		public final int code;
		boolean removal;

		public RelationOperation(String from, String to, boolean removal,
				int code) {
			super();
			this.from = from;
			this.to = to;
			this.removal = removal;
			this.code = code;
		}
	}

	public static class SemanticRelationOperation extends RelationOperation {

		public SemanticRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			if (removal) {
				TextElement from = getOrCreate(net, this.from);
				TextElement to = getOrCreate(net, this.to);
				SemanticRelation tt = new SemanticRelation(net.getWordNet(),
						to.id(), code);
				SemanticRelation tt1 = new SemanticRelation(net.getWordNet(),
						from.id(), code);
				net.removeSemanticRelation(from, tt);
				net.removeSemanticRelation(to, tt1);
			} else {
				TextElement from = getOrCreate(net, this.from);
				TextElement to = getOrCreate(net, this.to);
				SemanticRelation tt = new SemanticRelation(net.getWordNet(),
						to.id(), code);
				SemanticRelation tt1 = new SemanticRelation(net.getWordNet(),
						from.id(), backlink(code));
				net.addSemanticRelation(from, tt);
				net.addSemanticRelation(to, tt1);
			}
		}

		private int backlink(int code) {
			switch (code) {
			case SemanticRelation.ANTONIM:
				return SemanticRelation.ANTONIM_BACKLINK;
			case SemanticRelation.ANTONIM_BACKLINK:
				return SemanticRelation.ANTONIM;
			case SemanticRelation.SYNONIM:
				return SemanticRelation.SYNONIM_BACK_LINK;
			case SemanticRelation.SYNONIM_BACK_LINK:
				return SemanticRelation.SYNONIM;
			case SemanticRelation.MERONIM:
				return SemanticRelation.MERONIM_BACKLINK;
			case SemanticRelation.MERONIM_BACKLINK:
				return SemanticRelation.MERONIM;
			case SemanticRelation.GENERALIZATION:
				return SemanticRelation.GENERALIZATION_BACK_LINK;
			case SemanticRelation.GENERALIZATION_BACK_LINK:
				return SemanticRelation.GENERALIZATION;
			case SemanticRelation.SPECIALIZATION:
				return SemanticRelation.SPECIALIZATION_BACK_LINK;
			case SemanticRelation.SPECIALIZATION_BACK_LINK:
				return SemanticRelation.SPECIALIZATION;				
			default:
				break;
			}
			return 0;
		}

	}

	public static class MorphologicalRelationOperation extends
			RelationOperation {

		public MorphologicalRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			
			if (removal) {
				TextElement from = getOrCreate(net, this.from);
				TextElement to = getOrCreate(net, this.to);
				MorphologicalRelation tt = new MorphologicalRelation(net.getWordNet(),
						to.id(), code);
				MorphologicalRelation tt1 = new MorphologicalRelation(net.getWordNet(),
						from.id(), code);
				net.removeMorphologicalRelation(from, tt);
				net.removeMorphologicalRelation(to, tt1);
			} else {
				TextElement from = getOrCreate(net, this.from);
				TextElement to = getOrCreate(net, this.to);
				MorphologicalRelation tt = new MorphologicalRelation(net.getWordNet(),
						to.id(), code);
				MorphologicalRelation tt1 = new MorphologicalRelation(net.getWordNet(),
						from.id(), code+MorphologicalRelation.BACK_LINK_OFFSET);
				net.addMorphologicalRelation(from, tt);
				net.addMorphologicalRelation(to, tt1);
			}
		}

	}

	public static class GrammarRelationOperation extends RelationOperation {

		public GrammarRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
			throw new UnsupportedOperationException();
		}

		@Override
		protected void execute(IWordNetEditInterface net) {

		}
	}

	private static TextElement getOrCreate(IWordNetEditInterface net,
			String from2) {
		TextElement from = net.getWordNet().getWordElement(from2);
		if (from == null) {
			from = net.registerWord(from2);
		}
		return from;
	}
}
