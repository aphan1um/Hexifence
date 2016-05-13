package core;

import java.util.ArrayList;
import java.util.List;

import aiproj.hexifence.Piece;

public class SearchTree {
    private Node root;

    public SearchTree(Board initState, Piece colorTurn) {
    	// ensure the player is either BLUE or RED from Piece enum
    	assert(colorTurn == Piece.BLUE || colorTurn == Piece.RED);
        root = new Node(initState, null, colorTurn);
    }
    
    public Node getRoot() {
    	return root;
    }

    public static class Node {
        private Board state;
        private Node parent;
        private List<Node> children;
        private Piece colorTurn;
        
        private int minimax;
        
        public Node(Board board, Node parent, Piece colorTurn) {
        	this.state = board;
        	this.parent = parent;
        	this.colorTurn = colorTurn;
        	
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
        
        public Piece getColorTurn() {
        	return colorTurn;
        }
        
        public void setMiniMax(int value) {
        	this.minimax = value;
        }
        
        public Node addChild(Board board) {
        	Node new_node = new Node(board, this,
        			(colorTurn == Piece.BLUE) ? Piece.RED : Piece.BLUE);
        	
        	children.add(new_node);
        	return new_node;
        }
    }
}