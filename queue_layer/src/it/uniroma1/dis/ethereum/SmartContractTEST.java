package it.uniroma1.dis.ethereum;

import java.math.BigInteger;

import org.web3j.tuples.generated.Tuple5;

public class SmartContractTEST {

	public static void main(String[] args) throws Exception {

		SmartContractManager manager = new SmartContractManager();

		manager.start5w(null,null,null,null,null,null);
		
		manager.getPayload(null);
		
		manager.populateTestFiveW(5);
		
		manager.add5w(null,null,null);
		
		FiveW contract = manager.getContract();

		Tuple5<String, String, byte[], BigInteger, BigInteger> ret = contract.news(BigInteger.ZERO).send();

		System.out.println(ret);
	}

}
