/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package cn.kane.redisCluster.mapping.dept;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.kane.redisCluster.mapping.AbstractCollectionMappingInCacheService;


public class CertificatedDeptMappingServiceImpl extends AbstractCollectionMappingInCacheService<CertificateDeptMapping, Integer>{

    /** KEY-PREF: subType&dept mapping in tair <key=subTypeId,value=deptId> */
    private static final String ITEM_KEY_PREFIX = "credit.cert.dept.subtype." ;
    
    /** KEY-PREF: dept in tair */
    private static final String COLLECTION_KEY_PREFIX = "credit.cert.dept." ;
    
    /** KEY: all certDept list */
    private static final String COLLECTIONS_KEY = "credit.cert.depts" ;
    
    /** KEY: dept-id seq in tair */
    private static final String COLLECTION_SEQUENCE_KEY = "credit.cert.dept.id.seq" ;

    @Override
    public List<CertificateDeptMapping> listAllCollections() {
        return this.listAllCollections(CertificateDeptMapping.class) ;
    }

    @Override
    public CertificateDeptMapping queryCollectionByItem(Integer item) {
        return this.queryCollectionByItem(item,CertificateDeptMapping.class);
    }

    @Override
    public boolean checkCollection(CertificateDeptMapping collection) {
        if(null == collection || null == collection.getId() || StringUtils.isBlank(collection.getName())){
            return false;
        }
        return true ;
    }

    @Override
    public CertificateDeptMapping newCollectionBySrc(CertificateDeptMapping srcCol, CertificateDeptMapping newCol) {
        newCol.setItems(srcCol.getItems());
        return newCol;
    }
    
    @Override
    public String getItemKey(Integer item) {
        return ITEM_KEY_PREFIX + item ;
    }

    @Override
    public String getCollectionKey(CertificateDeptMapping collection) {
        return COLLECTION_KEY_PREFIX + collection.getId() ;
    }

    @Override
    public String getCollectionListKey() {
        return COLLECTIONS_KEY;
    }

    @Override
    public String getCollectionSeqKey() {
        return COLLECTION_SEQUENCE_KEY;
    }

}
