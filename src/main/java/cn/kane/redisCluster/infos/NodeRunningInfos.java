package cn.kane.redisCluster.infos;

import java.util.List;
import java.util.Map;
import java.util.Random;
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
	public static final int CACHE_OPERA_TYPE_MASTER = 0 ; 
	public static final int CACHE_OPERA_TYPE_SLAVE = 1 ; 
	private static final int ReplicasNumOfShard = 5 ;
	
	private static NodeRunningInfos runningInfos  ;
	private static Object mutex = new Object() ;
	
	//nodeInfos
	private Map<String,NodeInfo> nodeInfos ;
	//shardInfos
	private Map<String,ShardInfo> shardInfos ;
	//consistentCircle
	private ConsistentHash<ShardInfo> consistentCircle ;

	private NodeRunningInfos(){
		nodeInfos = new ConcurrentHashMap<String, NodeInfo>() ;
		shardInfos = new ConcurrentHashMap<String, ShardInfo>() ;
		consistentCircle = new ConsistentHash<ShardInfo>(HashAlgorithmEnum.KETAMA_HASH,
				HashAlgorithmEnum.FNV_HASH,ReplicasNumOfShard,null) ;
	}
	
	public static NodeRunningInfos getInstance(){
		if(null != runningInfos){
			return runningInfos ;
		}else{
			synchronized (mutex) {
				if(null == runningInfos){
					runningInfos = new NodeRunningInfos() ;
				}
			}
			return runningInfos ;
		}
	}
	
	public void addShard(ShardInfo shard){
		if(!shardInfos.containsKey(shard.getShardKey())){
			shardInfos.put(shard.getShardKey(), shard);
			consistentCircle.add(shard);
		}
	}
	
	public void removeShard(String shardKey){
		LOG.info("[RunInfos] remove shard : {}",shardKey);
		ShardInfo shard = shardInfos.get(shardKey) ;
		List<String> nodeKeyList = shard.getShardFollowerNodeName() ;
		if(null!=nodeKeyList){
			for(String nodeKey : nodeKeyList){
				this.removeNode(nodeKey);
			}
		}
		shardInfos.remove(shardKey) ;
	}
	
	public void addNode(NodeInfo node){
		LOG.info("[RunInfos] add node : {}",node);
		if(nodeInfos.containsKey(node.getNodeKey())){
			throw new IllegalArgumentException("some node with the same nodeKey,please check&resolve!");
		}
		nodeInfos.put(node.getNodeKey(), node);
	}
	
	public void removeNode(String nodeKey){
		LOG.info("[RunInfos] remove node : {}",nodeKey);
		nodeInfos.remove(nodeKey) ;
	}
	
	public ShardInfo getShardByKey(String shardKey){
		return shardInfos.get(shardKey) ;
	}
	
	public NodeInfo getNodeInfoByKey(String nodeKey){
		return nodeInfos.get(nodeKey) ;
	}
	
	/**
	 * find targetCacheServer by cacheObj
	 * @param cacheObj
	 * @param opType  0-master 1-single-slave
	 * @return
	 */
	public  Map<String,Object> getCacheServerByHash(Object cacheObj,int opType){
		ShardInfo targetShardInfo = consistentCircle.get(cacheObj) ;
		String nodeKey = this.getNodeKeyOfCacheServer(targetShardInfo, opType) ;
		return nodeInfos.get(nodeKey).getCacheConnInfo() ;
	}
	
	private String getNodeKeyOfCacheServer(ShardInfo targetShardInfo,int opType){
		String nodeKey = null ;
		if(opType == CACHE_OPERA_TYPE_MASTER){
			nodeKey = targetShardInfo.getShardLeaderNodeName() ;
		}else if(opType == CACHE_OPERA_TYPE_SLAVE){
			List<String> nodeKeyList = targetShardInfo.getShardFollowerNodeName() ;
			int slavePos = this.blancePos(nodeKey, nodeKeyList.size()) ;
			nodeKey = nodeKeyList.get(slavePos) ;
		}
		return nodeKey ;
	}
	
	//TODO
	private int blancePos(String nodeKey ,int totalSize){
		Random random = new Random() ;
		return random.nextInt(totalSize) ;
	}
	
	public void printShardInfos(){
		LOG.info(String.format("[Shard-Infos]:%s",consistentCircle.getNodeHashDesc()));
	}

}
