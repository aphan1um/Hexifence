package hex_test1;

public class Main {
	private static final int DIM = 2;
	
	public static void main(String[] args) {
		Board b = new Board(DIM);
		
		b.occupyEdge(1, 0, true);
		b.occupyEdge(2, 0, true);
		b.occupyEdge(2, 2, true);
		b.occupyEdge(4, 1, true);
		System.out.println(b.toString());
		System.out.println(b.rotateBoard(2).toString());
		
		//System.out.println(b.getBitLocation(6, 3));
		//b.occupyEdge(5, 4, true);
		//System.out.println(b.edge_data.toString());
	}

}
