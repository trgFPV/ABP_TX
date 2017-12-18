package abp_tx.nw.cs.hm.edu;

public class FileSender implements Runnable{

	public enum State {
		IDLE, SPLIT_DATA, BUILD_CONNECTION, SEND, SEND_NEXT, RECEIVE_ACK0,
		RECEIVE_ACK1
	}
	
	public State state;
	
	public FileSender() {
		state = State.IDLE;
	}
	
	public static void main(String[] args) {
		new Thread(new FileSender()).start();
	}
	
	public void run() {
		switch (state) {
		case IDLE:
			break;
		
		case SPLIT_DATA:
			break;
			
		case BUILD_CONNECTION:
			break;
			
		case SEND:
			break;
			
		case RECEIVE_ACK0:
			break;
			
		case RECEIVE_ACK1:
			break;
			
		case SEND_NEXT:
			break;
		}
	}
}
