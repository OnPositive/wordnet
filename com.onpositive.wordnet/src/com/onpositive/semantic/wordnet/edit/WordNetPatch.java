package com.onpositive.semantic.wordnet.edit;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;

public class WordNetPatch {

	protected ArrayList<AbstractOperation>toExecute=new ArrayList<WordNetPatch.AbstractOperation>();
	
	public void execute(IWordNetEditInterface ei){
		for (AbstractOperation op:toExecute){
			op.execute(ei);
		}
	}
	
	protected static abstract class AbstractOperation {
		protected abstract void execute(IWordNetEditInterface net);

		public abstract Node append(Document appendChild) ;
	}
	static final String WC = "wc";

	public static class WordOperation extends AbstractOperation {
		
		public final String word;
		boolean removal;

		public WordOperation(String word, boolean removal) {
			super();
			this.word = word;
			this.removal = removal;
		}

		public WordOperation(Node item) {
			NamedNodeMap attributes = item.getAttributes();
			this.word=attributes.getNamedItem("w").getNodeValue();
			this.removal=Boolean.parseBoolean(attributes.getNamedItem("removal").getNodeValue());
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			if (removal) {
				net.removeWord(word);
			} else {
				net.registerWord(word);
			}
		}

		@Override
		public Node append(Document appendChild) {
			String tagName = WC;
			Element createElement = appendChild.createElement(tagName);
			createElement.setAttribute("w", word);
			if (removal){
				createElement.setAttribute("removal",""+removal);
			}
			return createElement;
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
		
		public RelationOperation(Node item) {
			NamedNodeMap attributes = item.getAttributes();
			this.from=attributes.getNamedItem("f").getNodeValue();
			this.to=attributes.getNamedItem("t").getNodeValue();
			this.removal=Boolean.parseBoolean(attributes.getNamedItem("removal").getNodeValue());
			Node namedItem = attributes.getNamedItem("r");
			this.code=parseCode(namedItem.getNodeValue());
		}

		protected abstract int parseCode(String nodeValue) ;

		public abstract String getTag();
		
		@Override
		public Node append(Document appendChild) {
			Element createElement = appendChild.createElement(getTag());
			createElement.setAttribute("f", from);
			createElement.setAttribute("t", to);
			if (removal){
				createElement.setAttribute("removal",""+removal);
			}
			createElement.setAttribute("r", getRelationString(code));
			return createElement;
		}

		protected abstract String getRelationString(int code);
	}
	private static final String SC = "sc";
	
	public static class SemanticRelationOperation extends RelationOperation {

		
		@Override
		protected int parseCode(String nodeValue) {
			Class<SemanticRelationOperation> class1 = SemanticRelationOperation.class;
			return parse(nodeValue, class1);
		}
		

		public SemanticRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
		}

