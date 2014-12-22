package cn.kane.redisCluster.cache.proxy;

import org.springframework.data.redis.core.RedisTemplate;

import cn.kane.redisCluster.infos.NodeRunningInfos;

public class CacheServiceWithJedis implements ICacheService{

	@Override
	public void put(String key, String value) {
		if(null!=value){
			this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER).opsForValue().set(key, value);
		}
	}

	@Override
	public void put(String key, String value, long expiredMillis) {
		if(null!=value){
			this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER).opsForValue().set(key, value,expiredMillis);
		}
	}

	@Override
	public String get(String key) {
		Object result = this.getTargetJedisWithKey(key, NodeRunningInfos.CACHE_OPERA_TYPE_SLAVE).opsForValue().get(key) ;
		return result.toString() ;
	}

	@Override
	public void remove(String key) {
		this.getTargetJedisWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER).delete(key);
	}

	@SuppressWarnings("unchecked")
	private RedisTemplate<String,Object> getTargetJedisWithKey(String key,int opType){
		String nodeKey = NodeRunningInfos.getInstance().getCacheServerByHash(key,opType) ;
		Object cacheTemplate = NodeRunningInfos.getInstance().getCacheManByNodeKey(nodeKey) ;
		return (RedisTemplate<String,Object>)cacheTemplate;
	}
	
	
	
}
