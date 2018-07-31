package it.uniroma1.dis.facade;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.uniroma1.dis.block.FiveW;
import it.uniroma1.dis.block.ItemFiveW;

public class FiveWExtractor {
	
	public static List<FiveW> getextractedList(Byte[] resource){

		String s = null;
		String outputString = "";
		List<FiveW> retList = new ArrayList<>();
		try {
//			Process p = Runtime.getRuntime().exec("python3 ../Giveme5W-master/extractor/fivew_single_article.py");
			Process p = Runtime.getRuntime().exec("ls");
			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			// read the output from the command
			while ((s = stdInput.readLine()) != null)
				outputString += s;
			
			if (outputString.indexOf("[{") > -1 && outputString.indexOf("}]") > -1)
				outputString = outputString.substring(outputString.indexOf("[{"), outputString.indexOf("}]")+1);
			//else System.err.println("failure");
			

			outputString = "[{\"whoName\": \"Carl \", \"whoScore\": 50, \"whenName\": \"Friday  - \", \"whenScore\": 16.0, \"whereName\": \"Naples \", \"whereScore\": 50, \"whatName\": \"hat.02\", \"whatScore\": 50, \"dativeName\": \"Jhon\", \"dativeScore\": 50}, {\"whoName\": \"Alice \", \"whoScore\": 50, \"whenName\": \"Friday  - \", \"whenScore\": 16.0, \"whatName\": \"sleep_together.01\", \"whatScore\": 50, \"dativeName\": \"Bob\", \"dativeScore\": 50}]";
			//now check structure
			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
			
			System.out.println(outputString);
			Gson gson = new GsonBuilder()
					.setLenient()
					.create();
			
			JsonArray array = gson.fromJson (outputString, JsonArray.class);
			JsonElement element;
			JsonObject jsonObj;
			for (int i = 0; i < array.size(); i++) {
				element = array.get(i);
				jsonObj = element.getAsJsonObject();
				
				ItemFiveW dative = null;
				if (jsonObj.has("dativeName")) {
					dative = new ItemFiveW(jsonObj.get("dativeName")==null?null:jsonObj.get("dativeName").getAsString(), 
							jsonObj.get("dativeScore")==null?null:jsonObj.get("dativeScore").getAsDouble());
				}
				
				ItemFiveW who = null;
				if (jsonObj.has("whoName")) {
					who = new ItemFiveW(jsonObj.get("whoName")==null?null:jsonObj.get("whoName").getAsString(), 
							jsonObj.get("whoScore")==null?null:jsonObj.get("whoScore").getAsDouble());
				}
				
				ItemFiveW where = null;
				if (jsonObj.has("whereName")) {
					where = new ItemFiveW(jsonObj.get("whereName")==null?null:jsonObj.get("whereName").getAsString(), 
							jsonObj.get("whereScore")==null?null:jsonObj.get("whereScore").getAsDouble());
				}
				
				ItemFiveW when = null;
				if (jsonObj.has("whenName")) {
					when = new ItemFiveW(jsonObj.get("whenName")==null?null:jsonObj.get("whenName").getAsString(), 
							jsonObj.get("whenScore")==null?null:jsonObj.get("whenScore").getAsDouble());
				}
				
				ItemFiveW what = null;
				if (jsonObj.has("whatName")) {
					what = new ItemFiveW(jsonObj.get("whatName")==null?null:jsonObj.get("whatName").getAsString(), 
							jsonObj.get("whatScore")==null?null:jsonObj.get("whatScore").getAsDouble());
				}
				
				FiveW fw = new FiveW(who, what, where, when, null, dative);
				retList.add(fw);
				System.out.println(fw.getWho().getName());
			}
			
			
		} catch (Exception e) {
			retList = null;
			e.printStackTrace();
		}
		return retList;//retList;
	}

}
