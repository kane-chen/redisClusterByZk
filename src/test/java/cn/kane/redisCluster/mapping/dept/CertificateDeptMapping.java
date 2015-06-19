/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package cn.kane.redisCluster.mapping.dept;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CertificateDeptMapping extends cn.kane.redisCluster.mapping.model.AbstractCollection4Mapping<Integer> {
    
    private Set<Integer> items = new HashSet<Integer>() ;
    
    public CertificateDeptMapping(Integer id,String name){
        this.setId(id);
        this.setName(name);
    }

    @Override
    public boolean contains(Integer obj) {
        return getItems().contains(obj) ;
    }

    @Override
    public void add(Integer item) {
        getItems().add(item) ;
    }

    @Override
    public void remove(Integer item) {
        getItems().remove(item) ;
    }

    @Override
    public List<Integer> list() {
        return Arrays.asList((Integer[])getItems().toArray()) ;
    }

    public Set<Integer> getItems() {
        return items;
    }

    public void setItems(Set<Integer> items) {
        this.items = items;
    }
    

}
