package cn.kane.redisCluster.zookeeper.watchers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.zookeeper.ZooKeeper;

import cn.kane.redisCluster.infos.LivingNodeInfos;
import cn.kane.redisCluster.infos.NodeRunningInfos;
import cn.kane.redisCluster.zookeeper.nodes.ShardInfo;
import cn.kane.redisCluster.zookeeper.nodes.ZkNodeConstant;

public class GroupLeaderWatcher extends LeaderWatcher {

	private static final int ANY_VERSION = -1 ;
	private ZooKeeper zkClient;

	public GroupLeaderWatcher(ZooKeeper zkClient, String nodeName) {
		this.zkClient = zkClient;
	}
	
	@Override
	public boolean addWatcher(String path) {
		try {
			//groupLeaderName
			String groupLeaderName = this.getLeaderNodeName(path) ;
			LivingNodeInfos livingNodesInfo = new LivingNodeInfos() ;
			livingNodesInfo.setGroupLeader(groupLeaderName);
			// watch child-change
			List<String> shardPaths = zkClient.getChildren(path,false);
			for(String shardPath : shardPaths){
				String shardKey = this.getNodeData(shardPath) ;
				String shardLeaderName = this.getLeaderNodeName(shardPath) ;
				List<String> nodesInSlavePath = zkClient.getChildren(shardPath, false) ;
				//shard empty
				if(null == nodesInSlavePath || nodesInSlavePath.isEmpty()){
					//remove shard
					NodeRunningInfos.getInstance().removeShard(shardKey);
					continue ;
				}
				livingNodesInfo.addShardLeader(shardKey, shardLeaderName);
				//node
				ShardInfo shardInfo = NodeRunningInfos.getInstance().getShardByKey(shardKey) ;
				List<String> remainNodes = new CopyOnWriteArrayList<String>() ;
				List<String> lastShardNodes = shardInfo.getShardFollowerNodeName() ;
				for(String nodePath : nodesInSlavePath){
					String nodeKey = this.getNodeData(nodePath) ;
					remainNodes.add(nodeKey);
				}
				//to-remove
				for(String nodeKey : lastShardNodes){
					if(!remainNodes.contains(nodeKey)){
						NodeRunningInfos.getInstance().removeNode(nodeKey);
					}
				}
				shardInfo.setShardFollowerNodeName(remainNodes);
				livingNodesInfo.addSlaveNodes(shardKey, remainNodes);
			}
			//JSON
			String livingNodesInStr = livingNodesInfo.toString() ;
			zkClient.setData(path, livingNodesInStr.getBytes(),	ANY_VERSION);
		} catch (Exception e) {
			LOG.error("[LivingNodes-change] exception", e);
		}
		return false;
	}

	private String getLeaderNodeName(String path){
		String leaderPath = path+"/"+ZkNodeConstant.LEADER_NODE ;
		String leaderNodeName = this.getNodeData(leaderPath) ;
		return leaderNodeName ;
	}
	
	private String getNodeData(String path){
		String nodeData = null ;
		try {
			byte[] nodeDataBytes = zkClient.getData(path, false, null);
			nodeData = new String(nodeDataBytes) ;
		} catch (Exception e) {
			LOG.error("[Node-Data] error",e);
		}
		return nodeData ;
	}
	
	@Override
	public void addNextWacther() {
		//nothing
	}

}
