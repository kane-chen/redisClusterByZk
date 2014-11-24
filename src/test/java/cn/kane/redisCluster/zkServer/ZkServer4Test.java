package cn.kane.redisCluster.zkServer ;

import java.io.File;
import java.io.IOException;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;

public class ZkServer4Test {

	private File snapDir;
	private File logDir;
	/** min-time-unit.[unit=millisecond] */
	private int tickTime;
	/** server-port */
	private int port;
	/** max-client-conns */
	private int maxClientCnxns;
	/** server-conn-factory */
	private ServerCnxnFactory serverConnFactory = null ;
	public ZkServer4Test(File snapDir,File logDir,int tickTime,int port,int maxClientCnxns){
		this.snapDir = snapDir ;
		this.logDir = logDir ;
		this.tickTime = tickTime ;
		this.port = port ;
		this.maxClientCnxns = maxClientCnxns ;
	}
	
	public ZkServer4Test(int tickTime,int port,int maxClientCnxns){
		String tmpPath = System.getProperty("java.io.tmpdir");
		snapDir = new File(tmpPath, "snap");
		logDir = new File(tmpPath, "logs");
		this.tickTime = tickTime ;
		this.port = port ;
		this.maxClientCnxns = maxClientCnxns ;
	}
	
	public void startup() throws IOException, InterruptedException{
		System.out.println("try to start zkServer");
		ZooKeeperServer zkServer = new ZooKeeperServer(snapDir, logDir, tickTime) ;
		serverConnFactory = ServerCnxnFactory.createFactory(port, maxClientCnxns) ;
		serverConnFactory.startup(zkServer);
		System.out.println("zkServer started at "+port);
	}
	
	public void shutdown(){
		System.out.println("try to shutdown zkServer");
		serverConnFactory.shutdown();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ZkServer4Test zkServer = new ZkServer4Test(3000, 2181, 200) ;
		zkServer.startup();
		System.out.println("u can press any key to exit");
		System.in.read() ;
		zkServer.shutdown();
		System.out.println("server shutdown");
	}

}
