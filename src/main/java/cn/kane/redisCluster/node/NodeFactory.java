package cn.kane.redisCluster.node;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.jedis.JedisClient;
import cn.kane.redisCluster.zookeeper.nodes.GroupInfo;
import cn.kane.redisCluster.zookeeper.nodes.NodeInfo;
import cn.kane.redisCluster.zookeeper.nodes.ShardInfo;
import cn.kane.redisCluster.zookeeper.watchers.ShardLeaderNodeWatcher;

public class NodeFactory {

	private static final Logger LOG = LoggerFactory.getLogger(NodeFactory.class);
	
	public NodeInfo createNode(String groupName,String shardName,String nodeName, ZooKeeper zkClient,JedisClient jedisClient) throws KeeperException, InterruptedException{
		GroupInfo group = this.createGroup(groupName, zkClient) ;
		ShardInfo shard = this.createShard(shardName, group, zkClient);
		NodeInfo node = this.createNode(nodeName, group, shard, zkClient,jedisClient) ;
		return node ;
	}
	
	private GroupInfo createGroup(String groupName,ZooKeeper zkClient) throws KeeperException, InterruptedException{
		GroupInfo group = new GroupInfo(groupName) ;
		String groupPath = group.getGroupPath();
		String groupNodesDataNode = group.getLeaderNodeName();
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
			zkClient.create(shardPath, shardName.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		return shard ;
	}
	private NodeInfo createNode(String nodeName,GroupInfo group,ShardInfo shard,ZooKeeper zkClient,JedisClient jedisClient) throws KeeperException, InterruptedException{
		NodeInfo node = new NodeInfo(nodeName,group,shard);
		//create node
		String nodePath = zkClient.create(shard.getShardPath(),
				nodeName.getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
		LOG.info("node create {}",nodePath);
		//add-watcher
		ShardLeaderNodeWatcher shardLeaderWatcher = new ShardLeaderNodeWatcher(zkClient,jedisClient,node);
		shardLeaderWatcher.addWatcher(shard.getShardLeaderPath());
		//be-leader
		return node ;
	}
}
