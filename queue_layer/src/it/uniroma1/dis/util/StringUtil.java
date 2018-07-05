package it.uniroma1.dis.util;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StringUtil {
	//Applies Sha256 to a string and returns the result. 
	public static String applySha256(String input){		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");	        
			//Applies sha256 to our input, 
			byte[] hash = digest.digest(input.getBytes("UTF-8"));	        
			StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Byte[] toObjects(byte[] bytesPrim) {
	    Byte[] bytes = new Byte[bytesPrim.length];
	    Arrays.setAll(bytes, n -> bytesPrim[n]);
	    return bytes;
	}
	
	public static Byte[] toObjects(List<byte[]> bytesList) {
		byte[] bytesPrim = new byte [bytesList.size()];
		for (int i = 0; i < bytesList.size(); i++)
			bytesPrim[i] = bytesList.get(i)[0];
	    Byte[] bytes = new Byte[bytesPrim.length];
	    Arrays.setAll(bytes, n -> bytesPrim[n]);
	    return bytes;
	}
	
	public static byte[] fromObjects(Byte[] bytes) {
	    byte[] primByte = new byte[bytes.length];
	    int j=0;
		for(Byte b: bytes)
			primByte[j++] = b.byteValue();
	    return primByte;
	}
	
	public static <K, V> String mapToString(Map<K, V> map) {
	    return map.entrySet()
	        .stream()
	        .map(entry -> entry.getKey() + ":" + entry.getValue())
	        .collect(Collectors.joining(", ", "{", "}"));
	}
	
	public static <K, V> String applySha256Map(Map<K, V> map){		
		try {
			return applySha256(mapToString(map));
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
