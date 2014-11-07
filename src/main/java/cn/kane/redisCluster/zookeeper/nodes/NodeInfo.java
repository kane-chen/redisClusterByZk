package cn.kane.redisCluster.zookeeper.nodes;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class NodeInfo implements Serializable{

	private static final long serialVersionUID = -3322602428503004271L;
	
	private GroupInfo group ;
	private ShardInfo shard ;
	
	private String nodePath ;
	private boolean isShardLeader ;
	private boolean isGroupLeader ;
	
	public NodeInfo(String nodeName,GroupInfo group,ShardInfo shard){
		this.nodePath = shard.getShardPath()+ "/" + nodeName ;
		this.shard = shard ;
		this.group = group ;
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
