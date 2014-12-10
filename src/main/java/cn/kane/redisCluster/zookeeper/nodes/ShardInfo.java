package cn.kane.redisCluster.zookeeper.nodes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.kane.redisCluster.node.NodeRunningInfos;

public class ShardInfo implements Serializable{

	private static final long serialVersionUID = -707440552951524978L;
	private GroupInfo group ;
	private String shardName ;
	private String shardKey ;
	private String shardPath ;
	private String shardLeaderPath ;
	private String shardLeaderNodeName ;
	private Set<String> shardFollowerNodeName ;
	
	public ShardInfo(String shardName,GroupInfo group){
		this.group = group ;
		this.shardName = shardName ;
		this.shardKey = group.getGroupName() + NodeRunningInfos.NODE_INFO_KEY_SPLITOR + shardName ;
		this.shardPath = group.getGroupPath() + "/" + shardName ;
		this.shardLeaderPath = this.shardPath + ZkNodeConstant.LEADER_NODE ;
		shardFollowerNodeName = new HashSet<String>() ;
	}
	
	public String getShardName() {
		return shardName;
	}
	public String getShardKey() {
		return shardKey;
	}
	public String getShardPath() {
		return shardPath;
	}
	public void setShardPath(String shardPath) {
		this.shardPath = shardPath;
	}
	public String getShardLeaderPath() {
		return shardLeaderPath;
	}
	public GroupInfo getGroup() {
		return group;
	}
	
	public String getShardLeaderNodeName() {
		return shardLeaderNodeName;
	}
	
	public void setShardLeaderNodeName(String shardLeaderNodeName) {
		this.shardLeaderNodeName = shardLeaderNodeName;
	}

	public Set<String> getShardFollowerNodeName() {
		return shardFollowerNodeName;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE) ;
	}

}
