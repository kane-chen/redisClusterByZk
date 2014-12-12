package cn.kane.redisCluster.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.kane.redisCluster.cache.proxy.CacheServiceInLocalMem;
import cn.kane.redisCluster.cache.proxy.ICacheService;
import junit.framework.TestCase;

public class NodeFactoryOfflineTest extends TestCase {

	ApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext-test.xml");
	
	private ICacheService cacheService ;
	
	
	@Override
	public void setUp(){
		cacheService = new CacheServiceInLocalMem() ;
	}
	
	@Test
	public void testApp() throws IOException{
		this.commandHandler();
	}
	
	private void commandHandler() throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)) ;
		while(true){
			String command = reader.readLine() ;
			if("quit".equals(command)){
				System.exit(0);
			}
			String[] params = command.split(" ") ;
			if("put".equals(params[0])){
				if(params.length<3){
					System.out.println("illegal params");
				}
				String key =params[1] ;
				String value = params[2] ;
				cacheService.put(key, value);
			}else if("get".equals(params[0])){
				if(params.length<2){
					System.out.println("illegal params");
				}
				String value = cacheService.get(params[1]) ;
				System.out.println("[Result]"+value);
			}else if("rem".equals(params[0])){
				if(params.length<2){
					System.out.println("illegal params");
				}
				cacheService.remove(params[1]) ;
			}
		}
	}
}
