import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

// the class representing one of the chosen n-ominoes

public class Block implements Serializable{
	private static final long serialVersionUID = 8906889826578685991L;
	
	static HashMap<Integer, int[][]> posBlocks; // holds each blocks' pixel positions that define its shape
	static HashMap<Integer, ArrayList<Integer>> nonSymmetries; // holds the unique transformations of each block
	
	HashSet<OrderedPair> points; // the pixels this instance takes up on the tiling board
	OrderedPair[] pointArray; // the array of pixels that this instance takes up on the tiling board
	
	// designates the axis-aligned bounding box of the block
	int minx = Integer.MAX_VALUE;
	int miny = Integer.MAX_VALUE;
	int maxx = Integer.MIN_VALUE;
	int maxy = Integer.MIN_VALUE;
	
	int which; // the integer describing the shape of the block
	
	public Block(){
	}
	
	public Block(int which, int[] pos, int orientation, int anchor){
		this.which = which;
		if (posBlocks == null){
			initializeStatics();
		}
		
		// add the block's pixels to points
		int[][] t = posBlocks.get(which);
		
		points = new HashSet<>();
		
		for (int i=0; i<t.length; i++){
			points.add(new OrderedPair(t[i][0]-t[anchor][0],t[i][1]-t[anchor][1]));
		}
		
		// rotate in the case of orientation and compute its axis-aligned bounding box
		switch(orientation%4){
			case 0:
				for (OrderedPair p : points){
					if (orientation > 3) p.a = -p.a;
					
					p.a = p.a+pos[0];
					p.b = p.b+pos[1];
				
					if (p.a < minx) minx = p.a;
					if (p.a > maxx) maxx = p.a;
					
					if (p.b < miny) miny = p.b;
					if (p.b > maxy) maxy = p.b;
				}
				break;
			case 1:
				for (OrderedPair p : points){
					int temp = p.a;
					
					if (orientation > 3) temp = -temp;
					
					p.a = p.b+pos[0];
					p.b = -temp+pos[1];
				
					if (p.a < minx) minx = p.a;
					if (p.a > maxx) maxx = p.a;
					
					if (p.b < miny) miny = p.b;
					if (p.b > maxy) maxy = p.b;
				}
				break;
			case 2:
				for (OrderedPair p : points){
					int temp = p.a;
					
					if (orientation > 3) temp = -temp;
					
					p.a = -temp+pos[0];
					p.b = -p.b+pos[1];
					
					if (p.a < minx) minx = p.a;
					if (p.a > maxx) maxx = p.a;
					
					if (p.b < miny) miny = p.b;
					if (p.b > maxy) maxy = p.b;
				}
				break;
			case 3:
				for (OrderedPair p : points){
					int temp = p.a;
					
					if (orientation > 3) temp = -temp;
					
					p.a = -p.b+pos[0];
					p.b = temp+pos[1];
					
					if (p.a < minx) minx = p.a;
					if (p.a > maxx) maxx = p.a;
					
					if (p.b < miny) miny = p.b;
					if (p.b > maxy) maxy = p.b;
				}
		}
	}
	
	/* This initializes the static dictionary of blocks and their symmetries: n<4 are rotational symmetries s.t. degrees rotated is n*90,
	4<=n<8 are respectively the same rotational symmetries after being flipped over the points' b axes. 
	*/
	static public void initializeStatics() {
		posBlocks = new HashMap<>();
		nonSymmetries = new HashMap<Integer, ArrayList<Integer>>();
		
		int i = 0;
		int[][] toAdd = new int[][]{{0,0}}; //0
		
		nonSymmetries.put(0, new ArrayList<Integer>(Arrays.asList(0)));
		
		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{0,1},{1,1},{2,1},{2,0}}; //1
		
		nonSymmetries.put(1, new ArrayList<Integer>(Arrays.asList(0,1,2,3)));
		
		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{1,1},{1,-1},{2,-1}}; //2
		
		nonSymmetries.put(2, new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{0,1},{1,1},{2,0}}; //3
		
		nonSymmetries.put(3, new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{0,1},{1,1}}; //4

		nonSymmetries.put(4, new ArrayList<Integer>(Arrays.asList(0)));
		
		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{2,0},{0,1},{0,2}}; //5
		
		nonSymmetries.put(5, new ArrayList<Integer>(Arrays.asList(0,1,2,3)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{2,0},{3,0}}; //6
		
		nonSymmetries.put(6, new ArrayList<Integer>(Arrays.asList(0,1)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{-1,0},{-1,1},{0,-1},{1,-1}}; //7
		
		nonSymmetries.put(7, new ArrayList<Integer>(Arrays.asList(0,1,2,3)));
		
		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{1,1},{2,0},{3,0}}; //8
		
		nonSymmetries.put(8, new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{1,1},{1,2},{2,2}}; //9
		
		nonSymmetries.put(9, new ArrayList<Integer>(Arrays.asList(0,1,4,5)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{2,0},{3,0},{4,0}}; //10
		
		nonSymmetries.put(10, new ArrayList<Integer>(Arrays.asList(0,1)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{1,1},{2,0},{1,-1}}; //11
		
		nonSymmetries.put(11, new ArrayList<Integer>(Arrays.asList(0)));

		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{2,0},{1,1},{1,2}}; //12
		
		nonSymmetries.put(12, new ArrayList<Integer>(Arrays.asList(0,1,2,3)));
		
		posBlocks.put(i++, toAdd);
		
		toAdd = new int[][]{{0,0},{1,0},{0,1},{1,-1},{1,-2}}; //13
		
		nonSymmetries.put(13, new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7)));
		
		posBlocks.put(i++, toAdd);
	}

	// determines if another set of pixels collides with the block
	static public boolean pixelCollide(Block b, boolean[][] pixels){
		for (OrderedPair p : b.points){
			if (pixels[p.a][p.b]){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected Object clone(){
		Block b = new Block();
		
		b.maxx = maxx;
		b.maxy = maxy;
		b.minx = minx;
		b.miny = miny;
		
		b.points = new HashSet<>();
		
		for (OrderedPair p : points){
			b.points.add((OrderedPair) p.clone());
		}
		
		b.which = which;
		
		
		
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(pointArray);
		return result;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		
		if (!Arrays.deepEquals(pointArray,other.pointArray))
			return false;
		return true;
	}
}