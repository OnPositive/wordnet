package com.onpositive.semantic.words2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.RelationTarget;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.SenseElementHandle;
import com.onpositive.semantic.words3.WordHandle;
import com.onpositive.semantic.words3.WordSequenceHandle;

public class SimpleWordNet extends WordNet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected HashMap<String, TextElement> wordMap = new HashMap<String, TextElement>();

	protected ArrayList<TextElement> words = new ArrayList<TextElement>();

	protected ArrayList<MeaningElement> meanings = new ArrayList<MeaningElement>();

	protected HashMap<String, WordFormTemplate> wordTemplateMap = new HashMap<String, WordFormTemplate>();

	protected HashMap<String, ArrayList<GrammarRelation>> wordforms = new HashMap<String, ArrayList<GrammarRelation>>();

	public Set<String> getFormsSet() {
		return wordforms.keySet();
	}

	public int size() {
		return words.size();
	}

	public void readGrammems(DataInputStream is) throws IOException {
		super.readGrammems(is);
	}

	@Override
	public void storeGrammems(DataOutputStream ds) throws IOException {
		super.storeGrammems(ds);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<TextElement> iterator() {
		return new ArrayList(words).iterator();
	}

	public void write(String string) throws FileNotFoundException, IOException {
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(string))) {

			@Override
			public void defaultWriteObject() throws IOException {
				super.defaultWriteObject();
			}
		};
		os.writeObject(this);
		os.close();
	}

	public static WordNet read(String path) throws FileNotFoundException,
			IOException {
		ObjectInputStream si = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(path)));
		try {
			Object s = si.readObject();
			si.close();
			SimpleWordNet s2 = (SimpleWordNet) s;
			s2.prepareWordSeqs();
			return s2;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SimpleWordNet() {

	}

	public SimpleWordNet(AbstractWordNet net) {
		loadGrammems(net);
		int max = 0;
		for (int a = 0; a < net.wordCount(); a++) {
			TextElement wordElement = net.getWordElement(a);
			if (wordElement == null) {
				continue;
			}
			max = Math.max(wordElement.id(), max);
		}
		for (int a = 0; a <= max; a++) {
			words.add(null);
		}
		for (int a = 0; a < net.wordCount(); a++) {
			TextElement wordElement = net.getWordElement(a);
			if (wordElement instanceof SenseElementHandle) {
				if (wordElement instanceof WordHandle) {
					Word w = fromWordHandle((WordHandle) wordElement);
					wordElement = w;
				}
				if (wordElement instanceof WordSequenceHandle) {
					fromWordSequenceHandle(wordElement);
				}
			}
			if (wordElement != null) {
				if (net.hasContinuations(wordElement)) {
					TextElement[] possibleContinuations = net
							.getPossibleContinuations(wordElement);
					ArrayList<Integer> mm = new ArrayList<Integer>();
					for (TextElement q : possibleContinuations) {
						mm.add(q.id());
					}
					sequenceMap.put(wordElement.id(), mm);
				}
			}
		}
		String[] allGrammarKeys = net.getAllGrammarKeys();
		for (String s : allGrammarKeys) {
			GrammarRelation[] possibleGrammarForms = net
					.getPossibleGrammarForms(s);
			for (GrammarRelation q : possibleGrammarForms) {
				q.setOwner(this);
			}
			wordforms.put(
					s,
					new ArrayList<GrammarRelation>(Arrays
							.asList(possibleGrammarForms)));
		}

	}

	private void fromWordSequenceHandle(TextElement wordElement) {
		WordSequenceHandle sk = (WordSequenceHandle) wordElement;
		WordHandle[] words2 = sk.getWords();
		Word[] ws = new Word[words2.length];
		for (int i = 0; i < words2.length; i++) {
			ws[i] = fromWordHandle(words2[i]);
		}
		SimpleSequence s = new SimpleSequence(ws, sk.id(), sk.getBasicForm(),
				this);
		ArrayList<WordMeaning> newMeanings = convertMeanings(sk.getConcepts(),
				s);
		s.meanings = newMeanings;
		this.words.set(s.id(), wordElement);
		this.wordMap.put(s.getBasicForm(), wordElement);
		for (WordMeaning m : newMeanings) {
			if (this.meanings.size() <= m.id()) {
				expendMeanings(m.id);
			}
			this.meanings.set(m.id, m);
		}
	}

	private void expendMeanings(int id) {
		while (meanings.size() <= id) {
			meanings.add(null);
		}
	}

	private Word fromWordHandle(WordHandle wordElement) {
		Word w = new Word(wordElement.getBasicForm(), wordElement.id(), this);
		MeaningElement[] concepts = wordElement.getConcepts();
		ArrayList<WordMeaning> newMeanings = convertMeanings(concepts, w);
		w.meanings = newMeanings;
		w.hasKind = true;
		this.words.set(wordElement.id(), wordElement);
		this.wordMap.put(wordElement.getBasicForm(), wordElement);
		for (WordMeaning m : newMeanings) {
			if (this.meanings.size() <= m.id()) {
				expendMeanings(m.id);
			}
			this.meanings.set(m.id, m);
		}
		return w;
	}

	private ArrayList<WordMeaning> convertMeanings(MeaningElement[] concepts,
			TextElement owner) {
		ArrayList<WordMeaning> result = new ArrayList<WordMeaning>();
		for (MeaningElement q : concepts) {
			WordMeaning rr = new WordMeaning(q.id(), this, owner);
			AbstractRelation<MeaningElement>[] allRelations = q
					.getAllRelations();
			for (AbstractRelation<MeaningElement> z : allRelations) {
				rr.registerRelation(z.relation, z.conceptId);
			}
			result.add(rr);
		}
		return result;
	}

	@Override
	protected void registerWord(Word word) {
		if (wordMap.containsKey(word.getBasicForm())) {
			if (!word.equals(wordMap.get(word.getBasicForm()))) {
				throw new IllegalStateException();
			}
		} else {
			words.add(word);
			wordMap.put(word.getBasicForm(), word);
		}
	}

	static final GrammarRelation[] wordRelations = new GrammarRelation[0];

	public GrammarRelation[] getPosibleWords(String wf) {
		ArrayList<GrammarRelation> arrayList = wordforms.get(wf);
		if (arrayList == null) {
			RelationTarget word = wordMap.get(wf);
			if (word != null) {
				return new GrammarRelation[] { new GrammarRelation(this,
						word.id(), 0) };
			}
			// now we should try to parse sequence
			return wordRelations;
		}
		return arrayList.toArray(new GrammarRelation[arrayList.size()]);
	}

	@Override
	public void registerWordForm(String wf, GrammarRelation form) {
		wf = wf.toLowerCase();
		ArrayList<GrammarRelation> arrayList = wordforms.get(wf);
		if (arrayList == null) {
			arrayList = new ArrayList<GrammarRelation>();
			wordforms.put(wf, arrayList);
		}
		for (GrammarRelation q : arrayList) {
			if (q.conceptId == form.conceptId && q.relation == form.relation) {
				return;
			}
		}
		for (GrammarRelation q : arrayList) {
			if (q.conceptId == form.conceptId
					&& form.relation == GrammarRelation.UNKNOWN_GRAMMAR_FORM) {
				return;
			}
		}
		for (GrammarRelation q : arrayList) {
			if (q.conceptId == form.conceptId
					&& q.relation == GrammarRelation.UNKNOWN_GRAMMAR_FORM) {
				arrayList.remove(q);
				arrayList.add(form);
				return;
			}
		}
		arrayList.add(form);
	}

	@Override
	public WordFormTemplate findTemplate(String string) {
		return wordTemplateMap.get(string);
	}

	public void registerTemplate(WordFormTemplate template) {
		wordTemplateMap.put(template.title, template);
	}

	public RelationTarget getWordMeaning(int id) {
		return meanings.get(id);
	}

	@Override
	public Word getOrCreateWord(String lowerCase) {
		if (wordMap.containsKey(lowerCase)) {
			return (Word) wordMap.get(lowerCase);
		}
		Word s = new Word(lowerCase, words.size(), this);
		WordMeaning mm = new WordMeaning(s.id, this, s);
		s.meanings.add(mm);
		meanings.add(mm);
		registerWord(s);
		return s;
	}

	@Override
	public void init() {
		IntIntOpenHashMap idrecoder = new IntIntOpenHashMap();
		for (TextElement w : this.words) {
			if (w instanceof Word) {
				Word s = (Word) w;
				if (s.template != null) {
					s.template.register((Word) w);
				}
				MeaningElement[] concepts = s.getConcepts();
				for (MeaningElement q : concepts) {
					WordMeaning wm = (WordMeaning) q;
					if (wm.id < 0) {
						int old = wm.id;
						wm.id = meanings.size();
						idrecoder.put(old, meanings.size());
						meanings.add(wm);
					}
				}
			}
		}
		// recode relations
		for (TextElement w : this.words) {
			if (w instanceof Word) {
				Word s = (Word) w;
				MeaningElement[] concepts = s.getConcepts();
				for (MeaningElement q : concepts) {
					AbstractRelationTarget wm = (AbstractRelationTarget) q;
					wm.recodeRelations(idrecoder);
				}
			}
		}
		for (MeaningElement w : this.meanings) {
			AbstractRelation<?>[] relations = w.getAllRelations();
			for (AbstractRelation<?> q : relations) {
				if (q.getOwner() == null) {
					q.setOwner(this);
				}
				if (q.relation == SemanticRelation.SPECIALIZATION) {
					AbstractRelationTarget word = (AbstractRelationTarget) getWordMeaning(q.conceptId);
					word.registerRelation(
							SemanticRelation.GENERALIZATION_BACK_LINK, w);
				}
				if (q.relation == SemanticRelation.GENERALIZATION) {
					AbstractRelationTarget word = (AbstractRelationTarget) getWordMeaning(q.conceptId);
					word.registerRelation(
							SemanticRelation.SPECIALIZATION_BACK_LINK, w);
				}
				if (q.relation == SemanticRelation.SYNONIM) {
					AbstractRelationTarget word = (AbstractRelationTarget) getWordMeaning(q.conceptId);
					word.registerRelation(SemanticRelation.SYNONIM_BACK_LINK, w);
				}
			}
		}
		prepareWordSeqs();
	}

	@Override
	public Word getOrCreateRelationTarget(String s) {
		String lowerCase = s.toLowerCase();
		return getOrCreateWord(lowerCase);
	}

	protected HashMap<Integer, ArrayList<Integer>> sequenceMap = new HashMap<Integer, ArrayList<Integer>>();

	protected void prepareWordSeqs() {
		l2: for (TextElement q : this) {
			if (!(q instanceof Word)) {
				continue;
			}
			Word w = (Word) q;

			if (w.getBasicForm().indexOf(' ') != -1) {
				String[] split = w.getBasicForm().split(" ");
				ArrayList<Word> sequence = new ArrayList<Word>();
				for (String s : split) {
					Word singlePossibleWord = getSinglePossibleWord(s);
					if (singlePossibleWord == null) {
						continue l2;
					}
					sequence.add(singlePossibleWord);
				}
				SimpleSequence s = new SimpleSequence(
						sequence.toArray(new Word[sequence.size()]), w.id(),
						w.getBasicForm(), this);
				s.meanings = w.meanings;
				registerSequence(s);
				// register sequence
				words.set(w.id(), s);
				wordMap.put(w.getBasicForm(), s);
				Word[] words2 = s.getWords();
				ArrayList<Integer> intArrayList = sequenceMap.get(words2[0].id);
				if (intArrayList == null) {
					intArrayList = new ArrayList<Integer>();
					sequenceMap.put(words2[0].id, intArrayList);
				}
				intArrayList.add(s.id);
			}
		}
	}

	private Word getSinglePossibleWord(String s) {
		GrammarRelation[] possibleGrammarForms = getPossibleGrammarForms(s);
		if (possibleGrammarForms != null) {
			HashSet<TextElement> tt = new HashSet<TextElement>();
			for (GrammarRelation q : possibleGrammarForms) {
				TextElement word = q.getWord();
				tt.add(word);

			}
			if (tt.size() == 1) {
				return (Word) tt.iterator().next();
			}
		}
		return null;
	}

	protected class Sequences implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected ArrayList<SimpleSequence> ws = new ArrayList<SimpleSequence>();

		public void add(SimpleSequence sequence) {
			ws.add(sequence);
			System.out.println(sequence);
		}

		public SimpleSequence match(int a, ArrayList<Word> w) {
			for (SimpleSequence q : ws) {
				if (q.match(w, a)) {
					return q;
				}
			}
			return null;

		}

	}

	protected HashMap<Word, Sequences> seq = new HashMap<Word, SimpleWordNet.Sequences>();

	private Sequences registerSequence(SimpleSequence sequence) {
		if (seq == null) {
			seq = new HashMap<Word, SimpleWordNet.Sequences>();
		}
		Word key = sequence.get(0);
		Sequences sequences = seq.get(key);
		if (sequences == null)

		{
			sequences = new Sequences();
			seq.put(key, sequences);
		}
		sequences.add(sequence);
		return sequences;
	}

	public Word getSingleWord(GrammarRelation[] posibleWords) {
		if (posibleWords == null || posibleWords.length == 0) {
			return null;
		}
		if (posibleWords.length == 1) {
			return (Word) posibleWords[0].getWord();
		}
		int id = -1;
		for (GrammarRelation r : posibleWords) {
			TextElement word = r.getWord();
			if (word instanceof Word) {
				Word wr = (Word) word;

				if (wr.mayBeUsedAsNoun()) {
					if (r.relation == NounFormRule.NOM_SG) {
						return wr;
					}
					if (r.relation == NounFormRule.NOM_PL) {
						return wr;
					}
					if (r.relation == 0) {
						return wr;
					}
				}
			}
			if (id == -1) {
				id = r.conceptId;
			} else if (id != r.conceptId) {
				return null;
			}
		}
		if (id == -1) {
			return null;
		}
		return (Word) words.get(id);
	}

	@Override
	public int wordCount() {
		return words.size();
	}

	@Override
	public int conceptCount() {
		return meanings.size();
	}

	@Override
	public int grammarFormsCount() {
		return wordforms.size();
	}

	@Override
	public MeaningElement getConceptInfo(int conceptId) {
		return null;
	}

	@Override
	public GrammarRelation[] getPossibleGrammarForms(String wordForm) {
		return getPosibleWords(wordForm);
	}

	@Override
	public TextElement getWordElement(int wordId) {
		return (TextElement) words.get(wordId);
	}

	@Override
	public TextElement getWordElement(String basicForm) {
		return (TextElement) wordMap.get(basicForm);
	}

	@Override
	public void markRedirect(String from, String to) {

	}

	@Override
	public TextElement[] getPossibleContinuations(TextElement startOfSequence) {
		ArrayList<Integer> intArrayList = sequenceMap.get(startOfSequence.id());
		if (intArrayList != null) {
			TextElement[] result = new TextElement[intArrayList.size()];
			int a = 0;
			for (Integer q : intArrayList) {
				result[a++] = getWordElement(q);
			}
			return result;
		}
		return new TextElement[0];
	}

	public HashMap<Integer, ArrayList<Integer>> getSequenceMap() {
		return sequenceMap;
	}

	@Override
	public boolean hasContinuations(TextElement te) {
		return sequenceMap.containsKey(te.id());
	}

	public int getGrammemCode(LinkedHashSet<Grammem> mmm) {
		return registerGrammemSet(mmm);
	}

	public ObjectIntOpenHashMap<LinkedHashSet<Grammem>> iset() {
		return iset;
	}

	public IntObjectOpenHashMap<LinkedHashSet<Grammem>> gset() {
		return set;
	}

	@Override
	public String[] getAllGrammarKeys() {
		return wordforms.keySet().toArray(new String[wordforms.size()]);
	}

}