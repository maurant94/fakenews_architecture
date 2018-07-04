package it.uniroma1.dis.ethereum;

import java.math.BigInteger;
import java.util.List;

import org.web3j.tuples.generated.Tuple5;

import it.uniroma1.dis.facade.FiveWExtractor;

public class SmartContractTEST {

	public static void main(String[] args) throws Exception {

		SmartContractManager manager = new SmartContractManager(); //DEPLOY FIRST TIME THEN LOAD

		List<it.uniroma1.dis.block.FiveW> fivew = FiveWExtractor.getextractedList(null);
		manager.start5w("prova.pdf", "hashhashhash", "ciao".getBytes(), "claims-claims2-", fivew);
		
		manager.getPayload("hashhashhash");
		
		manager.populateTestFiveW(5);
		
		manager.add5w("hashhashhash", fivew);
		
		FiveW contract = manager.getContract();

		Tuple5<String, String, byte[], BigInteger, BigInteger> ret = contract.news(new BigInteger("0")).send();

		System.out.println(ret);
	}

}
