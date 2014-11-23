package cn.kane.redisCluster.agent;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class NodeFactoryTest extends TestCase {

	ApplicationContext appContext = new ClassPathXmlApplicationContext("applicationContext-test.xml");

	public void testApp() throws InterruptedException{
		Thread.sleep(10000);
	}
}
