package it.uniroma1.dis.facade;

import java.nio.ByteBuffer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class CassandraFacade {

	String serverIP = "127.0.0.1";
	String KEYSPACE = "news";
	String TABLE = "article";

	Cluster cluster;
	Session session;
	
	public CassandraFacade() {

		cluster = Cluster.builder()
		  .addContactPoints(serverIP)
		  .build();

		session = cluster.connect(KEYSPACE);
		
	}
	
	public Boolean existHash(String key) {
		try {
			Statement query = QueryBuilder.select().from(KEYSPACE,TABLE).where(QueryBuilder.eq("id",key)).limit(5);
			ResultSet row = session.execute(query);
			if (row != null && !row.isExhausted())
				return true;
			return false;
		} catch(Exception e) {
			//unable to connect to database or OTHER
			e.printStackTrace();
			return false;
		}
		
	}
	
	public Boolean insertResource(byte[] res, String hash) {
		Insert insert = QueryBuilder.insertInto(KEYSPACE,TABLE)
                .value("id", hash)
                .value("file", ByteBuffer.wrap(res));
		System.out.println(insert.toString());
		session.execute(insert.toString());
		
		return false;
	}
}
