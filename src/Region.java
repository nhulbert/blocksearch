import java.util.ArrayList;
import java.util.Collection;

// represents a region of pixels on the tiling board

public class Region {    
	ArrayList<int[]> pixels;

	public Region(Collection<int[]> p){
		pixels = new ArrayList<>();
		
		for (int[] i : p){
			pixels.add(i);
		}
	}
	
	public Region(){
		pixels = new ArrayList<>();
	}
	
	// finds the smallest empty region in a given tiling board through a recursive fill search of empty pixels
	static public Region findSmallestRegion(Region original, boolean[][] gridpixels){
		ArrayList<VisitedPixel> pixels = new ArrayList<>();
		
		for (int[] i : original.pixels){
			if (!gridpixels[i[0]][i[1]]){
				pixels.add(new VisitedPixel(i[0],i[1]));
			}
		}
		
		// visit all connected empty pixels
		ArrayList<Region> regions = new ArrayList<>();
		for (int i=0; i<pixels.size(); i++){
			if (!pixels.get(i).visited){
				ArrayList<int[]> temp = new ArrayList<>();
				visit(i,pixels,temp);
				regions.add(new Region(temp));
			}
		}
		
		if (regions.isEmpty()) return new Region();
		
		// find smallest region
		int ind = -1;
		int smallestSize = Integer.MAX_VALUE;
		
		for (int i=0; i<regions.size(); i++){
			int s = regions.get(i).pixels.size();
			if (s < smallestSize){
				smallestSize = s;
				ind = i;
			}
		}
		
		return regions.get(ind);
	}
	
	// visit a given empty pixel and all other connected empty pixels
	static private void visit(int which, ArrayList<VisitedPixel> pixels, ArrayList<int[]> region){
		pixels.get(which).visited = true;
		VisitedPixel p = pixels.get(which);
		region.add(new int[]{p.a,p.b});
		
		// visits the adjacent empty pixels that aren't off the tiling board
		if (p.a-1 > -1){
			Integer io = pixels.indexOf(new VisitedPixel(p.a-1,p.b));
			if (io != -1 && !pixels.get(io).visited){
				visit(io, pixels, region);
			}
		}
		if (p.a+1 < 8){
			Integer io = pixels.indexOf(new VisitedPixel(p.a+1,p.b));
			if (io != -1 && !pixels.get(io).visited){
				visit(io, pixels, region);
			}
		}
		if (p.b-1 > -1){
			Integer io = pixels.indexOf(new VisitedPixel(p.a,p.b-1));
			if (io != -1 && !pixels.get(io).visited){
				visit(io, pixels, region);
			}
		}
		if (p.b+1 < 8){
			Integer io = pixels.indexOf(new VisitedPixel(p.a,p.b+1));
			if (io != -1 && !pixels.get(io).visited){
				visit(io, pixels, region);
			}
		}
	}
	
	// represents a pixel and whether it has been visited in the fill search
	static private class VisitedPixel extends OrderedPair{		
		private static final long serialVersionUID = -6630529018816506039L;
		boolean visited;
		
		public VisitedPixel(int a, int b){
			super(a,b);
			visited = false;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pixels.size();
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
		Region other = (Region) obj;
		if (pixels.size() != other.pixels.size())
			return false;
		return true;
	}
}

