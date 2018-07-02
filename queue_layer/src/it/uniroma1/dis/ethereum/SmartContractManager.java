package it.uniroma1.dis.ethereum;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

public class SmartContractManager {
   public static void main(String[] args) throws Exception {
	   String rinkebyKey = args[0];
	   String walletFilePassword = args[1];
	  
	   String rinkebyUrl = "https://rinkeby.infura.io/" + rinkebyKey;
	   Web3j web3j = Web3j.build(new HttpService(rinkebyUrl));

	   String walletId = "4a50789f96b4121a14974d607d6a0f5384ec4af4";
	   String walletSource = "~/testnet-keystore/UTC--2017-10-12T11-10-59.66000000Z--" + walletId + ".json";
	   Credentials credentials = WalletUtils.loadCredentials(walletFilePassword, walletSource);

	   FiveW contract = FiveW.deploy( web3j, credentials, 
			   ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();

	   
	   System.out.println("NOW HERE");
}
}
