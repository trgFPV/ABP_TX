package abp_tx.nw.cs.hm.edu;

import edu.hm.cs.netze1.FsmWoman.Msg;
import edu.hm.cs.netze1.FsmWoman.SayHi;
import edu.hm.cs.netze1.FsmWoman.State;
import javafx.animation.Transition;

public class FileSenderController {
	enum State {
		IDLE, SPLIT_DATA, BUILD_CONNECTION, SEND, SEND_NEXT, RECEIVE_ACK0,
		RECEIVE_ACK1
	};
	// all messages/conditions which can occur
	enum Msg {
		RECEIVED_PACKAGE,DATA_SPLITTED,CONNECTION_SUCCESS,ACK0_RECEIVED,ACK1_RECEIVED,NO_PACKAGE_LEFT
	}
	// current state of the FSM	
	private State currentState;
	// 2D array defining all transitions that can occur
	private Transition[][] transition;
	
	/**
	 * constructor
	 */
	public FileSenderController(){
		currentState = State.IDLE;
		transition = new Transition[State.values().length] [Msg.values().length];
		transition[State.IDLE.ordinal()] [Msg.RECEIVED_PACKAGE.ordinal()] = new GoSplit();
		transition[State.SPLIT_DATA.ordinal()] [Msg.DATA_SPLITTED.ordinal()] = new BuildHeader();
		transition[State.BUILD_CONNECTION.ordinal()] [Msg.CONNECTION_SUCCESS.ordinal()] = new SendFirst();
		transition[State.RECEIVE_ACK0.ordinal()] [Msg.ACK0_RECEIVED.ordinal()] = new SendNextAck0();
		transition[State.RECEIVE_ACK1.ordinal()] [Msg.ACK1_RECEIVED.ordinal()] = new SendNextAck1();
		transition[State.RECEIVE_ACK0.ordinal()] [Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();
		transition[State.RECEIVE_ACK1.ordinal()] [Msg.NO_PACKAGE_LEFT.ordinal()] = new BackToIdle();
		
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
			return State.RECEIVE_ACK0;
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
	
	class BackToIdle extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("All Packages send");
			return State.IDLE;
		}
	}
}
