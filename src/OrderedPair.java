import java.io.Serializable;

// the serializable class representing an ordered pair of integers, which in this project is used to represent a 2D vector

class OrderedPair implements Serializable{
	private static final long serialVersionUID = -7037108644931705867L;
		int a;
		int b;
		
		public OrderedPair(int a, int b){
			this.a = a;
			this.b = b;
		}
		
		@Override
		protected Object clone(){
			// TODO Auto-generated method stub
			return new OrderedPair(a,b);
		}

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
			OrderedPair other = (OrderedPair) obj;
			if (a != other.a)
				return false;
			if (b != other.b)
				return false;
			return true;
		}
	}