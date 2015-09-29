

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;
import java.util.Random;

/**
 * Simulate the way a memory access the data for write and read and also simulate
 * when a memory loss a block because of physical damage.
 * 
 */
public class DiskUnit {
	private static final int DEFAULT_CAPACITY = 1024; // default number of
													// blocks
	private static final int DEFAULT_BLOCK_SIZE = 256; // default number of
														// bytes per
	// block

	private int capacity; // number of blocks of current disk instance
	private int blockSize; // size of each block of current disk instance

	private RandomAccessFile disk;

	/**
	 * name is the name of the disk
	 **/
	private DiskUnit(String name) {
		try {
			disk = new RandomAccessFile(name, "rw");
		} catch (IOException e) {
			System.err.println("Unable to start the disk");
			System.exit(1);
		}
	}

	/**
	 * Turns on an existing disk unit.
	 * 
	 * @param name
	 *            the name of the disk unit to activate
	 * @return the corresponding DiskUnit object
	 * @throws NonExistingDiskException
	 *             whenever no ¨disk¨ with the specified name is found.
	 */
	public static DiskUnit mount(String name) throws NonExistingDiskException {
		File file = new File(name);
		if (!file.exists())
			throw new NonExistingDiskException("No disk has name : " + name);

		DiskUnit dUnit = new DiskUnit(name);

		// get the capacity and the block size of the disk from the file
		// representing the disk
		try {
			dUnit.disk.seek(0);
			dUnit.capacity = dUnit.disk.readInt();
			dUnit.blockSize = dUnit.disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dUnit;
	}

	/***
	 * Creates a new disk unit with the given name. The disk is formatted as
	 * having default capacity (number of blocks), each of default size (number
	 * of bytes). Those values are: DEFAULT_CAPACITY and DEFAULT_BLOCK_SIZE. The
	 * created disk is left as in off mode.
	 * 
	 * @param name
	 *            the name of the file that is to represent the disk.
	 * @throws ExistingDiskException
	 *             whenever the name attempted is already in use.
	 */
	public static void createDiskUnit(String name) throws ExistingDiskException {
		createDiskUnit(name, DEFAULT_CAPACITY, DEFAULT_BLOCK_SIZE);
	}

	/**
	 * Creates a new disk unit with the given name. The disk is formatted as
	 * with the specified capacity (number of blocks), each of specified size
	 * (number of bytes). The created disk is left as in off mode.
	 * 
	 * @param name
	 *            the name of the file that is to represent the disk.
	 * @param capacity
	 *            number of blocks in the new disk
	 * @param blockSize
	 *            size per block in the new disk
	 * @throws ExistingDiskException
	 *             whenever the name attempted is already in use.
	 * @throws InvalidParameterException
	 *             whenever the values for capacity or blockSize are not valid
	 *             according to the specifications
	 */
	public static void createDiskUnit(String name, int capacity, int blockSize)
			throws ExistingDiskException, InvalidParameterException {
		File file = new File(name);
		if (file.exists())
			throw new ExistingDiskException("Disk name is already used: " + name);

		RandomAccessFile disk = null;
		if (capacity < 0 || blockSize < 0 || !Utils.powerOf2(capacity) || !Utils.powerOf2(blockSize))
			throw new InvalidParameterException(
					"Invalid values: " + " capacity = " + capacity + " block size = " + blockSize);
		// disk parameters are valid... hence create the file to represent the
		// disk unit.
		try {
			disk = new RandomAccessFile(name, "rw");
		} catch (IOException e) {
			System.err.println("Unable to start the disk");
			System.exit(1);
		}

		reserveDiskSpace(disk, capacity, blockSize);

		// after creation, just leave it in shutdown mode - just
		// close the corresponding file
		try {
			disk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The method store the capacity and the blockSize y the first byte of the file
	 * 
	 * @param disk
	 * 				 is a RandomAccessFile to store data
	 * @param capacity 
	 * 				is the number of blocks that the file will have, this number
	 * 				should be a power of two
	 * @param blockSize
	 * 				is the number of bytes that each blocks will have
	 */
	private static void reserveDiskSpace(RandomAccessFile disk, int capacity, int blockSize) {
		try {
			disk.setLength(blockSize * capacity);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// write disk parameters (number of blocks, bytes per block) in
		// block 0 of disk space
		try {
			disk.seek(0);
			disk.writeInt(capacity);
			disk.writeInt(blockSize);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The method search a specified position in file and make a copy of it in VirtualDiskBlock 
	 * 
	 * @param blockNum 
	 * 				a position in file
	 * @param b 			
	 * 				is the instance of VirtualDiskBlock
	 * @throws IOException
	 */
	public void read(int blockNum, VirtualDiskBlock b) throws IOException {
		blockNum = blockNum * blockSize;
		if (blockNum < 0 || blockNum > capacity * blockSize)
			throw new InvalidBlockNumberException("read: Invalid block number: " + blockNum);
		if (b.getCapacity() != blockSize)
			throw new InvalidBlockException("read: Invalid block capacity");
		disk.seek(blockNum);
		for (int i = 0; i < blockSize; i++)
			b.setElement(i, disk.readByte());

	}

	/**
	 * The method search a specified position in file and write on file the content
	 * in VirtualDiskBlock
	 * 
	 * @param blockNum
	 * 				a position in file
	 * @param b 
	 * 				is the instance of VirtualDiskBlock
	 * @throws IOException
	 */
	public void write(int blockNum, VirtualDiskBlock b) throws IOException {
		if (blockNum == 0)
			throw new InvalidBlockNumberException(
					"write: The first block is reserved for system use and can't be overwritten.");
		blockNum = blockNum * blockSize;
		if (blockNum < 0 || blockNum > capacity * blockSize)
			throw new InvalidBlockNumberException("read: Invalid block number: " + blockNum);
		if (b.getCapacity() != blockSize)
			throw new InvalidBlockException("read: Invalid block capacity");
		disk.seek(blockNum);
		for (int i = 0; i < blockSize; i++)
			disk.writeByte(b.getElement(i));
	}

	/**
	 * 
	 * @return the number of blocks in the file
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * 
	 * @return the number of bytes on each blocks
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * The method close the RandomAccessFile
	 */
	public void shutdown() {
		try {
			disk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The method erase the complete file and put
	 * @throws IOException 
	 */
	public void lowLevelFormat() throws IOException {
		Random ran = new Random();
		for(int i = 0; i < capacity*blockSize; i++){
			if(ran.nextInt(149) == 0)
				disk.writeByte(-1);
			else
				disk.writeByte(0);
		}
	}
}
