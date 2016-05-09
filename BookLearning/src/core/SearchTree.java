package core;

import java.util.ArrayList;
import java.util.List;

public class SearchTree {
    private Node root;

    public SearchTree(Board initState, boolean isMyTurn) {
        root = new Node(initState, null, isMyTurn);
    }
    
    public Node getRoot() {
    	return root;
    }

    public static class Node {
        private Board state;
        private Node parent;
        private List<Node> children;
        private boolean myTurn;
        
        private int minimax;
        
        public Node(Board board, Node parent, boolean isMyTurn) {
        	this.state = board;
        	this.parent = parent;
        	this.myTurn = isMyTurn;
        	
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
        
        public boolean isMyTurn() {
        	return myTurn;
        }
        
        public void setMiniMax(int value) {
        	this.minimax = value;
        }
        
        public Node addChild(Board board) {
        	Node new_node = new Node(board, this, !myTurn);
        	
        	children.add(new_node);
        	return new_node;
        }
    }
}