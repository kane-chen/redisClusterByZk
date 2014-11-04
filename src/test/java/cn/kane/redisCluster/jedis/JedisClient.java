package cn.kane.redisCluster.jedis;

import redis.clients.jedis.Jedis;

public class JedisClient {

	private Jedis jedis ;
	
	public JedisClient(String host,int port,int timeOut){
		this.jedis = new Jedis(host,port,timeOut);
	}

	public String ping(){
		return jedis.ping() ;
	}
	
}
