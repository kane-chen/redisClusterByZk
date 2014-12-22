package cn.kane.redisCluster.cache.man;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

public class JedisCacheManageService implements ICacheManageInterface<RedisTemplate<String,Object>> {

	private static final Logger LOG = LoggerFactory.getLogger(JedisCacheManageService.class) ;
	
	private String host ;
	private int port ;
	private int timeOut ;
	private JedisConnectionFactory jedisConnFactory ;
	private RedisTemplate<String,Object> redisTemplate ;
	
	public JedisCacheManageService(String host,int port,int timeOut){
		this.host = host ;
		this.port = port ;
		this.timeOut = timeOut ;
		this.initJedisConn();
	}
	
	private void initJedisConn(){
		/* POOL-CONFIG*/
		JedisPoolConfig jedisPoolConf = new JedisPoolConfig() ;
		jedisPoolConf.setMinIdle(10);
		jedisPoolConf.setMaxTotal(100);
		jedisPoolConf.setMaxWaitMillis(3000);
		jedisPoolConf.setTestOnBorrow(true);
		//conn-factory
		JedisConnectionFactory tmpJedisConnFactory = new JedisConnectionFactory(jedisPoolConf);
		tmpJedisConnFactory.setHostName(host);
		tmpJedisConnFactory.setPort(port);
		tmpJedisConnFactory.setTimeout(timeOut);
		//setter
		this.jedisConnFactory = tmpJedisConnFactory ;
		this.redisTemplate = new RedisTemplate<String, Object>() ;
		redisTemplate.setConnectionFactory(jedisConnFactory);
	}
	
	@Override
	public String ping() {
		LOG.debug(String.format("[Jedis]ping %s , timeOut= %s",this.cacheServerInfo(),timeOut));
		String result = redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.ping();
            }
        });
		if("PONG".equals(result)){
			return "OK";
		}else{
			return "FAILED";
		}
	}

	@Override
	public void slaveOf(String masterHost, int port) {
		LOG.debug(String.format("[Jedis] [%s] be slaveOf [%s:%s]",this.cacheServerInfo(),masterHost,port));
		redisTemplate.slaveOf(masterHost, port);
	}

	@Override
	public void beMaster() {
		LOG.debug(String.format("[Jedis]beMaster %s",this.cacheServerInfo()));
		redisTemplate.slaveOfNoOne();
	}

	@Override
	public void reConn() {
		LOG.debug(String.format("[CacheMan]reconn %s",this.cacheServerInfo()));
		try{
			jedisConnFactory.destroy();
		}catch(Exception e){
			LOG.warn(String.format("[CacheMan]disconnect-error [%s]",this.cacheServerInfo()),e);
		}finally{
			this.initJedisConn();
		}
	}

	@Override
	public String cacheServerInfo() {
		String serverInfo = host+":"+port ;
		return serverInfo;
	}

	@Override
	public RedisTemplate<String, Object> getCacheTemplate() {
		return this.redisTemplate;
	}
	
	@Override
	public String toString(){
		return this.cacheServerInfo();
	}

}
