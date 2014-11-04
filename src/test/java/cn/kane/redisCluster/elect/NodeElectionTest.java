package cn.kane.redisCluster.elect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.KeeperException;

import cn.kane.redisCluster.election.LeaderElectionNode;
import junit.framework.TestCase;

public class NodeElectionTest extends TestCase {

	
	private String groupName = "/demo";
	private int sessionTimeOut = 3000;
	private String conn = "127.0.0.1:2181";

	public void testLeaderElection(){
		List<Thread> threads = new ArrayList<Thread>() ;
		for(int i=1 ; i<=5 ; i++){
			String nodeName = "Node-"+i ;
			Runnable runna = new ElectionThread() ;
			Thread thr = new Thread(runna,nodeName);
			thr.start();
			threads.add(thr) ;
		}
		for(Thread thr : threads){
			try {
				thr.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ElectionThread implements Runnable{
		
		public void run() {
			String nodeName = Thread.currentThread().getName() ;
			Random random = new Random() ;
			try {
				int reconnTimes = random.nextInt(5) ;
				for(int i=0 ; i<=reconnTimes ; i++){
					new LeaderElectionNode(groupName, nodeName, sessionTimeOut, conn, null) ;
					Thread.sleep(random.nextInt(3000));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
