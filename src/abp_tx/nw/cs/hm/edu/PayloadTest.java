package abp_tx.nw.cs.hm.edu;

import java.io.File;
import java.io.IOException;

public class PayloadTest {
	public static void main(String... args) {
		File f = new File("test.txt");
		Payload p = null;
		try {
			p = new Payload(f);
		} catch (IOException fnfe) {

		}

		// output the file contents
		 for (int i = 0; i < p.getSize(); i++)
		 System.out.print((char)p.getCompleteDataArray()[i]);
		

//		while (true) {
//			try {
//				System.out.print((char) p.getNextByte());
//			} catch (NoBytesLeftException nble) {
//				break;
//			}
//		}

	}

}