		public SemanticRelationOperation(Node item) {
			super(item);
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			if (removal) {
				TextElement from = getOrCreate(net, this.from);
				TextElement to = getOrCreate(net, this.to);
				SemanticRelation tt = new SemanticRelation(net.getWordNet(),
						to.id(), code);
				SemanticRelation tt1 = new SemanticRelation(net.getWordNet(),
						from.id(), backlink(code));
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

		@Override
		public String getTag() {
			return SC;
		}

		@Override
		protected String getRelationString(int code) {
			Class<SemanticRelationOperation> class1 = SemanticRelationOperation.class;
			return toStringFromCode(code, class1);
		}

		

	}

	 static final String MC = "mc";
	public static class MorphologicalRelationOperation extends
			RelationOperation {

		

		public MorphologicalRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
		}

		public MorphologicalRelationOperation(Node item) {
			super(item);
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			
			if (removal) {
				TextElement from = getOrCreate(net, this.from);
				TextElement to = getOrCreate(net, this.to);
				MorphologicalRelation tt = new MorphologicalRelation(net.getWordNet(),
						to.id(), code);
				MorphologicalRelation tt1 = new MorphologicalRelation(net.getWordNet(),
						from.id(), code+MorphologicalRelation.BACK_LINK_OFFSET);
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

		@Override
		public String getTag() {
			return MC;
		}

		@Override
		protected String getRelationString(int code) {
			Class<MorphologicalRelation> class1 = MorphologicalRelation.class;
			return toStringFromCode(code, class1);
		}

		@Override
		protected int parseCode(String nodeValue) {
			Class<MorphologicalRelation> class1 = MorphologicalRelation.class;
			return parse(nodeValue, class1);
		}

	}

	static final String GC = "gc";
	public static class GrammarRelationOperation extends RelationOperation {

		
		public final String grammems;

		public GrammarRelationOperation(String from, String to,
				boolean removal, String grammems) {
			super(from, to, removal, 0);
			this.grammems=grammems;
		}

		public GrammarRelationOperation(Node item) {
			super(item);
			this.grammems=item.getAttributes().getNamedItem("r").getNodeValue();
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			TextElement to = getOrCreate(net, this.to);
			LinkedHashSet<Grammem> code=getCode(grammems,net);
			if (removal){
				
				net.removeGrammarRelation(from,to,code);
			}
			else{
				net.addGrammarRelation(from, to, code);
			}
		}

		private LinkedHashSet<Grammem> getCode(String grammems2, IWordNetEditInterface net) {
			String[] split = grammems.split(",");
			LinkedHashSet<Grammem>ms=new LinkedHashSet<Grammem>();
			for (String s:split){
				Grammem grammem = Grammem.get(s.trim());
				if (grammem==null){
					throw new IllegalStateException("unknown grammem");
				}
				ms.add(grammem);
			}
			
			return ms;
		}

		@Override
		public String getTag() {
			return GC;
		}

		@Override
		protected String getRelationString(int code) {
			return grammems;
		}

		@Override
		protected int parseCode(String nodeValue) {
			return 0;
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
	static int parse(String nodeValue, Class<?> class1) {
		Field[] fields = class1.getFields();
		for (Field f:fields){
			if (Modifier.isStatic(f.getModifiers())){
				if (f.getType()==int.class){
					if (f.getName().toLowerCase().equals(nodeValue)){
						try{
						return f.getInt(null);
						}catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
					
				}
			}
		}
		throw new IllegalArgumentException("Unknown relation:"+nodeValue);
	}
	static String toStringFromCode(int code, Class<?> class1) {
		Field[] fields = class1.getFields();
		for (Field f:fields){
			if (Modifier.isStatic(f.getModifiers())){
				if (f.getType()==int.class){
					try{
					if (f.getInt(null)==code){
						return f.getName().toLowerCase();
					}
					}catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
		return null;
	}
	public WordNetPatch parse(Reader reader) throws Exception{
		Document parse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(reader));
		WordNetPatch patch=new WordNetPatch();
		NodeList childNodes = parse.getDocumentElement().getChildNodes();
		int length = childNodes.getLength();
		for (int a=0;a<length;a++){
			Node item = childNodes.item(a);
			AbstractOperation op=createCommand(item);
			if (op==null){
				throw new IllegalArgumentException("unknown tag:"+item.getNodeName());
			}
			else{
				toExecute.add(op);
			}
		}
		return patch;	
	}
	
	private AbstractOperation createCommand(Node item) {
		if (item instanceof Element){
			Element el=(Element) item;
			if (el.getLocalName().equals(WC)){
				return new WordOperation(item);
			}
			if (el.getLocalName().equals(SC)){
				return new SemanticRelationOperation(item);
			}
			if (el.getLocalName().equals(MC)){
				return new MorphologicalRelationOperation(item);
			}
			if (el.getLocalName().equals(GC)){
				return new GrammarRelationOperation(item);
			}
		}
		return null;
	}
	public void store(Writer wr) throws Exception{
		Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element createElement = newDocument.createElement("wnc");
		Node appendChild = newDocument.appendChild(createElement);
		for (AbstractOperation o:this.toExecute){
			Node append = o.append(newDocument);
			appendChild.appendChild(append);
		}
		Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
		newTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
		newTransformer.transform(new DOMSource(newDocument), new StreamResult(wr));
	}
}
