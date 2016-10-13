import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DB.BTreeSetMaker;
import org.mapdb.DBMaker;

public class BlockSearch {	
	static int numSols = 0; // the number of solutions found
	static int duplicateCount = 0; // the number of solutions found that are the same as a previously found solution, up to isometry
	static Region entire; // the region representing the full tiling board
	
	static Comparator<Block> blockComparator = new Comparator<Block>() { // compares block types, for the canonicalization of solutions
        public int compare(Block o1, Block o2) {
        	return Integer.compare(o1.which, o2.which);
        }
    };
	
	static Comparator<PixelNeighbors> pixelComparator = new Comparator<PixelNeighbors>() { // compares the number of a pixel's filled neighbors
        public int compare(PixelNeighbors o1, PixelNeighbors o2) {
        	return Integer.compare(o2.numNeighbors, o1.numNeighbors);
        }
    };
    
    static Comparator<OrderedPair> opComparator = new Comparator<OrderedPair>(){ // compares ordered pair lexicographically
    	public int compare(OrderedPair o1, OrderedPair o2) {
        	int res = Integer.compare(o1.a, o2.a);
        	
        	if (res != 0) return res;
        	
        	return Integer.compare(o1.b, o2.b);
        }
    };
    
    static Set<ArrayList<Block>> solutions; // the set of solutions, handled with a MapDB B-tree
    
    static HashMap<IntPair,Integer> pairRankings = new HashMap<>(); // a map between each of the n int pairs and the integers 0 through (n-1), for the canonicalization of solutions
    
	public static void main(String[] args) {
		Comparator<ArrayList<Block>> comp = new Comparator<ArrayList<Block>>() { // compares canonicalized solutions
	        public int compare(ArrayList<Block> o1, ArrayList<Block> o2) {
	        	for (int i=0; i<14; i++){
	        		for (int h=0; h<o1.get(i).pointArray.length; h++){
	        			int res = opComparator.compare(o1.get(i).pointArray[h], o2.get(i).pointArray[h]);
	        			
	        			if (res != 0) return res;
	        		}
	        	}
	        	
	        	return 0;
	        }
	    };
	    
	    // set up the B-tree
	    
	    DB db = DBMaker.memoryDB()
                .transactionDisable()
                .make();
	    
		BTreeSetMaker btsm = db.new BTreeSetMaker("BTreeSetMaker");
		btsm.comparator(comp);
		
		solutions = btsm.make();
		
		initializePairRankings();
		Block.initializeStatics();
		
		// initialize tiling board
		ArrayList<int[]> ent = new ArrayList<>();
		
		for (int i=0; i<64; i++){
			ent.add(new int[]{i/8,i%8});
		}
		
		entire = new Region(ent);
		
		final boolean[][] pixels = new boolean[8][8];
		
		for (int i=0; i<64; i++){
			pixels[i/8][i%8] = false;
		}
		
		final ArrayList<Block> grid = new ArrayList<>();
		
		final ArrayList<Integer> usedBlocks = new ArrayList<>();
		
		final ArrayList<int[]> t = new ArrayList<>();
		for (int i=0; i<64; i++){
			t.add(new int[]{i/8,i%8});
		}
		
		// start search on four threads
		new Thread(){public void run(){search(0,2,new Region(t), pixels,grid,usedBlocks,0);}}.start();
		new Thread(){public void run(){search(3,5,new Region(t), pixels,grid,usedBlocks,0);}}.start();
		new Thread(){public void run(){search(6,9,new Region(t), pixels,grid,usedBlocks,0);}}.start();
		new Thread(){public void run(){search(10,13,new Region(t), pixels,grid,usedBlocks,0);}}.start();
	}

	// maps each of all possible pairs of blocks to an arbitrary integer for solution canonicalization
	private static void initializePairRankings() {
		int count = 0;
		for (int i=0; i<13; i++){
			for (int h=i+1; h<14; h++){
				pairRankings.put(new IntPair(i,h),count++);
			}
		}
	}

	static private class IntPair{ // An unordered integer pair
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + a;
			result = prime * result + b;
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
			IntPair other = (IntPair) obj;
			if (a != other.a)
				return false;
			if (b != other.b)
				return false;
			return true;
		}

		int a;
		int b;
		
