package abp_tx.nw.cs.hm.edu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import abp_tx.nw.cs.hm.edu.FsmWoman.Msg;
import abp_tx.nw.cs.hm.edu.FsmWoman.Transition;

public class FileSenderController implements Runnable {
	enum State {
		IDLE, SPLIT_DATA, BUILD_CONNECTION, SEND, SEND_FIRST, SEND_NEXT, RECEIVE_ACK0, RECEIVE_ACK1, SEND_AGAIN
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

		transition[State.SEND_FIRST.ordinal()][Msg.SEND_SUCCESSFULL.ordinal()] = new GetAck0();
		transition[State.SEND_FIRST.ordinal()][Msg.SEND_UNSUCCESSFULL.ordinal()] = new SendFirst();
		transition[State.SEND_FIRST.ordinal()][Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();
		transition[State.SEND_FIRST.ordinal()][Msg.CONNECTION_INTERRUPTED.ordinal()] = new ConnectAgain();

		transition[State.RECEIVE_ACK0.ordinal()][Msg.ACK0_RECEIVED.ordinal()] = new SendNextAck1();
		transition[State.RECEIVE_ACK0.ordinal()][Msg.ACK1_RECEIVED.ordinal()] = new SendAgain0();
		transition[State.RECEIVE_ACK0.ordinal()][Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();

		transition[State.RECEIVE_ACK1.ordinal()][Msg.ACK1_RECEIVED.ordinal()] = new SendNextAck0();
		transition[State.RECEIVE_ACK1.ordinal()][Msg.ACK0_RECEIVED.ordinal()] = new SendAgain1();
		transition[State.RECEIVE_ACK1.ordinal()][Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();

		transition[State.BUILD_CONNECTION.ordinal()][Msg.TIMEOUT_BUILD_CONNECTION.ordinal()] = new BackToIdle();
		transition[State.BUILD_CONNECTION.ordinal()][Msg.UNSUCCESSFULL_BUILD_CONNECTION.ordinal()] = new ConnectAgain();
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
			while (true) {
				//System.out.println("File please:");
				//Scanner in = new Scanner(System.in);
				//String file = in.nextLine();
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
			System.out.println("File Found");
			processMsg(Msg.RECEIVED_PACKAGE);

		case SPLIT_DATA:
			if(pay.splitted) {
				processMsg(Msg.DATA_SPLITTED);
			}
			break;

		case BUILD_CONNECTION:
			InetAddress adress = null;
			try {
				adress = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				transmitter = new Tx(adress,8000,pay.getCompleteDataArray().length,1400,pay);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPackageSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case SEND_FIRST:
			size = pay.getSize();
			try {
				transmitter.send(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processMsg(Msg.ACK0_RECEIVED);
			break;


		case SEND:
			index++;
			try {
				transmitter.send(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(index == (size-1)) {
				transmitter.allSend = true;
			}
			break;

		case SEND_NEXT:
			index++;
			try {
				transmitter.send(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(index == (size-1)) {
				transmitter.allSend = true;
			}
			break;

		case SEND_AGAIN:
			try {
				transmitter.send(index);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(index == (size-1)) {
				transmitter.allSend = true;
			}
			break;

		case RECEIVE_ACK0:
			
			break;

		case RECEIVE_ACK1:
			break;
		}
	}

	abstract class Transition {
		abstract public State execute(Msg input);
	}

	class GoSplit extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Package Received!");
			return State.SPLIT_DATA;
		}
	}

	class BuildHeader extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Header Builded");
			return State.BUILD_CONNECTION;
		}
	}

	class SendFirst extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send Package");
			return State.SEND;
		}
	}

	class SendNextAck0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send next Package");
			return State.RECEIVE_ACK1;
		}
	}

	class SendNextAck1 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send next Package");
			return State.RECEIVE_ACK0;
		}
	}

	class SendAgain0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send last Package");
			return State.RECEIVE_ACK0;
		}
	}

	class SendAgain1 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Send last Package");
			return State.RECEIVE_ACK0;
		}
	}

	class GetAck0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Ack0 received or not");
			return State.SEND_NEXT;
		}
	}

	class GetAck1 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Ack1 received or not");
			return State.SEND_NEXT;
		}
	}

	class BackToIdle extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("All Packages send");
			return State.IDLE;
		}
	}

	class ConnectAgain extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Connect Again");
			return State.BUILD_CONNECTION;
		}
	}
}
