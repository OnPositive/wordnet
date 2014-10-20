package com.onpositive.semantic.words3.hds;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.CharByteOpenHashMap;
import com.onpositive.semantic.words3.GrammarRelationEncoder;
import com.onpositive.semantic.words3.IEncoder;

public class StringTrie<T> extends StringCoder {
	
	protected IEncoder<T> encoder;
	protected int count = 0;

	public StringTrie() {
	
	}

	public TrieBuilder newBuilder() {
		return new TrieBuilder();
	}

	public void commit(TrieBuilder builder) {
		this.byteBuffer = builder.store();
		this.byteToCharTable = builder.byteToCharTable;
		this.charToByteMap = builder.charToByteMap;
		this.count = builder.count;
	}

	public T find(String element) {
		return find(element, 0, 0);
	}

	private T findRecursive(String search, int position, int cAddress) {
		int length = search.length();
		int childCount = byteBuffer[cAddress];
		
		if (childCount == 0 && position < length) { //if we have no children, but still have chars to search - return null
			return null;
		}
		
		int i = cAddress + 1;
		int dataPos = i;
		if (childCount < 0) { //Skip associated data
			i = i + getDataSize(dataPos);
		}
		
		if (position==length){
			//we need to decode value;
			if (childCount<=0){
				if (childCount < 0) {
					i = i - getDataSize(dataPos);
				}
				return decodeValue(i);
			}
			return null;
		}
		char nextChar = search.charAt(position);
		if (childCount<0){
			childCount=-childCount;
		}
		int currentItem=0;
		l1:while (true) {
			byte b = byteBuffer[i];
			boolean onChar=false;
			if (b<0){
				b=(byte) -b;
				onChar=true;
			}
			char curentChar = byteToCharTable[b];
			if (curentChar == nextChar) {
				if (onChar){
					//read next address;
					i++;
					byte vl=byteBuffer[i];
					if (vl==Byte.MIN_VALUE){
						i+=4;
					}
					else{
						i++;
					}
					return findRecursive(search,position+1,i);
				}
				else{
					i++;
					int charIndex=position+1;
					
					while (true){
						b = byteBuffer[i++];
						if (b<0){
							b=(byte) -b;
							onChar=true;
						}
						curentChar = byteToCharTable[b];
						if (charIndex>=length|| curentChar!=search.charAt(charIndex)){
							//no we can skip to end of string and go next;
							while (byteBuffer[i++]>=0);//skip string
							if (currentItem==childCount-1){
								return null;
							}
							currentItem++;
							//decode address
							byte vl=byteBuffer[i];
							if (vl==Byte.MIN_VALUE){
								i = makeMultibyteShift(i);
								continue l1;
							}
							else{
								int next=vl;
								i=i+next;
								continue l1;
							}
						}
						else{
							charIndex++;
							if (charIndex==length){
								//we are at the end of string
								if (onChar){
									//end of constant segment
									if (byteBuffer[i] == Byte.MIN_VALUE) {
										return decodeValue(i+5);
									} else {  
										return decodeValue(i+2);
									}
								}
								return null;
							}
							if (onChar){
								//end of constant segment
								//read next address;
								byte vl=byteBuffer[i];
								if (vl==Byte.MIN_VALUE){
									i+=4;
								}
								else{
									i++;
								}
								return findRecursive(search,charIndex,i);			
							}
						}
					}
				}
			}
			else{
				if (currentItem==childCount-1 || childCount == 0){
					return null;
				}
				currentItem++;
				if (onChar){
					//read next address;
					i++;
					byte vl=byteBuffer[i];
					if (vl==Byte.MIN_VALUE){
						i = makeMultibyteShift(i);
						continue l1;
					}
					else{
						int next=vl;
						i=i+next;
						continue l1;
					}
				}
				else{
					i++;
					while (byteBuffer[i++]>=0);//skip string
					//decode address
					byte vl=byteBuffer[i];
					if (vl==Byte.MIN_VALUE){
						i = makeMultibyteShift(i);
						continue l1;
					}
					else{
						int next=vl;
						i=i+next;
						continue l1;
					}
					
				}
				//we need to read next sibling position;				
			}
		}
		
	}
	