		public IntPair(int a, int b){
			if (a<b){
				this.a = a;
				this.b = b;
			}
			else{
				this.a = b;
				this.b = a;
			}
		}
	}
	
	// a pixel with its number of neighboring pixels unavailable for filling
	static private class PixelNeighbors{
		int[] pixel;
		Integer numNeighbors;
		
		public PixelNeighbors(int[] p, boolean[][] grid){
			pixel = p;
			numNeighbors = 0;
			
			if (p[0]-1 < 0 || grid[p[0]-1][p[1]]){
				numNeighbors++;
			}
			if (p[0]+1 > 7 || grid[p[0]+1][p[1]]){
				numNeighbors++;
			}
			if (p[1]-1 < 0 || grid[p[0]][p[1]-1]){
				numNeighbors++;
			}
			if (p[1]+1 > 7 || grid[p[0]][p[1]+1]){
				numNeighbors++;
			}
		}
	}
	
	// the recursive search for solutions
	public static void search(int nextPieceMin, int nextPieceMax, Region r, boolean[][] pixels, ArrayList<Block> grid, ArrayList<Integer> usedBlocks, final int level){		
			ArrayList<PixelNeighbors> pns = new ArrayList<>();
			
			for (int[] i : r.pixels){
				pns.add(new PixelNeighbors(i,pixels));
			}
			
			pns.sort(pixelComparator); // searches first the pixels with the most number of neighboring pixels unavailable to be filled, to eliminate impossible solutions ASAP
				int[] searchPix = pns.get(0).pixel;
					for (int h=nextPieceMin; h<=nextPieceMax; h++){
						if (!usedBlocks.contains((Integer)h)){
								int x = searchPix[0]; // x coordinate of pixel to be filled
								int y = searchPix[1]; // y coordinate of pixel to be filled
								
								ArrayList<Integer> ns = Block.nonSymmetries.get(h); // get the candidate block's unique transformations
								for (Integer p : ns){
									int len = Block.posBlocks.get(h).length; // the size of the block in pixels
									for (int anc=0; anc<len; anc++){ // 'anc' holds the index describing with which of the block's pixels we are trying to fill searchpix
										Block block = new Block(h,new int[]{x,y},p,anc); // creates a new block with pixel 'anc' anchored on searchPix
										if (block.maxx < 8 && block.minx > -1 && block.maxy < 8 && block.miny > -1 && !Block.pixelCollide(block, pixels)){ // checks whether the block fits on the tiling board
											//deep copies pixels
											boolean[][] temp = new boolean[8][8];
											for (int i=0; i<64; i++){
												temp[i/8][i%8]=pixels[i/8][i%8];
											}
											
											final boolean[][] newpixels = temp;
											
											// copies the previously added and used blocks and updates the copies with the new block
											ArrayList<Block> temp2 = new ArrayList<Block>(grid);
											ArrayList<Integer> temp3 = new ArrayList<Integer>(usedBlocks);
											
											for (OrderedPair point : block.points){
												newpixels[point.a][point.b] = true;
											}
											temp2.add(block);
											final ArrayList<Block> newgrid = temp2;
											
											temp3.add(h);
											final ArrayList<Integer> newUB = temp3;
											
											if (newUB.size() == 14){ // if this is a solution
												// convert the solution into its canonical form
												final ArrayList<Block> normed = getNormalizedGrid(newgrid);
												normed.sort(blockComparator);
												for (Block b : normed){
													OrderedPair[] op = new OrderedPair[b.points.size()];
													b.points.toArray(op);
													Arrays.sort(op,opComparator);
													b.pointArray = op;
												}
												
												// add solution if it's not already found
												if(!contains(normed)){
													new Thread(){public void run(){printGrid(normed);}}.start(); // print the solution on a separate thread
													
													addSolution(normed);
												}
												else{
													incrementDuplicates(); // increment the duplicate count in the case that the solution is already found
												}
											}
											else if (!checkOnes(newUB,newpixels)){ // checks for the simple case that the smallest region is 1 pixel big, before calling findSmallestRegion
												// continues the search by trying to fill the smallest region
												
												//printGrid(newgrid);
												Region temp1 = Region.findSmallestRegion(r, newpixels);
												if (temp1.pixels.isEmpty()){
													temp1 = Region.findSmallestRegion(entire, newpixels);
												}
												
												final Region newr = temp1;
												
												search(0,13,newr,newpixels,newgrid,newUB,level+1);
											}
										}
								}
							}
				}
			}
	}

	private static synchronized void incrementDuplicates() {
		duplicateCount++;
	}

	private static synchronized boolean contains(ArrayList<Block> normed) {
		return solutions.contains(normed);
	}
	
	@SuppressWarnings("unused")
	private static ArrayList<Block> getFlippedGrid(ArrayList<Block> newgrid){
		int m = -1;
		int n = -1;
		
		int l = 0;
		
		int t0 = l*m;
		int t1 = (1-l)*n;
		int t2 = (1-l)*m;
		int t3 = l*n;
		
		int offx = (-7*(t0+t1-1))/2;
		int offy = (-7*(t2+t3-1))/2;
		
		ArrayList<Block> out = new ArrayList<Block>();
		
		for (Block b : newgrid){
			Block nb = new Block();
			nb.points = new HashSet<>();
			nb.which = b.which;
			
			for (OrderedPair point : b.points){
				nb.points.add(new OrderedPair(t0*point.a+t1*point.b+offx, t2*point.a+t3*point.b+offy));
			}
			
			out.add(nb);
		}
		
		return out;
	}
	
	// computes the canonicalized version of a solution by making sure the 'smaller' (unordered) pair of block types among those occupying the corner squares are on the top and left, and 
	private static ArrayList<Block> getNormalizedGrid(ArrayList<Block> newgrid) {
		int lt=-1;
		int rt=-1;
		int lb=-1;
		int rb=-1;
		
		boolean stop = false; // whether to break out of the outer for loop
		for (Block b : newgrid){
			for (OrderedPair a : b.points){
				if (a.a == 0 && a.b == 0){
					lt = b.which;
				}
				else if (a.a == 7 && a.b == 0){
					rt = b.which;
				}
				else if (a.a == 7 && a.b == 7){
					rb = b.which;
				}
				else if (a.a == 0 && a.b == 7){
					lb = b.which;
				}
				
				if (lt != -1 && rt != -1 && lb != -1 && rb != -1){
					stop = true;
					break;
				}
			}
			if (stop) break;
		}
		
		int top = pairRankings.get(new IntPair(lt,rt));
		int bottom = pairRankings.get(new IntPair(lb,rb));
		int left = pairRankings.get(new IntPair(lt,lb));
		int right = pairRankings.get(new IntPair(rt,rb));
		
		int m = Integer.signum(right-left); // the right-left flip coefficient
		int n = Integer.signum(bottom-top); // the top-bottom flip coefficient
		
		int l = 1; //determines whether reversal of axes is necessary
		if (Math.abs(right-left)<Math.abs(bottom-top)) l=0;
		
		// the values of the transformation matrix that should be applied to the solution
		int t0 = l*m;
		int t1 = (1-l)*n;
		int t2 = (1-l)*m;
		int t3 = l*n;
		
		int offx = (-7*(t0+t1-1))/2;
		int offy = (-7*(t2+t3-1))/2;
		
		ArrayList<Block> out = new ArrayList<Block>();
		
		for (Block b : newgrid){
			Block nb = new Block();
			nb.points = new HashSet<>();
			nb.which = b.which;
			
			for (OrderedPair point : b.points){
				nb.points.add(new OrderedPair(t0*point.a+t1*point.b+offx, t2*point.a+t3*point.b+offy));
			}
			
			out.add(nb);
		}
		
		return out;
	}

	private static synchronized void addSolution(ArrayList<Block> newgrid) {		
		solutions.add(newgrid);
	}

	// checks if there are isolated empty blocks in the grid (in which case a solution is impossible)
	public static boolean checkOnes(ArrayList<Integer> usedBlocks, boolean[][] pixels){
		if (usedBlocks.contains((Integer)0)){
			for (int m1=0; m1<8; m1++){
				for (int n1=0; n1<8; n1++){
					if (!pixels[m1][n1]){
						if ((m1-1 < 0 || pixels[m1-1][n1]) &&
						   (m1+1 > 7 || pixels[m1+1][n1]) &&
						   (n1-1 < 0 || pixels[m1][n1-1]) &&
						   (n1+1 > 7 || pixels[m1][n1+1])){
								return true;
							}
					}
				}
			}
		}
		else{
			int singleSpaceCount = 0;
			
			for (int m1=0; m1<8; m1++){
				for (int n1=0; n1<8; n1++){
					if (!pixels[m1][n1]){
						if ((m1-1 < 0 || pixels[m1-1][n1]) &&
						   (m1+1 > 7 || pixels[m1+1][n1]) &&
						   (n1-1 < 0 || pixels[m1][n1-1]) &&
						   (n1+1 > 7 || pixels[m1][n1+1])){
								singleSpaceCount++;
								if (singleSpaceCount == 2) return true;
							}
					}
				}
			}
		}
		
		return false;
	}
	
	// prints the grid to the console
	public static synchronized void printGrid(ArrayList<Block> grid){
		System.out.print("Found Solution #");
		System.out.print(++numSols);
		System.out.print("\n Duplicate count: "+Integer.toString(duplicateCount));
		
		String[][] out = new String[8][8];
		
		for (int m=0; m<8; m++){
			for (int n=0; n<8; n++){
				out[m][n] = "___";
			}
		}
		
		int bn = 0;
		
		System.out.print("\n \n");
		for (Block b : grid){
			for (OrderedPair p : b.points){
				
				String toAdd = Integer.toString(bn)+"           ";
				toAdd = toAdd.substring(0, 3);
				out[p.a][p.b] = toAdd;
			}
			bn++;
		}
		
		for (int m=0; m<8; m++){
			for (int n=0; n<8; n++){
				System.out.print(out[m][n]);
			}
			System.out.print('\n');
		}
		
		System.out.print("\n\n");
	}
}
