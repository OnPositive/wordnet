package com.onpositive.semantic.words3.hds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IStringToDataStorage<T> {

	public abstract T get(String string);

	public abstract int store(String string, T data);

	public abstract void read(InputStream is) throws IOException;

	public abstract void write(OutputStream ds) throws IOException;
	
	public abstract void commit();

	public abstract int size();

}