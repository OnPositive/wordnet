package com.onpositive.wordnet.viewer.views;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.ui.workbench.elements.XMLView;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.composite.CompositeWordnet;
import com.onpositive.text.analysis.IToken;
import com.onpositive.text.analysis.syntax.ClauseToken;
import com.onpositive.text.analysis.syntax.SyntaxParser;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class WordFormView extends XMLView {

	public WordFormView() {
		super("dlf/wordview.dlf");
		wn = new CompositeWordnet();
		wn.add(WordFormView.class.getResourceAsStream("numerics.xml"));
		wn.add(WordFormView.class.getResourceAsStream("dimensions.xml"));
		wn.add(WordFormView.class.getResourceAsStream("modificator-adverb.xml"));
		wn.add(WordFormView.class.getResourceAsStream("prepositions.xml"));
		wn.add(WordFormView.class.getResourceAsStream("conjunctions.xml"));
		wn.prepare();
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

	public List parse(String text) {
		if (text != null) {
			StringWriter ss=new StringWriter();
			PrintWriter ww=new PrintWriter(ss);
			List<IToken> process = process(text);
			if (process!=null){
				for (IToken q:process){
					
					printTokens(ww, q.getChildren());
				}
			}
			ww.close();
			return process;
		}
		return new ArrayList();
	}
	public String parse2(String text) {
		if (text != null) {
			StringWriter ss=new StringWriter();
			PrintWriter ww=new PrintWriter(ss);
			List<IToken> process = process(text);
			if (process!=null){
				for (IToken q:process){
					
					printTokens(ww, q.getChildren());
				}
			}
			ww.close();
			return ss.toString();
		}
		return "";
	}

	private static final Set<Class<?>> prinTreeClasses = new HashSet<Class<?>>(
			Arrays.asList(SyntaxToken.class, ClauseToken.class));
	private static final String childOffStr = "  ";
	private CompositeWordnet wn;

	public static String printToken(IToken token, int off) {

		StringBuilder offsetBld = new StringBuilder();
		for (int i = 0; i < off; i++) {
			offsetBld.append(" ");
		}
		String offStr = offsetBld.toString();

		StringBuilder bld = new StringBuilder();

		bld.append(offStr);
		bld.append(TokenTypeResolver.getResolvedType(token));

		if (prinTreeClasses.contains(token.getClass())) {
			SyntaxToken st = (SyntaxToken) token;
			SyntaxToken mainGroup = st.getMainGroup();
			List<IToken> children = token.getChildren();
			bld.append("(");
			if (token.getType() == IToken.TOKEN_TYPE_CLAUSE) {
				ClauseToken ct = (ClauseToken) token;
				{
					bld.append("\n");
					SyntaxToken subject = ct.getSubject();
					String childStr = subject != null ? printToken(subject,
							off + 2).trim() : "no subject";
					bld.append(offStr).append(childOffStr).append("<subject>");
					bld.append(childStr);
				}
				{
					bld.append("\n");
					SyntaxToken predicate = ct.getPredicate();
					String childStr = predicate != null ? printToken(predicate,
							off + 2).trim() : "no predicate";
					;
					bld.append(offStr).append(childOffStr)
							.append("<predicate>");
					bld.append(childStr);
				}
			} else {
				for (int i = 0; i < children.size(); i++) {
					bld.append("\n");
					IToken ch = children.get(i);
					String childStr = printToken(ch, off + 2);
					if (ch == mainGroup) {
						bld.append(offStr).append(childOffStr).append("<main>");
						childStr = childStr.trim();
					}
					bld.append(childStr);
				}
			}
			bld.append("  )");
		} else {
			bld.append(" ").append(token.getStringValue());
		}
		String result = bld.toString();
		return result;
	}

	protected static void printTokens(PrintWriter st,List<IToken> processed) {

		st.println();
		st.println("-----");

		if (processed == null || processed.isEmpty()) {
			return;
		}

		int l = ("" + processed.get(processed.size() - 1).getEndPosition())
				.length();

		for (IToken t : processed) {
			st.format("%0" + l + "d", t.getStartPosition());
			st.print("-");
			st.format("%0" + l + "d", t.getEndPosition());
			st.println(" " + printToken(t, l + l + 2).trim());// TokenTypeResolver.getResolvedType(t)
																		// + " "
																		// +
																		// t.getStringValue());
		}
	}

	protected List<IToken> process(String str) {
		return new SyntaxParser(wn).parse(str);
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
				mdl.add(new WordModel((MeaningElement) qa,
						((MeaningElement) qa).getOwner()));
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
