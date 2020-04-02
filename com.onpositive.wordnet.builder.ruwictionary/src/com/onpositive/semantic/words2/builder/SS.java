package com.onpositive.semantic.words2.builder;

import java.util.Arrays;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class SS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		SimpleWordNet ws=new SimpleWordNet();
//		new  RuCorpusParser().contribute(new File("D:/wordnet"), ws);
//		new WictionaryParser().contribute(new File("D:/wordnet"), ws);
//		ws.init();
//		ReadOnlyWordNet readOnlyWordNet = new ReadOnlyMapWordNet(
//				(SimpleWordNet) ws);
//		try {
//			readOnlyWordNet.store(new File("D:/se1/rwnet.dat"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		AbstractWordNet instance = WordNetProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms("ךאדדכטעü");
		System.out.println(Arrays.toString(possibleGrammarForms));
		System.out.println(instance.getAllGrammarKeys().length);
	}

}
