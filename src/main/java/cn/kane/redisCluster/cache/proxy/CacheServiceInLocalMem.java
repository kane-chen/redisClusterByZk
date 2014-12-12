package cn.kane.redisCluster.cache.proxy;

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.kane.redisCluster.node.NodeRunningInfos;

@Service
public class CacheServiceInLocalMem implements ICacheService {

	private static final Logger LOG = LoggerFactory.getLogger(CacheServiceInLocalMem.class) ;
	private Map<String,Map<String,String>> cacheMapping = new ConcurrentHashMap<String, Map<String,String>>() ;
	
	@Override
	public void put(String key, String value) {
		Map<String,String> cacheServer = this.getTargetCacheServerWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER) ;
		cacheServer.put(key, value) ;
		LOG.info(String.format("[Put]%s:%s in %s",key,value,cacheServer.get("serverInfo")));
	}

	@Override
	public void put(String key, String value, long expiredMillis) {
		throw new UnsupportedOperationException("cache in memory,not support expireTime");
	}

	@Override
	public String get(String key) {
		Map<String,String> cacheServer = this.getTargetCacheServerWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_SLAVE) ;
		String value = cacheServer.get(key) ;
		LOG.info(String.format("[Get]%s:%s in %s",key,value,cacheServer.get("serverInfo")));
		return value;
	}

	@Override
	public void remove(String key) {
		Map<String,String> cacheServer = this.getTargetCacheServerWithKey(key,NodeRunningInfos.CACHE_OPERA_TYPE_MASTER) ;
		cacheServer.remove(key) ;
		LOG.info(String.format("[Remove]%s in %s",key,cacheServer.get("serverInfo")));
	}

	private Map<String,String> getTargetCacheServerWithKey(String key,int opType){
		Map<String,Object> cacheServerInfo = NodeRunningInfos.getInstance().getCacheServerByHash(key,opType) ;
		String memMappingKey = cacheServerInfo.toString() ;
		Map<String,String> cacheInMem = null ;
		if(cacheMapping.containsKey(memMappingKey)){
			cacheInMem = cacheMapping.get(memMappingKey) ;
		}else{
			//non-threadSafe
			cacheInMem = new ConcurrentHashMap<String, String>() ;
			cacheInMem.put("serverInfo", memMappingKey);
			cacheMapping.put(memMappingKey, cacheInMem) ;
		}
		return cacheInMem ;
	}
	
}
