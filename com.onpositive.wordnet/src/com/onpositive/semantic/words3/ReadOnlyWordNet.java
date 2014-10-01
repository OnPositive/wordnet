package com.onpositive.semantic.words3;

import static com.onpositive.semantic.words3.hds.StringCoder.int0;
import static com.onpositive.semantic.words3.hds.StringCoder.int1;
import static com.onpositive.semantic.words3.hds.StringCoder.int2;
import static com.onpositive.semantic.words3.hds.StringCoder.makeInt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.onpositive.semantic.wordnet.AbstractRelation;
import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.RelationTarget;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words2.SimpleSequence;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words2.Word;
import com.onpositive.semantic.words3.dto.ConceptInfo;
import com.onpositive.semantic.words3.dto.SenseElementInfo;
import com.onpositive.semantic.words3.dto.SequenceInfo;
import com.onpositive.semantic.words3.dto.WordInfo;
import com.onpositive.semantic.words3.hds.StringCoder;
import com.onpositive.semantic.words3.hds.StringToDataHashMap;

public class ReadOnlyWordNet extends AbstractWordNet {

	protected GrammarRelationsStore relations;
	
	public String[] getAllGrammarKeys() {
		return relations.getAllKeys();
	}

	protected WordStore wordsData;
	protected IntIntOpenHashMap sequences = new IntIntOpenHashMap();
	protected byte[] store;
	
	

	/**
	 * extremly simple write only string dictionary;
	 * 
	 * @author kor
	 * 
	 */
	public final class GrammarRelationsStore extends
			StringToDataHashMap<GrammarRelation[]> implements Serializable {
		private static final long serialVersionUID = 1L;

		public GrammarRelationsStore(int sz) {
			super(sz);
		}

		@Override
		protected final GrammarRelation[] decodeValue(byte[] buffer, int addr) {
			return decodeGrammarRelations(addr, buffer);
		}

		@Override
		protected byte[] encodeValue(GrammarRelation[] value) {
			byte[] encodeRelations = encodeRelations(value);
			GrammarRelation[] decodeGrammarRelations = decodeGrammarRelations(0, encodeRelations);
			if (!Arrays.equals(decodeGrammarRelations, value)){
				throw new IllegalStateException();
			}
			return encodeRelations;
		}

		public int size() {
			return usedCount;
		}
	}

	public final class WordStore extends StringToDataHashMap<SenseElementInfo> {
		public static final int CUSTOM_CASE_OFFSET = -50;
		
		public static final int SMALL_KIND = 8;
		public static final int NO_RELATIONS = 4;
		
		protected int[] wordIdToOffset;
		protected int[] conceptIdToOffet;

		protected final byte[] buffer() {
			return this.byteBuffer;
		}

		@Override
		public void write(DataOutputStream ds) throws IOException {
			super.write(ds);
			writeIntArray(wordIdToOffset, ds);
			writeIntArray(conceptIdToOffet, ds);
		}

		@Override
		public void read(DataInputStream is) throws IOException {
			super.read(is);
			wordIdToOffset = readIntArray(is);
			conceptIdToOffet = readIntArray(is);
		}

		public WordStore(int sz, int conceptCount) {
			super(sz * 4 / 3 + 10);
			wordIdToOffset = new int[sz];
			conceptIdToOffet = new int[conceptCount];
			for (int a = 0; a < wordIdToOffset.length; a++) {
				wordIdToOffset[a] = Integer.MIN_VALUE;
			}
			for (int a = 0; a < conceptIdToOffet.length; a++) {
				conceptIdToOffet[a] = Integer.MIN_VALUE;
			}
		}

		public SenseElementInfo getInfo(int index) {
			int addr = wordIdToOffset[index];
			if (addr == Integer.MIN_VALUE) {
				return null;
			}
			if (addr < 0) {
				addr = -addr;
			}
			return decodeValue(this.byteBuffer, addr - 1);
		}

		public ConceptInfo getConceptInfo(int conceptIndex) {
			int addr = conceptIdToOffet[conceptIndex];
			if (addr == Integer.MIN_VALUE) {
				return null;
			}
			if (addr < 0) {
				SenseElementInfo decodeValue = decodeValue(byteBuffer,
						-addr - 1);
				return decodeValue.senses[0];
			}
			return ConceptInfo.getFromBytes(byteBuffer, addr);
		}

