package abp_tx.nw.cs.hm.edu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Payload {
	private ArrayList<Byte> dataArray = new ArrayList<>();
	private int idx;
	private int size;
	public boolean splitted = false;

	/*
	 * create a new payload object using a existing file on the hard disk this will
	 * populate the byte array list
	 */
	public Payload(File f) throws IOException {
		this.size = 0;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			int dataByte;

			// read data as long its there
			while ((dataByte = fis.read()) != -1) {
				dataArray.add((byte) dataByte);
				size = size + 1;
			}
		} finally {
			fis.close();
			System.out.println(this.getClass() + "::read " + size + " bytes");
			splitted = true;
		}
	}

	public byte getNextByte() throws NoBytesLeftException {
		if (this.idx + 1 > size) {
			throw new NoBytesLeftException();
		} else {
			return (byte) dataArray.get(++this.idx);
		}
	}

	public byte[] getCompleteDataArray() {
		byte[] array = new byte[this.size];
		for (int i = 0; i < size; i++) {
			array[i] = dataArray.get(i);
		}
		return array;
	}

	/*
	 * returns the size of the payload in bytes
	 */
	public int getSize() {
		return size;
	}
	
	public String getHeader() {
		String head = "";
		return head;
	}
}
