package cn.kane.redisCluster.hash;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ConsistentHashTest extends TestCase {

	private int replicasNum = 3 ;
	
	public void testConsistentHash(){
		List<String> nodeList = new ArrayList<String>();
		nodeList.add("Node-1");
		nodeList.add("Node-2");
		nodeList.add("Node-3");
		ConsistentHash<String> conHash = new ConsistentHash<String>(HashAlgorithmEnum.KETAMA_HASH,
				HashAlgorithmEnum.FNV_HASH, replicasNum, nodeList) ;
		System.out.println(conHash.getNodeHashDesc());
		conHash.add("Node-4");
		System.out.println(conHash.getNodeHashDesc());
		conHash.remove("Node-3");
		System.out.println(conHash.getNodeHashDesc());
	}
}
