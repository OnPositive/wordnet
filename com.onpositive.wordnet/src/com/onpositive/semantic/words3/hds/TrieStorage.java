package com.onpositive.semantic.words3.hds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.onpositive.semantic.words3.hds.StringTrie.TrieBuilder;

public class TrieStorage<T> implements IStringToDataStorage<T> {
	
	protected StringTrie<T> store = new StringTrie<T>();
	private TrieBuilder builder;

	@Override
	public T get(String string) {
		return store.find(string);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int store(String string, T data) {
		if (builder == null) {
			builder = store.newBuilder();
		}
		builder.append(string, data);
		return 0;
	}

	public void read(InputStream stream) throws IOException {
		store.read(stream);
	}

	public void write(OutputStream stream) throws IOException {
		store.write(stream);
	}

	@Override
	public void commit() {
		if (builder == null) {
			return;
		}
		store.commit(builder);
		builder = null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
