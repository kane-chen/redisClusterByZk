package cn.kane.redisCluster.zookeeper.nodes;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.cache.man.ICacheManageInterface;
import cn.kane.redisCluster.cache.monitor.CacheMonitorThread;
import cn.kane.redisCluster.infos.NodeRunningInfos;
import cn.kane.redisCluster.zookeeper.watchers.LogBaseWatcher;
import cn.kane.redisCluster.zookeeper.watchers.ShardProposerWatcher;

public class NodeInfo implements Serializable{

	private static final long serialVersionUID = -3322602428503004271L;
	private static final Logger LOG = LoggerFactory.getLogger(NodeInfo.class) ;
	
	private static final int ZK_CONN_MAX_RETRY_TIME = 1 ;
	
	private GroupInfo group ;
	private ShardInfo shard ;

	private String nodeName ;
	private String nodeKey ;
	private String nodePath ;
	private boolean isShardLeader ;
	private boolean isGroupLeader ;
	
	private ZooKeeper zkClient ;
	private String zkConnStr ;
	private int zkSessionTimeout ;
	private ICacheManageInterface cacheMan ;
	
	private AtomicBoolean isWorking = new AtomicBoolean(false) ;
	private CacheMonitorThread monitorThr ;
	
	public NodeInfo(String nodeName,GroupInfo group,ShardInfo shard,ZooKeeper zkClient,String zkConnStr,int zkSessionTimeout,ICacheManageInterface cacheMan){
		this.nodeName = nodeName ;
		this.nodeKey = shard.getShardKey() + NodeRunningInfos.NODE_INFO_KEY_SPLITOR + nodeName ;
		this.shard = shard ;
		this.group = group ;
		this.zkClient = zkClient ;
		this.zkConnStr = zkConnStr ;
		this.zkSessionTimeout = zkSessionTimeout ;
		this.cacheMan = cacheMan ;
	}
	
	public boolean isWorking(){
		return isWorking.get() ;
	}
	
	public void build() throws KeeperException, InterruptedException{
		if(this.isWorking()){
			LOG.warn(String.format("[Node]is already working [%s]",this));
			return ;
		}
		//registry
		this.reg();
		//monitor
		monitorThr = new CacheMonitorThread(cacheMan,this,nodeKey+"-monitor");
		monitorThr.start() ;
		//infos
		NodeRunningInfos.getInstance().addNode(this);
	}
	
	public void destroy() throws InterruptedException{
		this.unReg();
		monitorThr.shutdown();
	}
	
	public void reg() throws KeeperException, InterruptedException{
		//check cache-conn
		if(!"OK".equals(cacheMan.ping())){
			LOG.warn(String.format("[Cache] ping failed [%s] ", cacheMan));
			return ;
		}
		//zk-create
		this.createZkNodeInfos();
		
	}
	
	private void createZkNodeInfos(){
		int retryTimes = 1 ; 
		while(retryTimes <= ZK_CONN_MAX_RETRY_TIME){
			try{
				//create node
				//TODO PERSISTENCE_NODE(for zk-disconnected)
				this.nodePath = zkClient.create(shard.getShardPath()+"/",
						nodeName.getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
				LOG.info(String.format("[Node] created [%s]",nodePath));
				//add-watcher
				ShardProposerWatcher shardLeaderWatcher = new ShardProposerWatcher(zkClient,cacheMan,this);
				shardLeaderWatcher.addWatcher(shard.getShardLeaderPath());
				isWorking.set(true);
				return ;
			}catch(Exception e){
				//retryTimes
				retryTimes++ ;
				LOG.warn(String.format("[Node] zkClient-connection not-alive"),e);
				//close first
				try{
					zkClient.close();
				}catch(Exception e1){
					LOG.error("[Zk]close not-alive conn error",e1);
				}
				//reconn
				try {
					zkClient = new ZooKeeper(zkConnStr, zkSessionTimeout, new LogBaseWatcher());
				} catch (IOException e1) {
					LOG.error("[Zk]init conn error",e1);
				}
			}
		}
		
	}
	
	public void unReg() throws InterruptedException{
		LOG.info(String.format("[Node] unreg [%s]",this));
		zkClient.close();
		isWorking.set(false);
		NodeRunningInfos.getInstance().removeNode(nodeKey);
		LOG.info(String.format("[Node] unreg done [%s]",this));
	}
	
	public Map<String,Object> getCacheConnInfo(){
		Map<String,Object> cacheConnInfo = null ;
		String cacheServerInfo = cacheMan.cacheServerInfo() ;
		if(StringUtils.isNoneBlank(cacheServerInfo)){
			String[] infos = cacheServerInfo.split(":") ;
			if(infos.length == 2){
				cacheConnInfo = new HashMap<String,Object>() ;
				cacheConnInfo.put("ip", infos[0]) ;
				cacheConnInfo.put("port", Integer.parseInt(infos[1]));
			}
		}
		return cacheConnInfo ;
	}
	

	public String getNodeName() {
		return nodeName;
	}
	public String getNodeKey() {
		return nodeKey;
	}
	public String getNodePath() {
		return nodePath;
	}
	public boolean isShardLeader() {
		return isShardLeader;
	}
	public void setShardLeader(boolean isShardLeader) {
		this.isShardLeader = isShardLeader;
	}
	public boolean isGroupLeader() {
		return isGroupLeader;
	}
	public void setGroupLeader(boolean isGroupLeader) {
		this.isGroupLeader = isGroupLeader;
	}
	public GroupInfo getGroup() {
		return group;
	}
	public ShardInfo getShard() {
		return shard;
	}

	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE) ;
	}
	
}
