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
		IDLE, SPLIT_DATA, BUILD_CONNECTION, SEND_FIRST, SEND_NEXT0, SEND_NEXT1, SEND_AGAIN
	};

	// all messages/conditions which can occur
	enum Msg {
		RECEIVED_PACKAGE, DATA_SPLITTED, CONNECTION_SUCCESS, ACK0_RECEIVED, ACK1_RECEIVED, NO_PACKAGE_LEFT, TIMEOUT_BUILD_CONNECTION, UNSUCCESSFULL_BUILD_CONNECTION, SEND_SUCCESSFULL, SEND_UNSUCCESSFULL, CONNECTION_INTERRUPTED
	}

	// current state of the FSM
	private State currentState;
	// 2D array defining all transitions that can occur
	private Transition[][] transition;

	private Payload pay;
	private Tx transmitter;
	private int index = 0;
	private int size;

	/**
	 * constructor
	 */
	public FileSenderController() {
		currentState = State.IDLE;
		transition = new Transition[State.values().length][Msg.values().length];

		transition[State.IDLE.ordinal()][Msg.RECEIVED_PACKAGE.ordinal()] = new GoSplit();

		transition[State.SPLIT_DATA.ordinal()][Msg.DATA_SPLITTED.ordinal()] = new BuildHeader();

		transition[State.BUILD_CONNECTION.ordinal()][Msg.CONNECTION_SUCCESS.ordinal()] = new SendFirst();

		transition[State.SEND_FIRST.ordinal()][Msg.SEND_SUCCESSFULL.ordinal()] = new GetAck1();
		transition[State.SEND_FIRST.ordinal()][Msg.SEND_UNSUCCESSFULL.ordinal()] = new SendFirst();
		transition[State.SEND_FIRST.ordinal()][Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();

		transition[State.SEND_AGAIN.ordinal()][Msg.SEND_SUCCESSFULL.ordinal()] = new SendNextAck0(); // nochmal
																										// ack0
																										// und
																										// ack1

		transition[State.SEND_NEXT0.ordinal()][Msg.ACK0_RECEIVED.ordinal()] = new SendNextAck1();
		transition[State.SEND_NEXT0.ordinal()][Msg.ACK1_RECEIVED.ordinal()] = new SendAgain0();
		transition[State.SEND_NEXT0.ordinal()][Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();

		transition[State.SEND_NEXT1.ordinal()][Msg.ACK1_RECEIVED.ordinal()] = new SendNextAck0();
		transition[State.SEND_NEXT1.ordinal()][Msg.ACK0_RECEIVED.ordinal()] = new SendAgain1();
		transition[State.SEND_NEXT1.ordinal()][Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();

		transition[State.BUILD_CONNECTION.ordinal()][Msg.TIMEOUT_BUILD_CONNECTION.ordinal()] = new BackToIdle();
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
		switch (currentState) {
		case IDLE:
			System.out.println("wait for File");
			while (true) {
				// System.out.println("File please:");
				// Scanner in = new Scanner(System.in);
				// String file = in.nextLine();
				// FileInputStream FileIn = new FileInputStream(file);
				File f = new File("test.txt");
				try {
					pay = new Payload(f);
					break;
				} catch (IOException e) {
					System.out.println("File Not Found");
					continue;
				}
			}
			processMsg(Msg.RECEIVED_PACKAGE);

		case SPLIT_DATA:
			if (pay.splitted) {
				processMsg(Msg.DATA_SPLITTED);
			}

		case BUILD_CONNECTION:
			InetAddress adress = null;
			try {
				adress = InetAddress.getByName("192.168.178.137");

				transmitter = new Tx(adress, 8087, pay.getCompleteDataArray().length, 1420, pay);
			} catch (SocketException e) {
				processMsg(Msg.CONNECTION_INTERRUPTED);
				e.printStackTrace();
			} catch (InvalidPackageSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processMsg(Msg.CONNECTION_SUCCESS);

		case SEND_FIRST:
			size = pay.getSize();
			try {
				transmitter.send(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (transmitter.waitAck1()) {
				System.out.println("suck my dick");
				processMsg(Msg.SEND_SUCCESSFULL);
			} else if (transmitter.allSend) {
				processMsg(Msg.NO_PACKAGE_LEFT);
			} else {
				processMsg(Msg.SEND_UNSUCCESSFULL);
			}

		case SEND_NEXT1:
			index++;
			transmitter.sequenceUP();
			try {
				transmitter.sendNext(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (index == (size - 1)) {
				transmitter.allSend = true;
				processMsg(Msg.NO_PACKAGE_LEFT);
			}
			if (transmitter.waitAck1()) {
				System.out.println("suck my dick");
				processMsg(Msg.ACK1_RECEIVED);
			} else {
				System.out.println("suck my dick twice");
			}

		case SEND_NEXT0:
			index++;
			transmitter.sequenceUP();
			try {
				transmitter.sendNext(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (index == (size - 1)) {
				transmitter.allSend = true;
				processMsg(Msg.NO_PACKAGE_LEFT);
			}
			if (transmitter.waitAck0()) {
				System.out.println("suck my dick");
			} else {
				System.out.println("suck my dick twice");
			}

		case SEND_AGAIN:
			try {
				transmitter.send(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (index == (size - 1)) {
				transmitter.allSend = true;
				processMsg(Msg.NO_PACKAGE_LEFT);
			}

			processMsg(Msg.SEND_SUCCESSFULL);

		}
	}

	abstract class Transition {
		abstract public State execute(Msg input);
	}

	class GoSplit extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("File Received!");
			return State.SPLIT_DATA;
		}
	}

	class BuildHeader extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Header Builded at Sending now");
			return State.BUILD_CONNECTION;
		}
	}

	class SendFirst extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Start sending Package");
			return State.SEND_FIRST;
		}
	}

	class SendNextAck0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send next Package");
			return State.SEND_NEXT0;
		}
	}

	class SendNextAck1 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send next Package");
			return State.SEND_NEXT1;
		}
	}

	class SendAgain0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send last Package");
			return State.SEND_AGAIN;
		}
	}

	class SendAgain1 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send last Package");
			return State.SEND_AGAIN;
		}
	}

	class GetAck0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Ack0 received");
			return State.SEND_NEXT1;
		}
	}

	class GetAck1 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Ack1 received");
			return State.SEND_NEXT0;
		}
	}

	class BackToIdle extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("All Packages send");
			return State.IDLE;
		}
	}
}
