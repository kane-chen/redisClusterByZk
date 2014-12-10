package cn.kane.redisCluster.cache.proxy;

import java.util.Map;

import cn.kane.redisCluster.node.NodeRunningInfos;
import redis.clients.jedis.Jedis;

public class CacheServiceWithJedis implements ICacheService{

	@Override
	public void put(String key, String value) {
		if(null!=value){
			Jedis jedis = this.getTargetJedisWithKey(key) ;
			jedis.set(key, value) ;
		}
	}

	@Override
	public void put(String key, String value, long expiredMillis) {
		if(null!=value){
			Jedis jedis = this.getTargetJedisWithKey(key) ;
			jedis.set(key, value,null,null,expiredMillis) ;
		}
	}

	@Override
	public String get(String key) {
		return this.getTargetJedisWithKey(key).get(key) ;
	}

	@Override
	public void remove(String key) {
		this.getTargetJedisWithKey(key).del(key) ;
	}

	private Jedis getTargetJedisWithKey(String key){
		Map<String,Object> cacheServerInfo = NodeRunningInfos.getCacheServerByHash(key) ;
		String host = (String)cacheServerInfo.get("host") ;
		Integer port = (Integer)cacheServerInfo.get("port") ;
		return new Jedis(host,port);
	}
	
}
