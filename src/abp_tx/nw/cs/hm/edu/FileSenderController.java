package abp_tx.nw.cs.hm.edu;

import javafx.animation.Transition;

public class FileSenderController {
	enum State {
		IDLE, SPLIT_DATA, BUILD_CONNECTION, SEND, SEND_NEXT, RECEIVE_ACK0,
		RECEIVE_ACK1
	};
	// all messages/conditions which can occur
	enum Msg {
		MEET_MAN, HI, TIME
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
	}
}
