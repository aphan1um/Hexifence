package core;

public class Main {
	private static final int DIM = 2;
	
	public static void main(String[] args) {
		Board b = new Board(DIM);
		
		b.occupyEdge(1, 0, true);
		b.occupyEdge(2, 0, true);
		b.occupyEdge(2, 2, true);
		b.occupyEdge(4, 1, true);
		System.out.println(b.toString());
		System.out.println(b.rotateBoard().toString());
	}

}
