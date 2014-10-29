package com.onpositive.semantic.words3;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.ZipFile;

import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.words2.SimpleWordNet;
import com.onpositive.semantic.words3.hds.StringStorage;
import com.onpositive.semantic.words3.hds.StringToDataHashMap;
import com.onpositive.semantic.words3.hds.StringTrie;

public class ReadOnlyTrieWordNet extends ReadOnlyWordNet {

	public ReadOnlyTrieWordNet(ZipFile zipFile) throws IOException {
		relations = createStorage(10);
		wordsData = new WordStore(5, 5);
		DataInputStream inputStream = new DataInputStream(zipFile.getInputStream(zipFile.getEntry("trie")));
		relations.read(inputStream);
		inputStream.close();
		inputStream = new DataInputStream(zipFile.getInputStream(zipFile.getEntry("words.dat")));
		wordsData.read(inputStream);
		inputStream.close();
		inputStream = new DataInputStream(zipFile.getInputStream(zipFile.getEntry("store")));
		int size = inputStream.readInt();
		this.store=new byte[size];
		inputStream.readFully(this.store);
		sequences=StringToDataHashMap.readMap(inputStream);
		readGrammems(inputStream);
		inputStream.close();
	}
	
	@Override
	protected void initRelations(SimpleWordNet original) {
		super.initRelations(original);
		((StringTrie<GrammarRelation[]>) relations).commit();
	}

	@Override
	protected StringStorage<GrammarRelation[]> createStorage(int size) {
		return new TrieGrammarStore();
	}
	
	public static ReadOnlyTrieWordNet load(ZipFile zipFile) throws IOException {
		return new ReadOnlyTrieWordNet(zipFile);
	}

}
