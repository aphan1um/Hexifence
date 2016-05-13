package aiproj.hexifence;
/*
 *   Piece:
 *      Define types of states that can appear on a board
 *      
 *   @author lrashidi
 *   Edited by Andy Phan (aphan1).
 *   
 */

public enum Piece {
    BLUE(1), 
    RED(2),
    DEAD(3),
    EMPTY(0),
    INVALID(-1);
    
    private int value;
    
    private Piece(int value) {
    	this.value = value;
    }
    
    public int getValue() {
    	return value;
    }
}
