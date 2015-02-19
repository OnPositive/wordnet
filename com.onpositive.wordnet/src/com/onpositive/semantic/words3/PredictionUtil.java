package com.onpositive.semantic.words3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.suggestions.GuessedGrammarRelation;
import com.onpositive.semantic.words3.suggestions.GuessedTextElement;

public class PredictionUtil {
	
	private List<String> basesList;
	private List<String> endingsList;
	
	private static PredictionUtil instance;
	private ReadOnlyWordNet wordNet;
	
	private PredictionUtil() {
		initialize();
	}
	
	public static synchronized PredictionUtil getInstance() {
		if (instance == null) {
			instance = new PredictionUtil();
		}
		return instance;
	}
	
	private void initialize() {
		try {
			wordNet = new ReadOnlyMapWordNet(new DataInputStream(new BufferedInputStream(new FileInputStream("D:/tmp/rwnet.dat")))); //XXX absolute path
			String[] grammarKeys = wordNet.getAllGrammarKeys();
			MultiHashMap<String, String> formsMap = new MultiHashMap<String, String>();
			for (String key : grammarKeys) {
				GrammarRelation[] forms = wordNet.getPossibleGrammarForms(key);
				for (GrammarRelation grammarRelation : forms) {
					String basicForm = grammarRelation.getWord().getBasicForm();
					formsMap.put(basicForm, key);
				}
			}
			Set<String> endings = new HashSet<String>();
			Set<String> bases = fillBases(formsMap, endings);
			basesList = new ArrayList<String>(bases);
			Collections.sort(basesList);
			endingsList = new ArrayList<String>(endings);
			Collections.sort(endingsList);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Set<String> fillBases(
			MultiHashMap<String, String> formsMap, Set<String> endings) {
		Set<String> bases = new LinkedHashSet<String>();
		Set<?> keys = formsMap.keySet();
		for (Object key: keys) {
			String word = key.toString().replace('ё','е');
			Collection<String> forms = formsMap.get(key);
			int endingStart = 0;
			outer:for (int i = 0; i < word.length(); i++) {
				char current = word.charAt(i);
				for (String string : forms) {
					if (string.length() <= i || string.charAt(i) != current) {
						endingStart = i;
						break outer;
					}
				}
			}
			if (endingStart < 3) {
				continue;
			}
			String base = word.substring(0, endingStart);
			bases.add(reverse(base));
			
			for (String string : forms) {
				if (string.length() > endingStart) {
					String ending = string.substring(endingStart);
					ending = ending.replace('ё','е');
					endings.add(reverse(ending));
				}
			}
		}
		return bases;
	}

	private String reverse(String base) {
		StringBuilder builder = new StringBuilder(base.length());
		for (int i = base.length() - 1; i >= 0; i--) {
			builder.append(base.charAt(i));
		}
		return builder.toString();
	}

	public GrammarRelation[] testSearch(String word) {
		String reversedWord = reverse(word);
		String[] endingsArray = endingsList.toArray(new String[0]);
		List<String> suitableEndings = new ArrayList<String>();
		for (int i = 0; i < reversedWord.length() - 2; i++) {
			String guessedEnding = reversedWord.substring(0, i+1);
			int pos = Arrays.binarySearch(endingsArray, guessedEnding);
			if (pos > 0) {
				suitableEndings.add(guessedEnding);
			} else {
				break;
			}
		}
		
		Collections.sort(suitableEndings, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
		});
		List<GrammarRelation> resList = new ArrayList<GrammarRelation>();
		for (String ending : suitableEndings) {
			String reversedBase = reverse(word.substring(0,word.length() - ending.length()));
			int baseIdx = Collections.binarySearch(basesList, reversedBase);
			String guessedBase;
			if (baseIdx >= 0) {
				guessedBase = word.substring(0,word.length() - ending.length());
				GrammarRelation[] forms = wordNet.getPossibleGrammarForms(guessedBase + reverse(ending));
				resList.addAll(Arrays.asList(forms));
			} else {
				int idx = -baseIdx - 1;
				resList.addAll(Arrays.asList(guessWord(basesList, wordNet, ending, reversedBase, idx - 1)));
				resList.addAll(Arrays.asList(guessWord(basesList, wordNet, ending, reversedBase, idx)));
				resList.addAll(Arrays.asList(guessWord(basesList, wordNet, ending, reversedBase, idx + 1)));
			}
		}
		return resList.toArray(new GrammarRelation[0]);
	}

	private GrammarRelation[] guessWord(List<String> basesList,
			ReadOnlyWordNet wordNet, String ending, String reversedBase, int idx) {
		String guessedBase = basesList.get(idx);
		int common = 0;
		for (int i = 0; i < Math.min(guessedBase.length(), reversedBase.length()); i++) {
			if (guessedBase.charAt(i) == reversedBase.charAt(i)) {
				common++;
			} else {
				break;
			}
		}
		GrammarRelation[] possibleGrammarForms = wordNet.getPossibleGrammarForms(reverse(guessedBase) + reverse(ending));
		if (possibleGrammarForms != null) {
//			String basicForm = possibleGrammarForms[0].getWord().getBasicForm();
			if (possibleGrammarForms!=null&&possibleGrammarForms.length>0){
				GuessedGrammarRelation[] gs=new GuessedGrammarRelation[possibleGrammarForms.length];
				int j=0;
				for (GrammarRelation q:possibleGrammarForms){
					TextElement word = q.getWord();
					if (word==null){
						continue;
					}
					String basicForm = word.getBasicForm();
					if (basicForm.length()<5){
						if (Grammem.PartOfSpeech.VERB.mayBeThisPartOfSpech(possibleGrammarForms)){
							return new GrammarRelation[0]; 
						}
					}
					String guessedEnding = basicForm.substring(guessedBase.length()); 
					String guessedWord = reverse(reversedBase) + guessedEnding;
					GuessedTextElement ts=new GuessedTextElement(wordNet, guessedWord);
					gs[j++]=new GuessedGrammarRelation(wordNet, q.conceptId, q.relation, ts);
				}
				return gs;
			}
		}
		return new GrammarRelation[0];
	}
}
