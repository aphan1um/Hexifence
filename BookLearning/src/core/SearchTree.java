package core;

import java.util.ArrayList;
import java.util.List;

public class SearchTree {
    private Node root;

    public SearchTree(Board initState) {
        root = new Node(initState, null);
    }
    
    public Node getRoot() {
    	return root;
    }

    public static class Node {
        private Board state;
        private Node parent;
        private List<Node> children;
        
        private int minimax;
        
        public Node(Board board, Node parent) {
        	this.state = board;
        	this.parent = parent;
        	
        	this.children = new ArrayList<Node>();
        }
        
        public Board getState() {
        	return state;
        }
        
        public Node getParent() {
        	return parent;
        }
        
        public List<Node> getChildren() {
        	return children;
        }

        public void setMiniMax(int value) {
        	this.minimax = value;
        }
        
        public Node addChild(Board board) {
        	Node new_node = new Node(board, this);
        	
        	children.add(new_node);
        	return new_node;
        }
    }
}