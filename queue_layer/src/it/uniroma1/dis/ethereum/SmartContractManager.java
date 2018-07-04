package it.uniroma1.dis.ethereum;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

public class SmartContractManager {

	private FiveW contract = null;

	public SmartContractManager() {
		Web3j web3j = Web3j.build(new HttpService("http://localhost:7545")); //GANACHE
		Credentials credentials = Credentials.create("5c71e8cfae6e0cb8c80602d2f1fc66d1fca5674dd6f2ff05b4908c0156c777c5");

		try {
			contract = FiveW.deploy( web3j, credentials, 
					ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start5w(String name, String hash, List<byte[]> payloadRes, String claims, String meta5w, List<BigInteger> accuracies) throws Exception {
		System.out.println("START START5W...");
		List<byte[]> bytes = new ArrayList<>();
		byte[] array = new byte[1]; // length is bounded by 7
		Random r = new Random(1);
		String generatedString;
		r.nextBytes(array);
		generatedString = new String(array, Charset.forName("UTF-8"));
		//System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
		bytes.add(generatedString.getBytes());
		accuracies = new ArrayList<>();
		for (int i = 0; i < 10; i++)
			accuracies.add(BigInteger.ONE);

		contract.startFiveW("prova", "hashhashhash", bytes, "claims-claims2", "a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#", accuracies).send();
	}
	
	public void getPayload(String resHash) throws Exception {
		System.out.println("START PAYLOAD...");
		contract.getPayload("hashhashhash").send();
	}
	
	public void add5w(String hash, String extracted, List<BigInteger> extAccuracy) throws Exception {
		System.out.println("START ADD5W...");
		List<BigInteger> accuracies = new ArrayList<>();
		for (int i = 0; i < 10; i++)
			accuracies.add(BigInteger.ONE);
		contract.add5w("hashhashhash", "a#+#b#+#a#+#a#+#a#+##-#a#+#a#+#a#+#a#+#a#+#", accuracies).send();
	}

	public void populateTestFiveW(Integer len) throws Exception {
		System.out.println("START POPULATE TEST5W...");
		for (int i = 0; i < len; i++)
			contract.populateTestFiveW().send();
	}
	
	public FiveW getContract() {
		System.out.println("GET CONTRACT...");
		return contract;
	}
}
