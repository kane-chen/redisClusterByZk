/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package cn.kane.redisCluster.mapping;

import java.util.List;

/**
 * 类CacheService.java的实现描述：TODO 类实现描述 
 * @author qingxiang.cqx 2015年6月19日 下午4:15:26
 */
public interface CacheService {

    public String get(String key) ;
    
    public void put(String key ,String value) ;
    
    public void del(String key) ;
    
    public int getNextSeqByKey(String key,int startValue,int step,long expireTime) ;

    /* serial */
    
    public <T> T getObjBySerailString(String value,Class<T> clazz) ;
    
    public <T>  List<T> getArrayBySerailString(String value,Class<T> clazz) ;
    
    public String serial(Object obj) ;
}
