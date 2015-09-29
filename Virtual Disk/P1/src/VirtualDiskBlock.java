

/**
 * The VirtualDiskBlock create an array to read and write to memory,
 * but this array should have the same capacity as the memory block capacity.
 * 
 * @author Kevin Santiago
 *
 */
public class VirtualDiskBlock {

	private static final int DEFAULT_CAPACITY = 256; // Default number of capacity
	private byte[] arr;
	
	/**
	 * Creates an array with length 256 
	 */
	public VirtualDiskBlock(){
		this(DEFAULT_CAPACITY);
	}
	
	/**
	 * Creates an array with length specified in blockCapacity
	 * 
	 * @param blockCapacity 
	 * 				is the capacity of the array
	 */
	public VirtualDiskBlock(int blockCapacity){
		arr = new byte[blockCapacity];
	}
	
	/**
	 * @return the length of the array
	 */
	public int getCapacity(){
		return arr.length;
	}
	
	/**
	 * @param index 
	 * 				is the array position to remove
	 * @param nuevo 
	 * 				is the element to place on position index
	 */
	public void setElement(int index, byte nuevo) throws IndexOutOfBoundsException {
		if(index < 0 || index > getCapacity())
			throw new IndexOutOfBoundsException("setElement: Invalid index: " + index);
		arr[index] = nuevo;
	}
	
	/**
	 * @param index 
	 * 				is the array position to remove
	 * @return the element at position index
	 */
	public byte getElement(int index) throws IndexOutOfBoundsException {
		if(index < 0 || index > getCapacity())
			throw new IndexOutOfBoundsException("getElement: Invalid index: " + index);
		return arr[index];
	}
}