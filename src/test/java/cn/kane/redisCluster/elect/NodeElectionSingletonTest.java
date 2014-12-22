package cn.kane.redisCluster.elect;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.election.LeaderElectionNode4Alltry;
import cn.kane.redisCluster.election.LeaderElectionNode4Ephemeral;
import cn.kane.redisCluster.cache.man.ICacheManageInterface;
import cn.kane.redisCluster.cache.man.JedisCacheManageService;

import junit.framework.TestCase;

public class NodeElectionSingletonTest extends TestCase {

	private static final Logger LOG = LoggerFactory.getLogger(NodeElectionSingletonTest.class) ;
	private String groupName = "/demo";
	private int sessionTimeOut = 3000;
	private String conn = "127.0.0.1:2181";
	
	private Watcher watcher = new Watcher(){
		public void process(WatchedEvent event) {
			LOG.info("zk-watcher"+event);
		}
	};

	public void testLeaderElection() throws IOException, KeeperException, InterruptedException{
		String nodeName = "Node-1" ;
		String redisHost = "192.168.56.15" ;
		int redisPort = 6380 ;
		int redisTimeout = 3000 ;
		Thread.currentThread().setName(nodeName);
//		new LeaderElectionNode(groupName, nodeName, sessionTimeOut, conn, watcher) ;
//		new LeaderElectionNode4Ephemeral(groupName, nodeName, sessionTimeOut, conn, watcher) ;
		new LeaderElectionNode4Alltry(groupName, nodeName, sessionTimeOut, conn, watcher,redisHost,redisPort,redisTimeout) ;
		Thread.sleep(200000);
	}
	
	public void testCleanDatas() throws IOException, InterruptedException, KeeperException{
		ZookeeperDatasUtils.deleteNode(conn, sessionTimeOut, groupName+LeaderElectionNode4Ephemeral.LEADER_DATA_NODE);
	}
	
	public void testJedisPing(){
		String host = "192.168.56.15";
		int port = 6381 ;
		int timeOut = 5000;
		ICacheManageInterface<?> jedisClient = new JedisCacheManageService(host,port,timeOut);
		Assert.assertEquals("PONG",jedisClient.ping());
	}
}
