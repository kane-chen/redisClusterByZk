package cn.kane.redisCluster.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class NodeFactoryTest extends TestCase {

	ApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext-test.xml");

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
		}
	}
}
