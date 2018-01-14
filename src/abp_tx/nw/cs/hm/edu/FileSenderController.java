package abp_tx.nw.cs.hm.edu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FileSenderController implements Runnable {
	enum State {
		IDLE
	}

	// all messages/conditions which can occur
	enum Msg {
		RECEIVED_PACKAGE, ACK0_RECEIVED, ACK1_RECEIVED, /**NO_PACKAGE_LEFT,**/ TIMEOUT
	}

	// current state of the FSM
	private State currentState;
	// 2D array defining all transitions that can occur
	private Transition[][] transition;

	private Payload pay;
	private Tx transmitter;
	private int index = 0;

	private int dataPkgSize = 1400;
	private int ack = 1;
	private boolean process = true;

	/**
	 * constructor
	 */
	public FileSenderController() {
		currentState = State.IDLE;
		transition = new Transition[State.values().length][Msg.values().length];

		transition[State.IDLE.ordinal()][Msg.ACK0_RECEIVED.ordinal()] = new SendNextPackage();

		transition[State.IDLE.ordinal()][Msg.ACK1_RECEIVED.ordinal()] = new SendNextPackage();

		transition[State.IDLE.ordinal()][Msg.TIMEOUT.ordinal()] = new Timeout();

	}

	public void processMsg(Msg input) {
		System.out.println("INFO Received " + input + " in state " + currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if (trans != null) {
			currentState = trans.execute(input);
		}
		System.out.println("INFO State: " + currentState);
	}

	public static void main(String[] args) {
		new Thread(new FileSenderController()).run();
	}

	public void run() {
		try {
			pay = new Payload(new File("test.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InetAddress adress = null;
		try {
			adress = InetAddress.getByName("192.168.178.137");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			transmitter = new Tx(adress, 8087, pay.getCompleteDataArray().length, 1420, pay);
		} catch (SocketException | InvalidPackageSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			transmitter.send(index);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (process) {

			int LOLack = transmitter.waitForIT();

			switch (ack) {
			case 0:
				if (ack == LOLack) {
					processMsg(Msg.ACK0_RECEIVED);
				}
				break;

			case 1:
				if (ack == LOLack) {
					processMsg(Msg.ACK1_RECEIVED);
				}
				break;

			}
			if (ack == 0) {
				ack = 1;
			} else {
				ack = 0;
			}

			try {
				transmitter.sendNext(index, ack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	abstract class Transition {
		abstract public State execute(Msg input);
	}

	class SendNextPackage extends Transition {
		@Override
		public State execute(Msg input) {
			index += dataPkgSize;
			return State.IDLE;
		}
	}
	
	class Timeout extends Transition {
		@Override
		public State execute(Msg input) {
			index = 0;
			return State.IDLE;
		}
	}

}
