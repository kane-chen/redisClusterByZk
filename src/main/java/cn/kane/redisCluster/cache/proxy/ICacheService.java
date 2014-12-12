package cn.kane.redisCluster.cache.proxy;

/**
 * simple
 * @author kane
 *
 */
//TODO more interfaces
public interface ICacheService {

	void put(String key,String value) ;

	void put(String key,String value,long expiredMillis) ;
	
	String get(String key) ;
	
	void remove(String key) ;
}