		@Override
		public int store(String string, SenseElementInfo data) {
			int store = super.store(string, data);
			int addr = store + string.length();
			
			wordIdToOffset[data.elementId] = data instanceof WordInfo ? addr
					: -addr;
			int conceptStart = addr + 4;
			if (data.senses.length == 1) {
				conceptIdToOffet[data.senses[0].senseId] = -addr;
			} else {
				for (ConceptInfo infs : data.senses) {
					conceptIdToOffet[infs.senseId] = conceptStart;
					SeparateConceptHandle separateConceptHandle = new SeparateConceptHandle(conceptStart, wordsData);
					if (separateConceptHandle.id()!=infs.senseId){
						System.out.println("Error");
					}
					short features = separateConceptHandle.getGrammemCode();
					if (features!=infs.gcode){
						System.out.println("Error");
					}
					int tid=separateConceptHandle.textElementId();
					if (tid!=data.elementId){
						System.out.println("Error");
					}
					AbstractRelation<MeaningElement>[] semanticRelations = separateConceptHandle.getAllRelations();
					if (!Arrays.equals(semanticRelations, infs.relations)){
						System.out.println("Error");
					}
					byte[] byteArray = infs.toByteArray();
					int conceptLength = ConceptInfo.getConceptLength(conceptStart-1, wordsData.byteBuffer);
					if (conceptLength!=byteArray.length){
						System.out.println(conceptLength+":"+byteArray.length);
						System.out.println("Error");
					}
					conceptStart += byteArray.length;
				}
				SenseElementHandle wordElement = (SenseElementHandle) getWordElement(data.elementId);
				MeaningElement[] concepts = wordElement.getConcepts();
				for (MeaningElement q:concepts){
					TextElement parentTextElement = q.getParentTextElement();
					if (parentTextElement==null){
						q.getParentTextElement();
					}
					if (parentTextElement.id()!=data.elementId){
						System.out.println("Error");
					}
				}
			}
			
			return store;
		}

		final int getDataAddress2(String string) {
			return super.getDataAddress(string);
		}

		public void store(RelationTarget t) {
			if (t instanceof Word) {
				String basicForm = ((Word) t).getBasicForm();
				 
				if (basicForm.length() == 0) {
					return;
				}
				if (basicForm.equals("вертолёт")){
					System.out.println("A");
				}
				for (int a = 0; a < basicForm.length(); a++) {
					char charAt = basicForm.charAt(a);
					if (!Character.isLetter(charAt)
							&& !Character.isWhitespace(charAt)) {
						return;
					}
				}
				store(basicForm, toWordInfo((Word) t));
			}
			if (t instanceof SimpleSequence) {
				SimpleSequence basicForm = ((SimpleSequence) t);
				store(basicForm.getBasicForm(), toWordInfo(basicForm));
			}
		}

		@Override
		protected SenseElementInfo decodeValue(byte[] buffer, int addr) {
			int id = makeInt((byte) 0, buffer[2 + addr], buffer[1 + addr],
					buffer[addr]);
			byte count = buffer[addr + 3];
			ConceptInfo[] infos = null;
			int pos = addr + 4;
			if (count > 0) {
				infos = new ConceptInfo[count];
				for (int a = 0; a < count; a++) {
					ConceptInfo fromBytes = ConceptInfo.getFromBytes(buffer,
							pos);
					infos[a] = fromBytes;
					pos += fromBytes.sizeInBytes();
				}
			} else {
				int msk = -((int) count);
				msk += CUSTOM_CASE_OFFSET;
				boolean smallKind = (msk & SMALL_KIND) != 0;
				boolean noRelations = (msk & NO_RELATIONS) != 0;
				int kind = 0;
				if (smallKind) {
					kind = buffer[pos++];
				} else {
					kind = makeInt((byte) 0, (byte) 0, buffer[pos + 1],
							buffer[pos]);
					pos += 2;
				}
				
				
				AbstractRelation<MeaningElement>[] rels = null;
				if (noRelations) {
					rels = new SemanticRelation[0];
				} else
					rels = decodeMeaningRelations(pos, buffer);
				ConceptInfo conceptInfo = new ConceptInfo(id, id, (short) kind, rels);
				infos = new ConceptInfo[] { conceptInfo };
				pos += length(rels);
			}
			byte c = buffer[pos++];
			String wordName = getStringBeforeData(buffer, addr);
			if (c == -1) {
				return new WordInfo(id, wordName, infos);
			} else {
				int[] wids = new int[c];
				for (int p = 0; p < c; p++) {
					byte c0 = buffer[pos++];
					byte c1 = buffer[pos++];
					byte c2 = buffer[pos++];
					int wid = makeInt((byte) 0, c2, c1, c0);
					wids[p] = wid;
				}
				return new SequenceInfo(id, wordName, infos, wids);
			}
		}

