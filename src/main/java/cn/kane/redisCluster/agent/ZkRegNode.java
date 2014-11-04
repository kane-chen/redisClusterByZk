package cn.kane.redisCluster.agent;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.hash.ConsistentHash;

public class ZkRegNode {

	private static final Logger LOG = LoggerFactory.getLogger(ZkRegNode.class);
	/* 一致性hash虚拟节点数  */
	private static final int CONSISTENTHASH_REPLICAS_NUM = 3 ;
	//path-constant
	public static final String LEADER_DATA_NODE = "/leader";
	public static final String LIVINGS_DATA_NODE = "/living_nodes";
	private static final String LEADER_STANDBY_PATH = "/leader_standby";
	private static final int ANY_VERSION = -1;
	// zk-conn
	private String zkConn;
	private int zkSessionTimeOut;
	private ZooKeeper zkClient = null;
	// watcher
	private Watcher watcher;
	private Watcher leaderWatcher;
	private Watcher leaderStandbyWatcher;
	//redis
	private String redisHost;
	private int redisPort;
	@SuppressWarnings("unused")
	private int redisTimeOut;
	// node-data
	private String groupName;
	private String leaderDataNode;
	private String livingsDataNode;
	private String leaderStandbyPath;
	private String nodeName;
	private String leaderSeq;

	private boolean isReg = false ;
	
	public ZkRegNode(String groupName, String nodeName, int sessionTimeOut,	String zkConn, Watcher watcher, 
			String redisHost, int redisPort,int redisTimeOut) {
		// node
		this.groupName = groupName;
		leaderDataNode = groupName + LEADER_DATA_NODE;
		leaderStandbyPath = groupName + LEADER_STANDBY_PATH;
		livingsDataNode = groupName + LIVINGS_DATA_NODE;
//		this.nodeName = nodeName;
		this.nodeName = redisHost+":"+redisPort ;
		this.zkConn = zkConn;
		this.zkSessionTimeOut = sessionTimeOut;
		// watcher
		this.watcher = watcher;
		//redis
		this.redisHost = redisHost ;
		this.redisPort = redisPort ;
		this.redisTimeOut = redisTimeOut ;
	}

	public void reg() throws IOException, KeeperException, InterruptedException{
		LOG.info(String.format("[REG] zk[%s]-Redis[%s:%s]-Data[%s/%s]",zkConn,redisHost,redisPort,groupName,nodeName));
		this.leaderWatcher = new LeaderNodeWatcher();
		this.leaderStandbyWatcher = new LeaderStandbyWatcher();
		zkClient = new ZooKeeper(zkConn, zkSessionTimeOut, watcher);
		// group-root path
		if (null == zkClient.exists(groupName, watcher)) {
			zkClient.create(groupName, new byte[4],ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		// living_nodes path
		if (null == zkClient.exists(livingsDataNode, watcher)) {
			zkClient.create(livingsDataNode, new byte[4],ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		// standby
		this.addSelf2StandbyList();
		this.setReg(true);
		LOG.info(String.format("[REG] done zk[%s]-Redis[%s:%s]-Data[%s/%s]",zkConn,redisHost,redisPort,groupName,nodeName));
	}
	
	public void unreg() throws InterruptedException{
		LOG.info(String.format("[UNREG] zk[%s]-Redis[%s:%s]-Data[%s/%s]",zkConn,redisHost,redisPort,groupName,nodeName));
		zkClient.close();
		this.setReg(false);
		LOG.info(String.format("[UNREG] done zk[%s]-Redis[%s:%s]-Data[%s/%s]",zkConn,redisHost,redisPort,groupName,nodeName));
	}
	
	private void addSelf2StandbyList() throws KeeperException,InterruptedException {
		// register in leader-standby
		leaderSeq = zkClient.create(leaderStandbyPath + "/",
				nodeName.getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
		LOG.info("[Standby]standby-node-path:"+leaderSeq);
		this.checkLeader();
	}

	private void checkLeader() throws KeeperException, InterruptedException {
		boolean isLeaderNodeExist = true;
		boolean isLeaderNodeValid = true;
		if (null == zkClient.exists(leaderDataNode, watcher)) {
			isLeaderNodeExist = false;
		}
		if (isLeaderNodeExist) {
			byte[] leaderNodeBytes = zkClient.getData(leaderDataNode,leaderWatcher, null);
			if (null == leaderNodeBytes) {
				isLeaderNodeValid = false;
			}
		}
		if (isLeaderNodeExist && isLeaderNodeValid) {
			LOG.info("i am follower");
		} else {
			this.tryToBeLeader();
		}
	}

	private void tryToBeLeader() {
		LOG.info("[ToBeLeader]-start " + nodeName);
		// beLeader
		try {
			zkClient.create(leaderDataNode, nodeName.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			// watch child-change
			List<String> livingNodes = zkClient.getChildren(leaderStandbyPath,leaderStandbyWatcher);
			String livingNodesInStr = this.getLivingNodesInString(livingNodes);
			zkClient.setData(livingsDataNode, livingNodesInStr.getBytes(),ANY_VERSION);
			LOG.info("[ToBeLeader]-[NEW] i am leader " + nodeName);
		} catch (KeeperException e) {
			LOG.info("[ToBeLeader]-[NEW] i am follower");
			try {
				byte[] leaderNodeBytes = zkClient.getData(leaderDataNode,leaderWatcher, null);
				LOG.info("[NewLeader]" + new String(leaderNodeBytes));
			} catch (Exception e1) {
				LOG.error("ToBeLeader new-leader", e1);
			}
		} catch (InterruptedException e) {
			LOG.error("[ToBeLeader] interrupt", e);
		}
	}

	private String getLivingNodesInString(List<String> livingNodes) {
		StringBuffer livingBuffer = new StringBuffer();
		if (null != livingNodes) {
			for (String node : livingNodes) {
				livingBuffer.append(node).append(",");
			}
		}
		return livingBuffer.toString();
	}

	public boolean isReg() {
		return isReg;
	}

	private void setReg(boolean isReg) {
		this.isReg = isReg;
	}

	class LeaderNodeWatcher implements Watcher {
		public void process(WatchedEvent event) {
			try {
				ZkRegNode.this.checkLeader();
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					String newLeader = new String(zkClient.getData(event.getPath(), false, null));
					LOG.info("[Leader-Change] newLeader = " + newLeader);
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class LeaderStandbyWatcher implements Watcher {
		public void process(WatchedEvent event) {
			try {
				List<String> livingNodes = zkClient.getChildren(leaderStandbyPath,leaderStandbyWatcher);
				ConsistentHash<String> consistentHash = new ConsistentHash<String>(ZkRegNode.CONSISTENTHASH_REPLICAS_NUM, livingNodes); 
				String nodeHashDesc = consistentHash.getNodeHashDesc();
				zkClient.setData(livingsDataNode, nodeHashDesc.getBytes(),ANY_VERSION);
				LOG.info("[Leader-Standby Change] livingNodes = "+ nodeHashDesc);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}