package it.uniroma1.dis.ethereum;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

public class SmartContractManager {
   public static void main(String[] args) throws Exception {
	   
	   Web3j web3j = Web3j.build(new HttpService("http://localhost:7545"));  // defaults to http://localhost:8545/
	   Credentials credentials = Credentials.create("5c71e8cfae6e0cb8c80602d2f1fc66d1fca5674dd6f2ff05b4908c0156c777c5");

	   FiveW contract = FiveW.deploy( web3j, credentials, 
			   ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();

	   
	   System.out.println(credentials.getAddress());
	   System.out.println("NOW SOME TEST");
	   

	   List<byte[]> bytes = new ArrayList<>();
	   byte[] array = new byte[1]; // length is bounded by 7
	   Random r = new Random(1);
	   String generatedString;
	   r.nextBytes(array);
	   generatedString = new String(array, Charset.forName("UTF-8"));
       //System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
	   bytes.add(generatedString.getBytes());
	   List<BigInteger> accuracies = new ArrayList<>();
	   for (int i = 0; i < 10; i++)
		   accuracies.add(BigInteger.ONE);
	   
	   contract.startFiveW("prova", "hashhashhash", bytes, "claims-claims2", "a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#", accuracies).send();
	   System.out.println("START FINISHED");
	   
	   contract.getPayload("hashhashhash").send();
	   for (int i = 0; i < 5; i++)
		   contract.populateTestFiveW().send();
	   
	   contract.add5w("hashhashhash", "a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#", accuracies).send();
	   System.out.println("FIWADD CALLED");
	   
	   Tuple5<String, String, byte[], BigInteger, BigInteger> ret = contract.news(BigInteger.ZERO).send();
	   
	   System.out.println(ret);
   }
}
