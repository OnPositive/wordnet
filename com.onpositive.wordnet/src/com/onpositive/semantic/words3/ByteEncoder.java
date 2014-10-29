package com.onpositive.semantic.words3;

public class ByteEncoder implements IEncoder<Byte> {

	@Override
	public Byte decodeValue(byte[] buffer, int addr) {
		return buffer[addr];
	}

	@Override
	public byte[] encodeValue(Byte value) {
		return new byte[]{value};
	}

	@Override
	public int getDataSize(byte[] byteBuffer, int i) {
		return 1;
	}
	
}
