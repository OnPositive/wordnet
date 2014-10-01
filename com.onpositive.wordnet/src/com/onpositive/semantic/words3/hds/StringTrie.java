package com.onpositive.semantic.words3.hds;

import java.util.ArrayList;

import com.carrotsearch.hppc.ByteArrayList;

public abstract class StringTrie<T> extends StringCoder {

	public TrieBuilder newBuilder() {
		return new TrieBuilder();
	}

	public void commit(TrieBuilder builder) {
		this.byteBuffer = builder.store();
		this.byteToCharTable = builder.byteToCharTable;
		this.charToByteMap = builder.charToByteMap;
	}

	public T find(String element) {
		return find(element, 0, 0);
	}

	private T find(String search, int position, int cAddress) {
		int childCount = byteBuffer[cAddress];
		
		int i = cAddress + 1;
		if (childCount < 0) { //Skip associated data
			i = i + getDataSize(i);
		}
		
		int length = search.length();
		if (position==length){
			//we need to decode value;
			if (childCount<=0){
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
						i+=3;
					}
					else{
						i++;
					}
					return find(search,position+1,i);
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
								i++;
								int next=makeInt(byteBuffer[i+2], byteBuffer[i+1],byteBuffer[i]);
								i=i+next;
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
									return decodeValue(i+2);			
								}
								return null;
							}
							if (onChar){
								//end of constant segment
								return find(search,charIndex,i + 1);			
							}
						}
					}
				}
			}
			else{
				if (currentItem==childCount-1){
					return null;
				}
				currentItem++;
				if (onChar){
					//read next address;
					i++;
					byte vl=byteBuffer[i];
					if (vl==Byte.MIN_VALUE){
						i++;
						int next=makeInt(byteBuffer[i+2], byteBuffer[i+1],byteBuffer[i]);
						i=i+next;
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
						i++;
						int next=makeInt(byteBuffer[i+2], byteBuffer[i+1],byteBuffer[i]);
						i=i+next;
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
	
	protected abstract int getDataSize(int i);

	protected abstract T decodeValue(int i);

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
				if (length < 255) {
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

		public void append(String s, T value) {
			TrieNode t = rootNode;
			int length = s.length();
			for (int a = 0; a < length; a++) {
				char c = s.charAt(a);
				t = t.getOrCreateChild(c);
			}
			t.associatedData = value;
		}

		public byte[] store() {
			rootNode.optimize();
			return rootNode.store();
		}
	}

	protected abstract byte[] encodeValue(T associatedData2);

	public StringTrie() {

	}
}
