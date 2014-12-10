package cn.kane.redisCluster.node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.hash.ConsistentHash;
import cn.kane.redisCluster.hash.HashAlgorithmEnum;
import cn.kane.redisCluster.zookeeper.nodes.NodeInfo;
import cn.kane.redisCluster.zookeeper.nodes.ShardInfo;

public class NodeRunningInfos {
	
	private static final Logger LOG = LoggerFactory.getLogger(NodeRunningInfos.class) ;

	public static final String NODE_INFO_KEY_SPLITOR = "##" ;
	private static final int ReplicasNumOfShard = 5 ;
	
	//nodeInfos
	private static Map<String,NodeInfo> nodeInfos ;
	//shardInfos
	private static Map<String,ShardInfo> shardInfos ;
	//consistentCircle
	private static ConsistentHash<ShardInfo> consistentCircle ;

	static{
		nodeInfos = new ConcurrentHashMap<String, NodeInfo>() ;
		shardInfos = new ConcurrentHashMap<String, ShardInfo>() ;
		consistentCircle = new ConsistentHash<ShardInfo>(HashAlgorithmEnum.KETAMA_HASH,
				HashAlgorithmEnum.FNV_HASH,ReplicasNumOfShard,null) ;
	}
	
	public static void addShard(ShardInfo shard){
		shardInfos.put(shard.getShardKey(), shard);
		consistentCircle.add(shard);
	}
	
	public static void addNode(NodeInfo node){
		if(nodeInfos.containsKey(node.getNodeKey())){
			throw new IllegalArgumentException("some node with the same nodeKey,please check&resolve!");
		}
		nodeInfos.put(node.getNodeKey(), node);
	}
	
	public static NodeInfo getNodeInfoByKey(String nodeKey){
		return nodeInfos.get(nodeKey) ;
	}
	
	public static Map<String,Object> getCacheServerByHash(Object cacheObj){
		ShardInfo targetShardInfo = consistentCircle.get(cacheObj) ;
		String nodeKey = targetShardInfo.getShardLeaderNodeName() ;
		return nodeInfos.get(nodeKey).getCacheConnInfo() ;
	}
	
	public void printShardInfos(){
		LOG.info(String.format("[Shard-Infos]:%s",consistentCircle.getNodeHashDesc()));
	}
}
