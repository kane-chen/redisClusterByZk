package cn.kane.redisCluster.node;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.cache.man.ICacheManageInterface;
import cn.kane.redisCluster.infos.NodeRunningInfos;
import cn.kane.redisCluster.zookeeper.nodes.GroupInfo;
import cn.kane.redisCluster.zookeeper.nodes.NodeInfo;
import cn.kane.redisCluster.zookeeper.nodes.ShardInfo;

public class NodeFactory {

	private static final Logger LOG = LoggerFactory.getLogger(NodeFactory.class);
	
	/* input */
	private List<NodeConfig> nodeConfigs ;
	
	
	public void startup(){
		LOG.info("[NodeFactory]startuping");
		if(null==nodeConfigs || nodeConfigs.isEmpty()){
			LOG.warn("[NodeFactory]no node configs!!");
			return ;
		}
		//node initialing
		for(NodeConfig nodeConfig : nodeConfigs){
			try{
				this.addNode(nodeConfig);
			}catch(Exception e){
				LOG.error(String.format("[NodeFactory]create-node-error:%s",nodeConfig),e);
			}
		}
	}
	
	public void addNode(NodeConfig nodeConf) throws KeeperException, InterruptedException{
		LOG.info("[Node] addNode:{}",nodeConf);
		NodeInfo nodeInfo = this.createNode(nodeConf) ;
		LOG.info("[Node] addNode done:{}",nodeInfo);
	}
	
	public void removeNode(String nodeKey) throws InterruptedException{
		LOG.info("[Node] removeNode:{}",nodeKey);
		NodeInfo nodeInfo = NodeRunningInfos.getInstance().getNodeInfoByKey(nodeKey) ;
		nodeInfo.destroy();
		LOG.info("[Node] removeNode done:{}",nodeKey);
	}
	
	//create Node entrance
	private NodeInfo createNode(NodeConfig nodeConfig) throws KeeperException, InterruptedException{
		String groupName = nodeConfig.getGroupName();
		String shardName = nodeConfig.getShardName();
		String nodeName = nodeConfig.getNodeName();
		ZooKeeper zkClient = nodeConfig.getZkClient();
		ICacheManageInterface cacheMan = nodeConfig.getCacheMan() ;
		GroupInfo group = this.createGroup(groupName, zkClient) ;
		ShardInfo shard = this.createShard(shardName, group, zkClient);
		NodeInfo node = this.createNode(nodeName, group, shard, zkClient,cacheMan,nodeConfig) ;
		return node ;
	}
	
	private GroupInfo createGroup(String groupName,ZooKeeper zkClient) throws KeeperException, InterruptedException{
		GroupInfo group = new GroupInfo(groupName) ;
		String groupPath = group.getGroupPath();
		String groupNodesDataNode = group.getChildsRootPath();
		String livingsDataNode = group.getLivingDataNode();
		// group-root path
		if (null == zkClient.exists(groupPath, null)) {
			zkClient.create(groupPath, groupName.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		// group-nodes path
		if (null == zkClient.exists(groupNodesDataNode, null)) {
			zkClient.create(groupNodesDataNode, new byte[4],ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		// living_nodes path
		if (null == zkClient.exists(livingsDataNode, null)) {
			zkClient.create(livingsDataNode, new byte[4],ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		return group ;
	}
	private ShardInfo createShard(String shardName,GroupInfo group ,ZooKeeper zkClient) throws KeeperException, InterruptedException{
		ShardInfo shard = new ShardInfo(shardName,group) ;
		String shardPath = shard.getShardPath() ;
		// shard-root path
		if (null == zkClient.exists(shardPath, null)) {
			zkClient.create(shardPath, shard.getShardKey().getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		NodeRunningInfos.getInstance().addShard(shard);
		return shard ;
	}
	private NodeInfo createNode(String nodeName,GroupInfo group,ShardInfo shard,ZooKeeper zkClient,ICacheManageInterface cacheMan,NodeConfig nodeConfig) throws KeeperException, InterruptedException{
		String zkConnStr = nodeConfig.getZkConnStr() ;
		int zkSessionTimeout = nodeConfig.getZkSessionTimeOut() ;
		NodeInfo node = new NodeInfo(nodeName,group,shard,zkClient,zkConnStr,zkSessionTimeout,cacheMan);
		node.build();
		return node ;
	}

	public List<NodeConfig> getNodeConfigs() {
		return nodeConfigs;
	}

	public void setNodeConfigs(List<NodeConfig> nodeConfigs) {
		this.nodeConfigs = nodeConfigs;
	}
	
}
