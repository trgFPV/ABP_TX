package abp_tx.nw.cs.hm.edu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

public class Tx {
	private InetAddress RX_IP = null;
	private int PORT = 0;
	private int completePkgSize = 0;
	private int dataPkgSize = 0;
	private int sequenceNrSize = 0;
	private int checkSumNrSize = 0;
	private int ack = 1;
	private Payload payload = null;
	DatagramSocket outPutSocket;
	public boolean allSend = false;
	private byte[] inData = new byte[1400];

	public Tx(InetAddress rx_ip, int port, int completePkgSize, int dataPkgSize, Payload payload)
			throws SocketException, InvalidPackageSizeException {

		// check if data and sequence number exceed the maximum package size
		if (completePkgSize < dataPkgSize + checkSumNrSize + sequenceNrSize) {
			throw new InvalidPackageSizeException();
		}
			this.outPutSocket = new DatagramSocket(8086);
		

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
		// package data = payload + 4 bytes sequence + sequenceNrSize +
		// checkSumSize

		// copy the dataFrame from the payload
		byte[] dataFrame = payload.getCompleteDataArray();
		dataFrame = Arrays.copyOfRange(dataFrame, index, index + dataPkgSize);

		byte[] header = getHeader(dataFrame);

		return new DatagramPacket(header, header.length, RX_IP, PORT);
	}

	public byte[] getHeader(byte[] data) {

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try {
			output.write(storeIntInToByte(ack));
			output.write(storeIntInToByte(payload.getSequence()));
			output.write(storeLongInToByte(generateChecksum(data)));
			output.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toByteArray();
	}

	private long generateChecksum(byte[] field) {
		CRC32 crc32 = new CRC32();
		crc32.update(field);
		return crc32.getValue();
	}
	
	public void waitAck0() {
		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length,InetAddress.getByName("192.168.178.137"),8087);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			outPutSocket.receive(input);
			System.out.println("package received");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] head = input.getData();
		int ack = head[0];
		
		if(ack == 0) {
			
		} else {
			
		}
	}
	
	public void waitAck1() {
		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length,InetAddress.getByName("192.168.178.137"),8087);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			outPutSocket.receive(input);
			System.out.println("package received");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] head = input.getData();
		int ack = head[0];
		
		if(ack == 1) {
			
		} else {
			
		}
	}

	public static byte[] storeLongInToByte(Long data) {
		// // bytes needed to store data
		// int n = 1;
		//
		// // if we need to store a 0 we still need atleast one byte
		// if (data != 0l) {
		// n = (int) Math.ceil((Math.log(data) / Math.log(2)) / 8);
		// }
		//
		// byte dataArray[] = new byte[n];
		//
		// for (int i = 0; n > i; i++) {
		// int bitmask = 0x0000FF;
		// byte valueToStore = (byte) (data & bitmask);
		//
		// dataArray[i] = valueToStore;
		//
		// System.out.println("bitmask: " + bitmask);
		// System.out.println("value: " + valueToStore);
		//
		// for (int x = 0; x <= 7; x++) {
		// data >>>= data;
		// }
		// }
		//
		// // bytes needed
		// System.out.println(n);
		// return dataArray;
		final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong(data);
		return bb.array();
	}
	
	public static byte[] storeIntInToByte(int data) {
	
		final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(data);
		return bb.array();
	}

}