		public String getStringBeforeData(byte[] buffer, int addr) {
			int stOff = 0;
			for (int a = addr - 1; a >= 0; a--) {
				if (buffer[a] < 0 && a != addr - 1) {
					stOff = a + 1;
					break;
				}
			}
			String wordName = decode(stOff + 1);
			return wordName;
		}

		int smallPath;
		int smallPathLength;
		int otherPath;
		int otherPathLength;

		@Override
		protected byte[] encodeValue(SenseElementInfo value) {
			ByteArrayList ll = new ByteArrayList();
			ll.add(int0(value.elementId));
			ll.add(int1(value.elementId));
			ll.add(int2(value.elementId));
			int length = value.senses.length;
			if (length == 1) {
				ConceptInfo tt = value.senses[0];
				byte vl = CUSTOM_CASE_OFFSET;
				// no features
				
				// no relations
				if (tt.relations.length == 0) {
					vl -= NO_RELATIONS;

				}
				// simple kind
				if (tt.gcode < 255) {
					vl -= SMALL_KIND;
				}
				
				ll.add(vl);
				ll.add(tt.encodeWordInfoSmall(tt.gcode,
						tt.relations));
				byte[] array = ll.toArray();
				smallPath++;
				smallPathLength += array.length;
				// return array;
				// features
			} else {
				ll.add((byte) length);
				for (ConceptInfo q : value.senses) {
					byte[] byteArray = q.toByteArray();
					ll.add(byteArray, 0, byteArray.length);
				}
			}
			if (value instanceof SequenceInfo) {
				SequenceInfo si = (SequenceInfo) value;
				ll.add((byte) si.words.length);
				for (int q : si.words) {
					ll.add(int0(q));
					ll.add(int1(q));
					ll.add(int2(q));
				}
				ll.add((byte) -1);
			} else {
				ll.add((byte) -1);
			}
			byte[] array = ll.toArray();
			otherPath++;
			otherPathLength += array.length;
			return array;
		}

		public SenseElementInfo toWordInfo(Word w) {
			MeaningElement[] concepts = w.getConcepts();
			ConceptInfo[] wordSenseInfo = conceptsConvert(w, concepts);
			SenseElementInfo data = new WordInfo(w.id(), w.getBasicForm(),
					wordSenseInfo);
			return data;
		}

		private ConceptInfo[] conceptsConvert(TextElement par,
				MeaningElement[] concepts) {
			ConceptInfo[] result = new ConceptInfo[concepts.length];
			for (int a = 0; a < concepts.length; a++) {
				MeaningElement el = concepts[a];
				result[a] = new ConceptInfo(par.id(), el.id(), 
						el.getGrammemCode(), el.getAllRelations());
			}
			return result;
		}

		public SenseElementInfo toWordInfo(SimpleSequence w) {
			ConceptInfo[] wordSenseInfo = conceptsConvert(w, w.getConcepts());
			Word[] words = w.getWords();
			int[] wIds = new int[words.length];
			for (int a = 0; a < wIds.length; a++) {
				wIds[a] = words[a].id();
			}
			SenseElementInfo data = new SequenceInfo(w.id(), w.getBasicForm(),
					wordSenseInfo, wIds);
			return data;
		}

		public ReadOnlyWordNet getWordNet() {
			return ReadOnlyWordNet.this;
		}
	}

