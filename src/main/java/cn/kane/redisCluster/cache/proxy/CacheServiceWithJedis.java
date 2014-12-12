package cn.kane.redisCluster.cache.proxy;

import java.util.Map;

import cn.kane.redisCluster.node.NodeRunningInfos;
import redis.clients.jedis.Jedis;

public class CacheServiceWithJedis implements ICacheService{

	@Override
	public void put(String key, String value) {
		if(null!=value){
			Jedis jedis = this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER) ;
			jedis.set(key, value) ;
		}
	}

	@Override
	public void put(String key, String value, long expiredMillis) {
		if(null!=value){
			Jedis jedis = this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER) ;
			jedis.set(key, value,null,null,expiredMillis) ;
		}
	}

	@Override
	public String get(String key) {
		return this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER).get(key) ;
	}

	@Override
	public void remove(String key) {
		this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER).del(key) ;
	}

	private Jedis getTargetJedisWithKey(String key,int opType){
		Map<String,Object> cacheServerInfo = NodeRunningInfos.getInstance().getCacheServerByHash(key,opType) ;
		String host = (String)cacheServerInfo.get("host") ;
		Integer port = (Integer)cacheServerInfo.get("port") ;
		return new Jedis(host,port);
	}
	
}
