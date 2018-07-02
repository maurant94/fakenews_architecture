package it.uniroma1.dis.ethereum;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

public class SmartContractManager {
   public static void main(String[] args) throws Exception {
	   
	   Web3j web3j = Web3j.build(new HttpService("http://localhost:7545"));  // defaults to http://localhost:8545/
	   Credentials credentials = Credentials.create("5c71e8cfae6e0cb8c80602d2f1fc66d1fca5674dd6f2ff05b4908c0156c777c5");

	   FiveW contract = FiveW.deploy( web3j, credentials, 
			   ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();

	   
	   System.out.println(credentials.getAddress());
}
}
