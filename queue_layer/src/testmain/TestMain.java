package testmain;

import java.nio.charset.Charset;
import java.util.Random;

import it.uniroma1.dis.peer.Peer;
import it.uniroma1.dis.util.StringUtil;

public class TestMain {

	public static void main(String[] args) {

		int byz = 1;
		int peers = 1;
		int request = 3;
		
		//TEST EXTRACTOR PYTHON
//		FiveWExtractor.getextractedList(null);
		//TEST CASSANDRA QUERY
//		CassandraFacade c = new CassandraFacade();
//		System.out.println(c.existHash("a1"));
//		c.insertResource("ciao".getBytes());
		
		try {
			for (int i = 0; i < peers; i++)
				new Peer(byz);
			
			byte[] array = new byte[1]; // length is bounded by 1 due to ethereum
		    Random r = new Random();
		    String generatedString;
			
			for (int i = 0; i < request; i++) {
				Peer p1;
				try {
					p1 = new Peer(byz);
				    r.nextBytes(array);
				    generatedString = new String(array, Charset.forName("UTF-8"));
					p1.start(StringUtil.toObjects(generatedString.getBytes()),"prova.txt");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
