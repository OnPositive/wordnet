package com.onpositive.semantic.words3;


public interface IEncoder<T> {
	public T decodeValue(byte[] buffer, int addr);

	public byte[] encodeValue(T value);

	public int getDataSize(byte[] buffer, int i);
}
