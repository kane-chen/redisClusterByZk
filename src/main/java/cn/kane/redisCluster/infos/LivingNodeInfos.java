package cn.kane.redisCluster.infos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LivingNodeInfos {

	private String groupLeader ;
	private Map<String,String> shardLeaderMapping = new HashMap<String,String>() ;
	private Map<String,List<String>> shardSlaveMapping = new HashMap<String,List<String>>() ;
	
	public String getGroupLeader() {
		return groupLeader;
	}
	public void setGroupLeader(String groupLeader) {
		this.groupLeader = groupLeader;
	}
	public Map<String, String> getShardLeaderMapping() {
		return shardLeaderMapping;
	}
	public void addShardLeader(String shardKey,String nodeKey){
		shardLeaderMapping.put(shardKey, nodeKey) ;
	}
	public Map<String, List<String>> getShardSlaveMapping() {
		return shardSlaveMapping;
	}
	public void addSlaveNodes(String shardKey,List<String> nodeKeys){
		shardSlaveMapping.put(shardKey, nodeKeys) ;
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
