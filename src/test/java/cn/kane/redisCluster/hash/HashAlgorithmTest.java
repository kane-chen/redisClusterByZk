package cn.kane.redisCluster.hash;

import junit.framework.TestCase;

public class HashAlgorithmTest extends TestCase {

	private String[] nodes = {"Node-1","Node-2","Node-3","Node-4"};
	private int replicasNum = 200 ;
	
	public void testFnvBalance(){
		for(String node : nodes){
			for(int i=0 ; i<replicasNum; i++){
				String key = node + "-" + i ;
				long hash = HashAlgorithmEnum.FNV_HASH.hash(key);
				System.out.println(key+":"+hash);
			}
		}
	}

	public void testKetamaBalance(){
		for(String node : nodes){
			for(int i=0 ; i<replicasNum; i++){
				String key = node + "-" + i ;
				long hash =  HashAlgorithmEnum.KETAMA_HASH.hash(key);
				System.out.println(key+":"+hash);
			}
		}
	}
}
