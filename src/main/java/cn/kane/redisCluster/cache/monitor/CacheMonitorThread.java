package cn.kane.redisCluster.cache.monitor;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.kane.redisCluster.cache.man.ICacheManageInterface;
import cn.kane.redisCluster.zookeeper.nodes.NodeInfo;

public class CacheMonitorThread extends Thread {
	
	private static final Logger LOG = LoggerFactory.getLogger(CacheMonitorThread.class) ;
	//constant
	private final static Long MONITOR_INTERRAL = 20000L;
	private final static int MONITOR_MAX_FAIL_TIMES = 3;
	/* instance */
	private ICacheManageInterface<?> cacheMan;
	private NodeInfo nodeInfo ;
	/* running flag */
	private AtomicBoolean isWorking = new AtomicBoolean(true) ;
	
	public CacheMonitorThread(NodeInfo nodeInfo) {
		this.nodeInfo = nodeInfo ;
		this.cacheMan = nodeInfo.getCacheMan() ;
	}
	public CacheMonitorThread(NodeInfo nodeInfo,String thrName) {
		this.nodeInfo = nodeInfo ;
		this.cacheMan = nodeInfo.getCacheMan() ;
		this.setName(thrName);
	}

	@Override
	public void run() {
		long lastRunMillis = 0L;
		int failTimes = 0;
		while (isWorking.get()) {
			long curMillis = System.currentTimeMillis();
			if (curMillis - lastRunMillis > MONITOR_INTERRAL) {
				lastRunMillis = curMillis;
				String pingResult = null;
				try {
					pingResult = cacheMan.ping();
				} catch (Exception e) {
					LOG.error(String.format("[Monitor]error [%s]",cacheMan.cacheServerInfo()), e);
					try{
						cacheMan.reConn();
					}catch(Exception e1){
						LOG.error(String.format("[Monitor]reconn-error [%s]",cacheMan.cacheServerInfo()), e1);
					}
				}
				if ("OK".equals(pingResult)) {
					LOG.info(String.format("[Monitor] ping success [%s]",cacheMan.cacheServerInfo()));
					failTimes = 0;
					if(!nodeInfo.isWorking()){
						try {
							nodeInfo.reg();
						} catch (Exception e) {
							LOG.error("[Zk-REG] ERROR",e);
						}
					}
				} else {
					failTimes++;
					if (failTimes >= MONITOR_MAX_FAIL_TIMES) {
						LOG.info(String.format("[Monitor] exceed max-reconn times [%s]",cacheMan.cacheServerInfo()));
						// zkClient remove
						try {
							if(nodeInfo.isWorking()){
								nodeInfo.unReg();
							}
						} catch (Exception e) {
							LOG.error("[Zk-UnReg] ERROR",e);
						}
						LOG.error(String.format("[Monitor] exceed max-reconn times,ping failed [%s]",cacheMan.cacheServerInfo()));
						
					}
					LOG.warn(String.format("[Monitor] ping failed [%s]",cacheMan.cacheServerInfo()));
				}
			} else {
				// do nothing,recycle
			}
		}
	}
	
	public void shutdown(){
		LOG.info("[Monitor] stop {}",cacheMan.cacheServerInfo());
		this.isWorking.set(false);
		LOG.info("[Monitor] stop done {}",cacheMan.cacheServerInfo());
	}

}