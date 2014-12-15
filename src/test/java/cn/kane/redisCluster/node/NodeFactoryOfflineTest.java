package cn.kane.redisCluster.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.kane.redisCluster.cache.proxy.CacheServiceInLocalMem;
import cn.kane.redisCluster.cache.proxy.ICacheService;
import junit.framework.TestCase;

public class NodeFactoryOfflineTest extends TestCase {

	private static final Logger LOG = LoggerFactory.getLogger(NodeFactoryOfflineTest.class) ;
	ApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext-test.xml");
	@Autowired
	private NodeFactory nodeFactory ;
	private ICacheService cacheService ;
	
	
	@Override
	public void setUp(){
		cacheService = new CacheServiceInLocalMem() ;
	}
	
	@Test
	public void testApp() throws IOException{
		this.commandHandler();
	}
	
	private void commandHandler() throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)) ;
		while(true){
			String command = reader.readLine() ;
			if("quit".equals(command)){
				System.exit(0);
			}
			String[] params = command.split(" ") ;
			if("put".equals(params[0])){//cache put
				if(params.length<3){
					LOG.warn("illegal params");
				}
				String key =params[1] ;
				String value = params[2] ;
				cacheService.put(key, value);
			}else if("get".equals(params[0])){//cache get
				if(params.length<2){
					LOG.warn("illegal params");
				}
				String value = cacheService.get(params[1]) ;
				System.out.println("[Result]"+value);
			}else if("rem".equals(params[0])){//cache rem
				if(params.length<2){
					LOG.warn("illegal params");
				}
				cacheService.remove(params[1]) ;
			}else if("add".equals(params[0])){
				this.addNode(params);
			}
		}
	}
	
	private void addNode(String[] params){
		if(params.length<9){
			LOG.warn("illegal params");
		}
		String groupName = params[1] ;
		String shardName = params[2] ;
		String nodeName = params[3] ;
		//zookeeper
		String zkConnStr = params[4] ;
		int zkSessionTimeOut = Integer.parseInt(params[5]) ;
		//cache host&port
		String cacheHost = params[6] ;
		int cachePort = Integer.parseInt(params[7]) ;
		int cacheTimeout = Integer.parseInt(params[8]) ;
		NodeConfig nodeConf = new NodeConfig() ;
		nodeConf.setGroupName(groupName);
		nodeConf.setShardName(shardName);
		nodeConf.setNodeName(nodeName);
		nodeConf.setCacheHost(cacheHost);
		nodeConf.setCachePort(cachePort);
		nodeConf.setCacheTimeout(cacheTimeout);
		nodeConf.setZkConnStr(zkConnStr);
		nodeConf.setZkSessionTimeOut(zkSessionTimeOut);
		try {
			nodeConf.init4Test();
			nodeFactory.addNode(nodeConf);
		} catch (Exception e) {
			LOG.error("addNode error",e);
		}
	}
}