	public ReadOnlyWordNet(SimpleWordNet original) {
		Set<String> formsSet = original.getFormsSet();
		this.iset=original.iset();
		this.set=original.gset();
		relations = new GrammarRelationsStore(formsSet.size() * 4 / 3 + 10);
		WordStore store = new WordStore(original.size(),
				original.conceptCount());
		this.wordsData = store;
		for (RelationTarget t : original) {
			store.store(t);
		}
		
		for (String q : original.getFormsSet()) {
			relations.store(q, original.getPosibleWords(q));
		}
		HashMap<Integer,ArrayList<Integer>> sequenceMap = original
				.getSequenceMap();
		ByteArrayList mm = new ByteArrayList();

		for (Integer q : sequenceMap.keySet()) {
			sequences.put(q, mm.size());
			ArrayList<Integer> intArrayList = sequenceMap.get(q);
			int size = intArrayList.size();
			if (size > 120) {
				if (size > Short.MAX_VALUE) {
					throw new IllegalStateException();
				}
				mm.add((byte) -1);
				mm.add(StringCoder.int0(size));
				mm.add(StringCoder.int1(size));
			} else {
				mm.add((byte) size);
			}
			for (Integer v : intArrayList) {
				mm.add(StringCoder.int0(v));
				mm.add(StringCoder.int1(v));
				mm.add(StringCoder.int2(v));
			}
			
		}
		this.store = mm.toArray();
	}

	public ReadOnlyWordNet(DataInputStream is) throws IOException {
		relations = new GrammarRelationsStore(10);
		relations.read(is);
		wordsData = new WordStore(5, 5);
		wordsData.read(is);
		int readInt = is.readInt();
		this.store=new byte[readInt];
		is.readFully(this.store);
		sequences=StringToDataHashMap.readMap(is);
		readGrammems(is);
	}

	public static ReadOnlyWordNet load(String file) throws IOException {
		return load(new File(file));
	}

	public static ReadOnlyWordNet load(File file) throws FileNotFoundException,
			IOException {
		DataInputStream is = new DataInputStream(new BufferedInputStream(
				new FileInputStream(file)));
		try {
			return new ReadOnlyWordNet(is);
		} finally {
			is.close();
		}
	}

	public void store(String fl) throws FileNotFoundException, IOException {

		store(new File(fl));
	}

