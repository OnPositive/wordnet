package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;

public abstract class MetaLayer<T> {

	protected final Class<T>type;
	protected final String id;
	protected final String caption;
	
	public MetaLayer(Class<T> type, String id,String caption) {
		super();
		this.type = type;
		this.id = id;
		this.caption=caption;
	}

	public Class<T>getType(){return type;}
	
	public String getId(){
		return id;
	}
	
	public final T getValue(MeaningElement el){
		if (el==null){
			return null;
		}
		return getValue(el.id());
	}
	
	public abstract T getValue(int meaningId);
	public abstract void putValue(int meaningId,T value);
	public abstract void removeValue(int meaningId);
	public abstract boolean hasValue(int id);
	public abstract int[]getAllIds();
	
	protected abstract void store(DataOutputStream stream) throws IOException;
	protected abstract void load(DataInputStream stream) throws IOException;

	public T getValue(TextElement textElement) {
		MeaningElement[] concepts = textElement.getConcepts();
		for (MeaningElement q:concepts){
			T value = getValue(q.id());
			if (value!=null){
				return value;
			}
		}
		return null;
	}

	public String getCaption() {
		return caption;
	}
}
