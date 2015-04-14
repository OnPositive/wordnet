package com.onpositive.wordnet.viewer.views;

import com.onpositive.semantic.ui.workbench.elements.XMLView;
import com.onpositive.semantic.wikipedia2.catrelations2.MergedGraph;

public class GraphView extends XMLView{

	public GraphView() {
		super("dlf/graph.dlf");
	}
	
	static class Node{
		
		MergedGraph
		
		String getName(){
			return title;			
		}
		
		boolean isConfirmed;
		
		public Node[] getChildren(){
			return null;			
		}
	}
	
}