	public void store(File fl) throws FileNotFoundException, IOException {
		DataOutputStream stream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(fl)));
		try {
			store(stream);
		} finally {
			stream.close();
		}
	}

	public void trim() {
		relations.trim();
		wordsData.trim();
	}

	public void store(DataOutputStream stream) throws IOException {
		relations.write(stream);
		wordsData.write(stream);
		stream.writeInt(store.length);
		stream.write(store);
		StringToDataHashMap.writeMap(stream, sequences);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		storeGrammems(dataOutputStream);
		storeGrammems(stream);
		dataOutputStream.close();
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		DataInputStream di=new DataInputStream(new ByteArrayInputStream(byteArray));
		SimpleWordNet simpleWordNet = new SimpleWordNet();
		simpleWordNet.readGrammems(di);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream ds = new DataOutputStream(out);
		simpleWordNet.storeGrammems(ds);
		ds.close();
		byte[] byteArray2 = out.toByteArray();
		if (!Arrays.equals(byteArray2, byteArray)){
			throw new IllegalStateException();
		}
	}

	public static int estimateRelationsLength(int offset, byte[] source) {
		int len = source[offset];
		int ad = 1;
		boolean oneWord = false;
		if (len == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			len = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			ad = 3;
		}
		if (len < 0) {
			oneWord = true;
			len = -len;
		}
		if (oneWord) {
			return len*2 + 4;
		} else {
			return len * 5 +  ad;
		}
	}

	public static GrammarRelation[] decodeGrammarRelations(int offset,
			byte[] source) {
		int len = source[offset];
		boolean oneWord = false;
		if (len == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			len = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			offset++;
		}
		if (len < 0) {
			oneWord = true;
			len = -len;
		}

		GrammarRelation[] result = new GrammarRelation[len];
		offset++;
		if (oneWord) {
			byte b0 = source[offset++];
			byte b1 = source[offset++];
			byte b2 = source[offset++];
			int word = makeInt((byte) 0, b2, b1, b0);
			for (int a = 0; a < result.length; a++) {
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = new GrammarRelation(null, word, makeInt((byte)0,(byte) 0, rel1,rel));
			}
		} else {
			for (int a = 0; a < result.length; a++) {
				byte b0 = source[offset++];
				byte b1 = source[offset++];
				byte b2 = source[offset++];
				int word = makeInt((byte) 0, b2, b1, b0);
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = new GrammarRelation(null, word, makeInt((byte)0,(byte) 0, rel1,rel));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static AbstractRelation<MeaningElement>[] decodeMeaningRelations(int offset,
			byte[] source) {
		int len = source[offset];
		boolean oneWord = false;
		if (len == Byte.MIN_VALUE) {
			oneWord = false;
			offset++;
			len = makeInt((byte) 0, (byte) 0, source[offset + 1],
					source[offset]);
			offset++;
		}
		if (len < 0) {
			oneWord = true;
			len = -len;
		}

		AbstractRelation<MeaningElement>[] result = new AbstractRelation[len];
		offset++;
		if (oneWord) {
			byte b0 = source[offset++];
			byte b1 = source[offset++];
			byte b2 = source[offset++];
			int word = makeInt((byte) 0, b2, b1, b0);
			for (int a = 0; a < result.length; a++) {
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = createRelation(word, rel, rel1);
			}
		} else {
			for (int a = 0; a < result.length; a++) {
				byte b0 = source[offset++];
				byte b1 = source[offset++];
				byte b2 = source[offset++];
				int word = makeInt((byte) 0, b2, b1, b0);
				byte rel = source[offset++];
				byte rel1 = source[offset++];
				result[a] = createRelation(word, rel, rel1);				
			}
		}
		return result;
	}

	private static AbstractRelation<MeaningElement> createRelation(int word, byte rel, byte rel1) {
		int makeInt = makeInt((byte)0,(byte) 0, rel1,rel);
		if (makeInt>=MorphologicalRelation.MORPHOLOGICAL_OFFSET){
			return new MorphologicalRelation(null, word, makeInt);
		}
		return new SemanticRelation(null, word, makeInt);
	}

	public static int length(AbstractRelation<?>[] relations) {
		int w = -1;
		boolean oneWord = true;
		boolean customCase = false;
		if (relations.length >= Byte.MAX_VALUE) {
			oneWord = false;
			customCase = true;
		}
		for (int a = 0; a < relations.length; a++) {
			if (w != -1 && w != relations[a].conceptId) {
				oneWord = false;
			}
			if (w == -1) {
				w = relations[a].conceptId;
			}
		}
		if (relations.length == 0) {
			oneWord = false;
		}
		int delta = 0;
		if (oneWord) {
			delta = relations.length*2 + 3 + 1;
		} else {
			delta = relations.length * 5 + 1;
			if (customCase) {
				delta += 2;
			}
		}
		return delta;
	}

	public static byte[] encodeRelations(AbstractRelation<?>[] relations) {
		int w = -1;
		boolean customCase = false;
		boolean oneWord = true;
		if (relations.length >= Byte.MAX_VALUE) {
			oneWord = false;
			customCase = true;
		}
		for (int a = 0; a < relations.length; a++) {
			if (w != -1 && w != relations[a].conceptId) {
				oneWord = false;
			}
			if (w == -1) {
				w = relations[a].conceptId;
			}
		}
		if (relations.length == 0) {
			oneWord = false;
		}
		int delta = 0;
		if (oneWord) {
			delta = relations.length*2 + 3 + 1;
		} else {
			delta = relations.length * 5 + 1;
			if (customCase) {
				delta += 2;
			}
		}
		byte[] result = new byte[delta];
		encodeRelations(relations, w, oneWord, result, 0, customCase);		
		return result;
	}

	public static void encodeRelations(AbstractRelation<?>[] relations, int w,
			boolean oneWord, byte[] target, int position, boolean customCase) {
		
		if (customCase) {
			target[position++] = Byte.MIN_VALUE;
			target[position++] = int0(relations.length);
			target[position++] = int1(relations.length);
			for (AbstractRelation<?> q : relations) {
				int word = q.conceptId;
				if (q.relation>Short.MAX_VALUE){
					throw new IllegalStateException();
				}
				target[position++] = int0(word);
				target[position++] = int1(word);
				target[position++] = int2(word);
				target[position++] = (byte) int0(q.relation);
				target[position++] = (byte) int1(q.relation);
			}
		} else {
			target[position++] = (byte) (oneWord ? -relations.length
					: relations.length);
			if (oneWord) {
				target[position++] = int0(w);
				target[position++] = int1(w);
				target[position++] = int2(w);
				for (AbstractRelation<?> q : relations) {
					if (q.relation>Short.MAX_VALUE){
						throw new IllegalStateException();
					}
					target[position++] = (byte) int0(q.relation);
					target[position++] = (byte) int1(q.relation);
				}
			} else {
				for (AbstractRelation<?> q : relations) {
					int word = q.conceptId;
					target[position++] = int0(word);
					target[position++] = int1(word);
					target[position++] = int2(word);
					if (q.relation>Short.MAX_VALUE){
						throw new IllegalStateException();
					}
					target[position++] = (byte) int0(q.relation);
					target[position++] = (byte) int1(q.relation);
				}
			}
		}
	}

	@Override
	public int wordCount() {
		return wordsData.wordIdToOffset.length;
	}

	@Override
	public int conceptCount() {
		return wordsData.conceptIdToOffet.length;
	}

	@Override
	public int grammarFormsCount() {
		return relations.size();
	}

	@Override
	public MeaningElement getConceptInfo(int conceptId) {
		int i = wordsData.conceptIdToOffet[conceptId];
		if (i < 0) {
			SenseElementHandle senseElementHandle = new SenseElementHandle(
					-i - 1, wordsData);
			return senseElementHandle.getConcepts()[0];
		}
		return new SeparateConceptHandle(i, wordsData);
	}

	@Override
	public TextElement getWordElement(int wordId) {
		int i = wordsData.wordIdToOffset[wordId];
		if (i == Integer.MIN_VALUE) {
			return null;
		}
		if (i < 0) {
			return new WordSequenceHandle(-i - 1, this.wordsData);
		}
		return new WordHandle(i - 1, wordsData);
	}

	public TextElement getWordElement(String word) {
		int dataAddress2 = wordsData.getDataAddress2(word);			
		if (dataAddress2 >= 0) {
			byte[] buffer = wordsData.buffer();
			int makeInt = StringCoder.makeInt(buffer[dataAddress2 + 2],
					buffer[dataAddress2 + 1], buffer[dataAddress2]);
			return getWordElement(makeInt);
		}
		return null;
	}

	@Override
	public GrammarRelation[] getPossibleGrammarForms(String wordForm) {
		GrammarRelation[] wordRelations = relations.get(wordForm);
		if (wordRelations != null) {
			for (AbstractRelation<?> q : wordRelations) {
				GrammarRelation g = (GrammarRelation) q;
				g.setOwner(this);
			}
		} else {
			TextElement wordElement = getWordElement(wordForm);
			if (wordElement != null) {
				return new GrammarRelation[] { new GrammarRelation(this,
						wordElement.id(), 0) };
			}
		}
		return wordRelations;
	}

	@Override
	public TextElement[] getPossibleContinuations(TextElement startOfSequence) {
		int id = startOfSequence.id();
		if (sequences.containsKey(id)) {
			int addr = sequences.get(id);
			int count = store[addr];
			if (count < 0) {
				addr++;
				count = StringCoder.makeInt((byte) 0, store[addr + 1],
						store[addr]);
				addr++;
				addr++;
			} else {
				addr++;
			}
			TextElement[] result = new TextElement[count];
			int a = 0;
			while (a < count) {
				int makeInt = StringCoder.makeInt(store[addr + 2],
						store[addr + 1], store[addr]);
				result[a] = getWordElement(makeInt);
				a++;
				addr += 3;
			}
			return result;
		}
		return null;
	}

	@Override
	public boolean hasContinuations(TextElement te) {
		int id = te.id();
		if (sequences.containsKey(id)) {
			return true;
		}
		return false;
	}
}