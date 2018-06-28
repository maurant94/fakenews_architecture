package it.uniroma1.dis.peer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import it.uniroma1.dis.block.Data;
import it.uniroma1.dis.util.StringUtil;

public class TrackMessage implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String type;
	//default type
	public static final String REQUEST = "REQUEST";
	public static final String ORDER_REQUEST = "ORDER_REQUEST";
	public static final String SPEC_REQUEST = "SPEC_REQUEST";
	public static final String DECIDE_REQUEST = "DECIDE_REQUEST";

	private Long timestamp;
	private Byte[] resurce;
	private String resource_name;
	private String client_public_key;
	private String leader_public_key;
	private Integer sequence_number;
	private String history_digest;
	private String data_digest;
	private Data data;
	private String outcome_response;
	private Byte[] encrytped_message;
	private TrackMessage order_request_message;
	private HashMap<Integer, String> decided;
	
	private PublicKey decryptKey;
	private Byte[] secKeyAES;
	
	private TrackMessage() {}
	
	public static TrackMessage getRequest(Byte[] resource, String name, String client_pub, PrivateKey key, PublicKey publicKey) {
		TrackMessage message = new TrackMessage();
		message.setType(REQUEST);
		message.setResource_name(name);
		message.setResurce(resource);
		message.setTimestamp(new Date().getTime());
		message.setClient_public_key(client_pub);
		
		//ENCRYPT
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128); // The AES key size in number of bits
			SecretKey sec = generator.generateKey();
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, sec);
			byte[] byteCipherText = aesCipher.doFinal(message.toBytes());
			
			message = new TrackMessage();
			message.setEncrytped_message(StringUtil.toObjects(byteCipherText));
			message.setDecryptKey(publicKey);
			
			//save key encrypted
			Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			encrypt.init(Cipher.ENCRYPT_MODE, key);
			byte[] encKey = encrypt.doFinal(sec.getEncoded());
			message.setSecKeyAES(StringUtil.toObjects(encKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return message;
	}
	
	public static TrackMessage getOrderRequest(Data data, Integer sequence_number, 
			HashMap<Integer, TrackMessage> hystory, PrivateKey key, PublicKey publicKey) {
		TrackMessage message = new TrackMessage();
		message.setType(ORDER_REQUEST);
		message.setSequence_number(sequence_number);
		message.setHistory_digest(StringUtil.applySha256Map(hystory));
		message.setData_digest(StringUtil.applySha256(data.toString()));
		//ENCRYPT
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128); // The AES key size in number of bits
			SecretKey sec = generator.generateKey();
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, sec);
			byte[] byteCipherText = aesCipher.doFinal(message.toBytes());
			
			message = new TrackMessage();
			message.setEncrytped_message(StringUtil.toObjects(byteCipherText));
			message.setData(data);
			message.setDecryptKey(publicKey);
			
			//save key encrypted
			Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			encrypt.init(Cipher.ENCRYPT_MODE, key);
			byte[] encKey = encrypt.doFinal(sec.getEncoded());
			message.setSecKeyAES(StringUtil.toObjects(encKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return message;
	}
	
	public static TrackMessage getSpeculativeResponse(Data data, Integer sequence_number, 
			HashMap<Integer, TrackMessage> hystory, PrivateKey key, String leader_public_key,
			String outcome_response, PublicKey publicKey) {
		TrackMessage message = new TrackMessage();
		message.setType(SPEC_REQUEST);
		message.setSequence_number(sequence_number);
		message.setHistory_digest(StringUtil.applySha256Map(hystory));
		message.setData_digest(StringUtil.applySha256(outcome_response));
		message.setTimestamp(new Date().getTime());
		message.setLeader_public_key(leader_public_key);
		//ENCRYPT
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128); // The AES key size in number of bits
			SecretKey sec = generator.generateKey();
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, sec);
			byte[] byteCipherText = aesCipher.doFinal(message.toBytes());
			
			message = new TrackMessage();
			message.setEncrytped_message(StringUtil.toObjects(byteCipherText));
			message.setOutcome_response(outcome_response);
			message.setDecryptKey(publicKey);
			
			//save key encrypted
			Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			encrypt.init(Cipher.ENCRYPT_MODE, key);
			byte[] encKey = encrypt.doFinal(sec.getEncoded());
			message.setSecKeyAES(StringUtil.toObjects(encKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return message;
	}
	
	public static TrackMessage getDecidedResponse(Integer sequence_number, HashMap<Integer, String> decided, String leader_pub, PrivateKey key, PublicKey publicKey) {
		TrackMessage message = new TrackMessage();
		message.setType(DECIDE_REQUEST);
		message.setDecided(decided);
		message.setSequence_number(sequence_number);
		message.setTimestamp(new Date().getTime());
		message.setLeader_public_key(leader_pub);
		
		//ENCRYPT
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128); // The AES key size in number of bits
			SecretKey sec = generator.generateKey();
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, sec);
			byte[] byteCipherText = aesCipher.doFinal(message.toBytes());
			
			message = new TrackMessage();
			message.setEncrytped_message(StringUtil.toObjects(byteCipherText));
			message.setDecryptKey(publicKey);
			
			//save key encrypted
			Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			encrypt.init(Cipher.ENCRYPT_MODE, key);
			byte[] encKey = encrypt.doFinal(sec.getEncoded());
			message.setSecKeyAES(StringUtil.toObjects(encKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return message;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Byte[] getResurce() {
		return resurce;
	}
	public void setResurce(Byte[] resurce) {
		this.resurce = resurce;
	}
	public String getClient_public_key() {
		return client_public_key;
	}
	public void setClient_public_key(String client_public_key) {
		this.client_public_key = client_public_key;
	}
	public String getLeader_public_key() {
		return leader_public_key;
	}
	public void setLeader_public_key(String leader_public_key) {
		this.leader_public_key = leader_public_key;
	}
	public Integer getSequence_number() {
		return sequence_number;
	}
	public void setSequence_number(Integer sequence_number) {
		this.sequence_number = sequence_number;
	}
	public String getHistory_digest() {
		return history_digest;
	}
	public void setHistory_digest(String history_digest) {
		this.history_digest = history_digest;
	}
	public String getData_digest() {
		return data_digest;
	}
	public void setData_digest(String data_digest) {
		this.data_digest = data_digest;
	}
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}
	public String getOutcome_response() {
		return outcome_response;
	}
	public void setOutcome_response(String outcome_response) {
		this.outcome_response = outcome_response;
	}
	public String getResource_name() {
		return resource_name;
	}
	public void setResource_name(String resource_name) {
		this.resource_name = resource_name;
	}
	public Byte[] getEncrytped_message() {
		return encrytped_message;
	}
	public void setEncrytped_message(Byte[] encrytped_message) {
		this.encrytped_message = encrytped_message;
	}
	public TrackMessage getOrder_request_message() {
		return order_request_message;
	}
	public void setOrder_request_message(TrackMessage order_request_message) {
		this.order_request_message = order_request_message;
	}
	public PublicKey getDecryptKey() {
		return decryptKey;
	}
	public void setDecryptKey(PublicKey decryptKey) {
		this.decryptKey = decryptKey;
	}
	public Byte[] getSecKeyAES() {
		return secKeyAES;
	}
	public void setSecKeyAES(Byte[] secKeyAES) {
		this.secKeyAES = secKeyAES;
	}
	public HashMap<Integer, String> getDecided() {
		return decided;
	}
	public void setDecided(HashMap<Integer, String> decided) {
		this.decided = decided;
	}

	private byte[] toBytes() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] bytes = null;
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(this);
		  out.flush();
		  bytes = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		  try {
		    bos.close();
		  } catch (IOException e) {
				e.printStackTrace();
		  }
		}
		return bytes;
	}
	
	private static TrackMessage fromBytes(byte[] bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		TrackMessage t = null;
		try {
		  in = new ObjectInputStream(bis);
		  t = (TrackMessage) in.readObject(); 
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException e) {
				e.printStackTrace();
		  }
		}
		return t;
	}
	
	public static TrackMessage decrypt(Byte[] encKey, Byte[] message, PublicKey key) {
		try {
			
			Cipher decrypt = Cipher.getInstance("RSA");
			decrypt.init(Cipher.DECRYPT_MODE, key);
			//hystory and digest
			byte[] decryptedKey = decrypt.doFinal(StringUtil.fromObjects(encKey));
			SecretKey sec = new SecretKeySpec(decryptedKey, "AES");
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.DECRYPT_MODE, sec);
			byte[] decrypted = aesCipher.doFinal(StringUtil.fromObjects(message));
			//now we have the key
			return fromBytes(decrypted);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return "TrackMessage [type=" + type + ", timestamp=" + timestamp + ", resurce=" + Arrays.toString(resurce)
				+ ", resource_name=" + resource_name + ", client_public_key=" + client_public_key
				+ ", leader_public_key=" + leader_public_key + ", sequence_number=" + sequence_number + ", view_id="
				+ ", history_digest=" + history_digest + ", data_digest=" + data_digest + ", data=" + data
				+ ", outcome_response=" + outcome_response + ", encrytped_message=" + Arrays.toString(encrytped_message)
				+ ", order_request_message=" + order_request_message + ", decided=" + decided + ", decryptKey="
				+ decryptKey + ", secKeyAES=" + Arrays.toString(secKeyAES) + "]";
	}
	
	
	
	
}
