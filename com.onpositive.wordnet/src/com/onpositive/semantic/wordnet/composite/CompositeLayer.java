package com.onpositive.semantic.wordnet.composite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.words2.AbstractRelationTarget;
import com.onpositive.semantic.words3.MetaLayer;

public class CompositeLayer<T> extends MetaLayer<T>{

	MetaLayer<T>first;
	public CompositeLayer(Class<T> type, String id, String caption,
			MetaLayer<T> first, MetaLayer<T> second) {
		super(type, id, caption);
		this.first = first;
		this.second = second;
	}

	MetaLayer<T>second;
	private CompositeWordnet qq;
	
	public CompositeLayer(MetaLayer<T>first,MetaLayer<T>second,CompositeWordnet qq) {
		super(first!=null?first.getType():second.getType(), first!=null?first.getId():second.getId(),
				first!=null?first.getCaption():second.getCaption());
		this.first=first;
		this.qq=qq;
		this.second=second;
	}

	@Override
	public T getValue(int meaningId) {
		CompositeMeaning conceptInfo = (CompositeMeaning) qq.getConceptInfo(meaningId);
		if(conceptInfo.original instanceof AbstractRelationTarget){
			return second!=null?second.getValue(conceptInfo.original.id()):null;
		}
		T value = first!=null?first.getValue(conceptInfo.original.id()):null;
		return value;
		
	}

	@Override
	public void putValue(int meaningId, T value) {
		first.putValue(meaningId, value);
		second.putValue(meaningId, value);
	}

	@Override
	public void removeValue(int meaningId) {
		first.removeValue(meaningId);
		second.removeValue(meaningId);
	}

	@Override
	public boolean hasValue(int id) {
		return first.hasValue(id)&&second.hasValue(id);
	}

	@Override
	public int[] getAllIds() {
		IntOpenHashSet ss=new IntOpenHashSet();
		ss.add(first.getAllIds());
		ss.add(second.getAllIds());
		return ss.toArray();
	}

	@Override
	protected void store(DataOutputStream stream) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void load(DataInputStream stream) throws IOException {
		throw new UnsupportedOperationException();
	}

}
