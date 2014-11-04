package cn.kane.redisCluster.elect;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

public class ZookeeperDatasUtils {

	private static final String PATH_SPILTOR = "/" ;
	
	public static String createPath(String conn,int timeOut,String path,byte[] data, 
			List<ACL> acl, CreateMode createMode) throws IOException, KeeperException, InterruptedException{
		if(null == conn || path == null){
			return null ;
		}
		ZooKeeper zkClient = new ZooKeeper(conn, timeOut, null) ;
		if(!path.startsWith(PATH_SPILTOR)){
			path = PATH_SPILTOR + path ;
		}
		if(!path.endsWith(PATH_SPILTOR)){
			path = path + PATH_SPILTOR ;
		}
		return zkClient.create(path, data, acl, createMode) ;
	}
	
	public static void delChildsInPath(String conn,int timeOut,String path) throws IOException, KeeperException, InterruptedException{
		ZooKeeper zkClient = new ZooKeeper(conn, timeOut, null) ;
		List<String> nodes = zkClient.getChildren(path, null) ;
		for(String node : nodes){
			zkClient.delete(path+PATH_SPILTOR+node, -1);
		}
	}
	
	public static void deleteNode(String conn,int timeOut,String nodePath) throws IOException, InterruptedException, KeeperException{
		ZooKeeper zkClient = new ZooKeeper(conn, timeOut, null) ;
		zkClient.delete(nodePath, -1);
	}
}
