package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntDoubleOpenHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;

public class IntIntLayer extends MetaLayer<Integer>{

	protected IntIntOpenHashMap map;
	
	public IntIntLayer(String id, String caption) {
		super(Integer.class, id, caption);
	}

	@Override
	public Integer getValue(int meaningId) {
		if(!map.containsKey(meaningId)){
			return null;
		}
		int d = map.get(meaningId);
		return d;
	}

	@Override
	public boolean hasValue(int id) {
		return map.containsKey(id);
	}

	@Override
	public int[] getAllIds() {
		return map.keys().toArray();
	}

	@Override
	protected void store(DataOutputStream stream) throws IOException {
		stream.writeInt(map.size());
		for(int k=0;k<map.allocated.length;k++){
			if(map.allocated[k]){
				stream.writeInt(map.keys[k]);
				stream.writeInt(map.values[k]);
			}
		}
	}

	@Override
	protected void load(DataInputStream stream) throws IOException {
		int sz=stream.readInt();
		map=new IntIntOpenHashMap(sz*3/2);
		for(int a=0;a<sz;a++){
			map.put(stream.readInt(), stream.readInt());
		}
	}
}