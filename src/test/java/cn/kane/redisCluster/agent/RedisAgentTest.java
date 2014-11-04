package cn.kane.redisCluster.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class RedisAgentTest extends TestCase{
	
	private static final Logger LOG = LoggerFactory.getLogger(RedisAgentTest.class) ;
	
	private RedisAgent redisAgent ;
	
	private Watcher watcher = new Watcher(){
		public void process(WatchedEvent event) {
			LOG.info("zk-watcher"+event);
		}
	};

	public void testRedisAgent(){
		String nodeName = "Node5";
		int redisPort = 6384 ;
		redisAgent = new RedisAgent() ;
		redisAgent.setGroupName("/demo");
		redisAgent.setNodeName(nodeName);
		redisAgent.setRedisHost("192.168.56.15");
		redisAgent.setRedisPort(redisPort);
		redisAgent.setRedisTimeOut(3000);
		redisAgent.setZkConn("127.0.0.1:2181");
		redisAgent.setZkSessionTimeOut(3000);
		redisAgent.setWatcher(watcher);
		redisAgent.startup();
		this.inputCommandHandler();
	}
	
	private void inputCommandHandler(){
		BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in)) ;
		while(true){
			String command = null ;
			try {
				command = reader.readLine();
			} catch (IOException e) {
				LOG.error("Command handle",e);
			}
			if("quit".equals(command)){
				redisAgent.shutdown();
				System.exit(0);
			}
		}
		
	}
	
}
