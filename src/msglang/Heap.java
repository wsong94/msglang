package msglang;

/**
 * Representation of a heap, which maps references to values.
 * 
 * @author hridesh
 *
 */
public interface Heap {

	Value ref (Value value);

	Value deref (Value.RefVal loc);

	Value setref (Value.RefVal loc, Value value);

	Value free (Value.RefVal value);

	Heap clone ();
	
	static public class Heap16Bit implements Heap {
		static final int HEAP_SIZE = 65_536;
		
		Value[] _rep = new Value[HEAP_SIZE];
		int index = 0;
		
		public Value ref (Value value) {
			if(index >= HEAP_SIZE)
				return new Value.DynamicError("Out of memory error");
			Value.RefVal new_loc = new Value.RefVal(index);
			_rep[index++] = value;
			return new_loc;
		}

		public Value deref (Value.RefVal loc) {
			try {
				return _rep[loc.loc()];
			} catch (ArrayIndexOutOfBoundsException e) {
				return new Value.DynamicError("Segmentation fault at access " + loc);
			}
		}

		public Value setref (Value.RefVal loc, Value value) {
			try {
				return _rep[loc.loc()] = value;
			} catch (ArrayIndexOutOfBoundsException e) {
				return new Value.DynamicError("Segmentation fault at access " + loc);
			}
		}

		public Value free (Value.RefVal loc) {
			try {
				_rep[loc.loc()] = null;
				//REMARK: students should add this location to free list.
				return loc;
			} catch (ArrayIndexOutOfBoundsException e) {
				return new Value.DynamicError("Segmentation fault at access " + loc);
			}
		}

		/***
		 * Explanation: 
		 */
		public Heap clone () {
			Heap16Bit _clone = new Heap16Bit(); 
			for(int i = 0; i < HEAP_SIZE; i++)
				_clone._rep[i] = this._rep[i];
			return _clone;
		}
		
		public Heap16Bit(){}
	}
	
}
