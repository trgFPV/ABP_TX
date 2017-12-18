package abp_tx.nw.cs.hm.edu;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class Tx {
	private InetAddress RX_IP = null;
	private int PORT = 0;
	private int completePkgSize = 0;
	private int dataPkgSize = 0;
	private int sequenceNrSize = 0;
	private int checkSumNrSize = 0;
	private Payload payload = null;
	DatagramSocket outPutSocket = new DatagramSocket();

	public Tx(InetAddress rx_ip, int port, int completePkgSize, int dataPkgSize, Payload payload)
			throws SocketException, InvalidPackageSizeException {

		// check if data and sequence number exceed the maximum package size
		if (completePkgSize < dataPkgSize + checkSumNrSize + sequenceNrSize) {
			throw new InvalidPackageSizeException();
		}

		this.RX_IP = rx_ip;
		this.PORT = port;
		this.completePkgSize = completePkgSize;
		this.dataPkgSize = dataPkgSize;
		this.payload = payload;
	}

	public void send(int index) throws IOException {
		// prepare packet
		DatagramPacket p = preparePacket(index);
		outPutSocket.send(p);
	}

	public DatagramPacket preparePacket(int index) {
		// package data = payload + 4 bytes sequence + sequenceNrSize + checkSumSize

		// copy the dataFrame from the payload
		byte[] dataFrame = payload.getCompleteDataArray();
		dataFrame = Arrays.copyOfRange(dataFrame, index, index + dataPkgSize);

		return new DatagramPacket(dataFrame, completePkgSize, RX_IP, PORT);
	}

	private long generateChecksum(byte[] field) {
		CRC32 crc32 = new CRC32();
		crc32.update(field);
		return crc32.getValue();
	}

	public static byte[] storeLongInToByte(Long data) {
		// bytes needed to store data
		int n = 1;
		
		// if we need to store a 0 we still need atleast one byte
		if (data != 0l) {
			n = (int) Math.ceil((Math.log(data) / Math.log(2)) / 8);
		}
		
		byte dataArray [] = new byte [n];
		
		for (int i = 0; n > i; i++) {
			int bitmask = 0x0000FF;
			byte valueToStore = (byte) (data & bitmask);
			
			dataArray[i] = valueToStore;
			
			System.out.println("bitmask: " + bitmask);
			System.out.println("value: " + valueToStore);
			
			for(int x = 0; x <= 7; x++) {
				data >>>= data;
			}
		}

		// bytes needed
		System.out.println(n);
		return dataArray;
	}

}
