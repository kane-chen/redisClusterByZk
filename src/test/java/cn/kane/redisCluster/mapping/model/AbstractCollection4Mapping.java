/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package cn.kane.redisCluster.mapping.model;

import java.util.List;

/**
 * Collection
 * @author qingxiang.cqx 
 */
public abstract class AbstractCollection4Mapping<I> {

    private Integer id ;
    
    private String name ;
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    
    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean contains(I obj) ;
    
    public abstract void add(I item) ;
    
    public abstract void remove(I item) ;
    
    public abstract List<I> list() ;
}