	private T find(String search, int position, int cAddress) {
		int i = cAddress;
		outerLoop:while (position <= search.length()) {
			int childCount = byteBuffer[i];
			if (childCount == 0 && position < search.length()) { //if we have no children, but still have chars to search - return null
				return null;
			}
			i++;
			int dataPos = i;
			if (childCount < 0) { //Skip associated data
				i = i + getDataSize(dataPos);
			}
			
			int length = search.length();
			if (position==length){
				//we need to decode value;
				if (childCount<=0){
					if (childCount < 0) {
						i = i - getDataSize(dataPos);
					}
					return decodeValue(i);
				}
				return null;
			}
			char nextChar = search.charAt(position);
			if (childCount<0){
				childCount=-childCount;
			}
			int currentItem=0;
			l1:while (true) {
				byte b = byteBuffer[i];
				boolean onChar=false;
				if (b<0){
					b=(byte) -b;
					onChar=true;
				}
				char curentChar = byteToCharTable[b];
				if (curentChar == nextChar) {
					if (onChar){
						//read next address;
						i++;
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i+=4;
						}
						else{
							i++;
						}
						position++;
						continue outerLoop;
//						return find(search,position+1,i);
					}
					else{
						i++;
						int charIndex=position+1;
						
						while (true){
							b = byteBuffer[i++];
							if (b<0){
								b=(byte) -b;
								onChar=true;
							}
							curentChar = byteToCharTable[b];
							if (charIndex>=length|| curentChar!=search.charAt(charIndex)){
								//no we can skip to end of string and go next;
								while (byteBuffer[i++]>=0);//skip string
								if (currentItem==childCount-1){
									return null;
								}
								currentItem++;
								//decode address
								byte vl=byteBuffer[i];
								if (vl==Byte.MIN_VALUE){
									i = makeMultibyteShift(i);
									continue l1;
								}
								else{
									int next=vl;
									i=i+next;
									continue l1;
								}
							}
							else{
								charIndex++;
								if (charIndex==length){
									//we are at the end of string
									if (onChar){
										//end of constant segment
										if (byteBuffer[i] == Byte.MIN_VALUE) {
											return decodeValue(i+5);
										} else {  
											return decodeValue(i+2);
										}
									}
									return null;
								}
								if (onChar){
									//end of constant segment
									//read next address;
									byte vl=byteBuffer[i];
									if (vl==Byte.MIN_VALUE){
										i+=4;
									}
									else{
										i++;
									}
									position = charIndex;
									continue outerLoop;
//									return find(search,charIndex,i);			
								}
							}
						}
					}
				}
				else{
					if (currentItem==childCount-1 || childCount == 0){
						return null;
					}
					currentItem++;
					if (onChar){
						//read next address;
						i++;
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i = makeMultibyteShift(i);
							continue l1;
						}
						else{
							int next=vl;
							i=i+next;
							continue l1;
						}
					}
					else{
						i++;
						while (byteBuffer[i++]>=0);//skip string
						//decode address
						byte vl=byteBuffer[i];
						if (vl==Byte.MIN_VALUE){
							i = makeMultibyteShift(i);
							continue l1;
						}
						else{
							int next=vl;
							i=i+next;
							continue l1;
						}
						
					}
					//we need to read next sibling position;				
				}
			}
		}
		return null;
	}

	private int makeMultibyteShift(int i) {
		i++;
		int next=makeInt(byteBuffer[i+2], byteBuffer[i+1],byteBuffer[i]);
		i=i + 2 + next;
		return i;
	}
	
	protected int getDataSize(int i) {
		return encoder.getDataSize(byteBuffer, i);
	}

	protected T decodeValue(int i) {
		return encoder.decodeValue(byteBuffer, i);
	}

	public class TrieBuilder extends StringCoder {

		public class TrieNode {
			char ch;
			String s = "";
			ArrayList<TrieNode> children = new ArrayList<TrieNode>();
			T associatedData;

			public TrieNode getOrCreateChild(char c) {
				for (TrieNode q : children) {
					if (q.ch == c) {
						return q;
					}
				}
				TrieNode newNode = new TrieNode();
				newNode.ch = c;
				children.add(newNode);
				return newNode;
			}

			protected void store(ByteArrayList list) {
				int size = children.size();
				if (size > 127) {
					System.out.println("PREVED");
				}
				if (associatedData != null) {
					size = -size;
				}
				list.add((byte) size);
				if (associatedData != null) {
					byte[] vl = encodeValue(associatedData);
					list.add(vl);
				}
				for (TrieNode q : children) {
					byte[] values = q.store();
					q.encodeHeader(list);
					encodeLength(values.length + 1, list);
					list.add(values);
				}
			}

			private void encodeLength(int length, ByteArrayList list) {
				if (length < 128) {
					list.add(int0(length));
				} else {
					list.add(Byte.MIN_VALUE);
					list.add(int0(length));
					list.add(int1(length));
					list.add(int2(length));
				}
			}

			private void encodeHeader(ByteArrayList headers) {
				if (s.length() == 0) {
					s += ch;
				}
				byte[] rs = new byte[s.length()];
				encodeString(s, rs);
				headers.add(rs);
				
			}

			public byte[] store() {
				ByteArrayList b = new ByteArrayList();
				store(b);
				return b.toArray();
			}

			public void optimize() {
				for (TrieNode q : children) {
					q.optimize();
				}
				if (children.size() == 1&&this!=rootNode&&this.associatedData==null) {
					TrieNode trieNode = children.get(0);
					this.associatedData=trieNode.associatedData;
					if (this.s.length()==0){
						this.s=""+ch;
					}
					if (trieNode.s.length() > 0) {
						this.s += trieNode.s;
					} else {
						this.s += trieNode.ch;
					}
					this.children = trieNode.children;
				}
			}
			@Override
			public String toString() {
				if (s.length() > 0) {
					return s;
				} else {
					return "" + ch;
				}
			}
		}

		protected TrieNode rootNode = new TrieNode();
		private int count = 0;

		public void append(String s, T value) {
			TrieNode t = rootNode;
			int length = s.length();
			for (int a = 0; a < length; a++) {
				char c = s.charAt(a);
				t = t.getOrCreateChild(c);
			}
			t.associatedData = value;
			count++;
		}

		public byte[] store() {
			rootNode.optimize();
			return rootNode.store();
		}
	}

	protected byte[] encodeValue(T associatedData2) {
		return encoder.encodeValue(associatedData2);
	}

	public void write(OutputStream stream) throws IOException {
		DataOutputStream dos = (stream instanceof DataOutputStream) ? (DataOutputStream) stream : new DataOutputStream(stream);
		dos.writeInt(usedBytes);
		ByteBuffer buffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(charToByteMap.keys));
		byte[] keys = new byte[buffer.limit()];
		buffer.get(keys);
		dos.writeInt(keys.length);
		dos.write(keys);
		dos.writeInt(charToByteMap.values.length);
		dos.write(charToByteMap.values);
		buffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(byteToCharTable));
		byte[] ba2 = new byte[buffer.limit()];
		buffer.get(ba2);
		dos.writeInt(ba2.length);
		dos.write(ba2);
		dos.writeInt(byteBuffer.length);
		dos.write(byteBuffer);
	}

	public void read(InputStream stream) throws IOException {
		DataInputStream dis =  (stream instanceof DataInputStream) ? (DataInputStream)stream : new DataInputStream(stream);
		usedBytes = dis.readInt();
		int length = dis.readInt();
		byte[] charToByteKeys = new byte[length];
		dis.read(charToByteKeys);
		CharBuffer buffer = Charset.forName("UTF-8").decode(ByteBuffer.wrap(charToByteKeys));
		char[] keys = new char[buffer.limit()];
		buffer.get(keys);
		length = dis.readInt();
		byte[] values = new byte[length];
		dis.read(values);
		charToByteMap = CharByteOpenHashMap.from(keys, values);
		length = dis.readInt();
		byte[] byteToChar = new byte[length];
		dis.read(byteToChar);
		buffer = Charset.forName("UTF-8").decode(ByteBuffer.wrap(byteToChar));
		byteToCharTable = new char[buffer.limit()];
		buffer.get(byteToCharTable);
		length = dis.readInt();
		byteBuffer = new byte[length];
		dis.read(byteBuffer);
	}

	public void setEncoder(IEncoder<T> encoder) {
		this.encoder = encoder;
	}

	public int size() {
		return count;
	}
}
