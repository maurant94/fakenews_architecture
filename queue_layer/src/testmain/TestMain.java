package testmain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import it.uniroma1.dis.block.Data;
import it.uniroma1.dis.peer.Peer;
import it.uniroma1.dis.util.StringUtil;

public class TestMain {

	public static void main(String[] args) {

		if (args == null || args.length <= 5) {
			args = new String[7];
			args[1] = "1"; //byz
			args[2] = "2"; //peers
			args[3] = "3"; //request
			args[4] = "prova.txt";
			args[5] = "/Users/antoniomauro/Desktop/test.txt";
			args[6] = "/Users/antoniomauro/Desktop/test.txt";		
		}
		int byz = Integer.parseInt(args[1]);
		int peers = Integer.parseInt(args[2]);
		int request = Integer.parseInt(args[3]);
		String name = args[4];
		String fileName = args[5];
		String attacch = args[6];
		
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
			Path path = Paths.get(fileName);
			array = Files.readAllBytes(path);
			
			Peer p1 = null;
			for (int i = 0; i < request; i++) {
				try {
					p1 = new Peer(byz);
					p1.start(StringUtil.toObjects(array),name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
//			Thread.sleep(60000);
//			for (Data d : p1.getChainValues()) {
//				System.out.println(d);
//			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
