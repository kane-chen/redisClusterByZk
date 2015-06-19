/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package cn.kane.redisCluster.mapping.model;

/**
 * RESULT-ENUM
 * @author qingxiang.cqx 
 */
public enum CollectionMappingServiceResultEnum {
    /**
     * invalid-param
     */
    PARAM_ERROR(-2),
    /**
     * execute failed
     */
    FAILED(-1),
    /**
     * success
     */
    SUCCESS(0),
    /**
     * donot need to do it
     */
    EXISTED(1) ;
    
    private int retCode  ;
    
    CollectionMappingServiceResultEnum(int retCode){
        this.retCode = retCode ;
    }
    
    public int getRetCode(){
        return this.retCode ;
    }
}
