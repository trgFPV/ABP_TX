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
import java.util.ArrayList;
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
	private byte[] inData = new byte[1000];

	public Tx(InetAddress rx_ip, int port, int completePkgSize, int dataPkgSize, Payload payload)
			throws SocketException, InvalidPackageSizeException {

		// check if data and sequence number exceed the maximum package size
		if (completePkgSize < dataPkgSize + checkSumNrSize + sequenceNrSize) {
			throw new InvalidPackageSizeException();
		}
		this.outPutSocket = new DatagramSocket(50000);

		this.RX_IP = rx_ip;
		this.PORT = port;
		this.completePkgSize = completePkgSize;
		this.dataPkgSize = dataPkgSize;
		this.payload = payload;
	}

	public void sequenceUP() {
		sequenceNrSize++;
	}

	public void send(int index) throws IOException {
		// prepare packet
		DatagramPacket p = preparePacket(index, 1);
		outPutSocket.send(p);
	}

	public void sendNext(int index, int ack) throws IOException {

		if (index >= payload.dataArray.size() - 1) {
			System.out.println("index:" + index + "Size: "+ payload.dataArray.size());
			allSend = true;
			ack = 2;
		}

		// prepare packet
		DatagramPacket p = preparePacket(index, ack);
		outPutSocket.send(p);
	}

	public DatagramPacket preparePacket(int index, int ack) {
		// package data = payload + 4 bytes sequence + sequenceNrSize +
		// checkSumSize
		// copy the dataFrame from the payload
		if (ack < 2) {
			byte[] dataFrame = payload.getCompleteDataArray();

			dataFrame = Arrays.copyOfRange(dataFrame, index, index + dataPkgSize);
			System.out.println(dataFrame.length);

			byte[] header = getHeader(dataFrame, ack);

			System.out.println(header.length);

			return new DatagramPacket(header, header.length, RX_IP, PORT);
		} else {

			byte[] header = getSmallHeader(ack);

			return new DatagramPacket(header, header.length, RX_IP, PORT);
		}
	}

	public byte[] getSmallHeader(int ack) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			output.write(storeIntInToByte(ack));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output.toByteArray();
	}

	public byte[] getHeader(byte[] data, int ack) {

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			output.write(storeIntInToByte(ack));
			output.write(storeIntInToByte(payload.getSequence()));
			output.write(storeLongInToByte(generateChecksum(data)));
			output.write(storeIntInToByte(data.length));

			output.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toByteArray();
	}

	private long generateChecksum(byte[] field) {
		// int checksum = 0;
		// for(byte b : field) {
		// checksum += b;
		// }
		//
		// return checksum;
		CRC32 crc32 = new CRC32();
		crc32.update(field);
		return crc32.getValue();
	}

	public int waitForIT() {
		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length, InetAddress.getByName("192.168.178.137"), 8087);
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
		return ack;
	}

	public boolean waitAck0() {
		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length, InetAddress.getByName("192.168.178.137"), 8087);
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

		if (ack == 0) {
			return true;
		} else {

		}
		return false;
	}

	public boolean waitAck1() {
		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length, InetAddress.getByName("192.168.178.137"), 8087);
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

		if (ack == 1) {
			return true;
		} else {
			return false;
		}
	}

	public static byte[] storeLongInToByte(Long data) {
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

	public int byteToInt(byte[] input) {
		final ByteBuffer buff = ByteBuffer.wrap(input);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		return buff.getInt();
	}

	public boolean SendAgain() {
		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length, InetAddress.getByName("192.168.178.137"), 8087);
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

		if (ack == this.ack) {
			return true;
		} else {
			return false;
		}
	}

}
