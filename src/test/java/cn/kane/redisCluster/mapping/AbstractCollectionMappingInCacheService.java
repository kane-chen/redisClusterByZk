/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package cn.kane.redisCluster.mapping;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cn.kane.redisCluster.mapping.model.AbstractCollection4Mapping;
import cn.kane.redisCluster.mapping.model.CollectionMappingServiceResultEnum;


/**
 * CollectionService
 * 
 * @author qingxiang.cqx 
 */
public abstract class AbstractCollectionMappingInCacheService<C extends AbstractCollection4Mapping<I>,I> implements CollectionMappingService<C,I>{

    @Autowired
    private CacheService cacheService ;

    public List<C> listAllCollections(Class<C> clazz) {
        String key = this.getCollectionListKey();
        String value = cacheService.get(key);
        return cacheService.getArrayBySerailString(value, clazz);
    }

    public abstract List<C> listAllCollections() ;
    
    public C queryCollectionByItem(I item, Class<C> clazz) {
        String itemKey = this.getItemKey(item);
        String groupKey = cacheService.get(itemKey) ;
        String value = cacheService.get(groupKey);
        C c = cacheService.getObjBySerailString(value, clazz);
        return c;
    }

    public abstract C queryCollectionByItem(I item) ;
    
    @SuppressWarnings("unchecked")
    public C queryCollection(C c) {
        String groupKey = this.getCollectionKey(c);
        String value = cacheService.get(groupKey);
        C obj = (C) cacheService.getObjBySerailString(value, c.getClass());
        return obj;
    }

    public CollectionMappingServiceResultEnum addOrUpdateCollection(C collection){
        C srcCol = this.queryCollection(collection) ;
        //generate collection by source-collection
        if(null!=srcCol){
            collection = this.newCollectionBySrc(srcCol, collection) ;
        }else{
            int startValue = 0 ;//start position
            int step = 1 ; //step,default 1 ;
            int expireTime = 0 ;//never expired
            String key = this.getCollectionSeqKey() ;
            int newId = cacheService.getNextSeqByKey(key, startValue, step, expireTime) ;
            collection.setId(newId);
        }
        //check collection
        boolean checkCollection = this.checkCollection(collection) ;
        if(!checkCollection){
            return CollectionMappingServiceResultEnum.PARAM_ERROR;
        }
        //put collection in tair
        boolean putColInTair = this.putCollectionInTairWithoutVersion(collection) ;
        if(putColInTair){
            return CollectionMappingServiceResultEnum.SUCCESS;
        }else{
            return CollectionMappingServiceResultEnum.FAILED;
        }
    }
    
    private boolean putCollectionInTairWithoutVersion(C collection){
        String key = this.getCollectionKey(collection);
        String value = cacheService.serial(collection);
        cacheService.put(key, value);
        return true;
    }
    
    public CollectionMappingServiceResultEnum bind(I item, C collection) {
        //tair-key check
        if (this.checkItemCollectionKey(item, collection)) {
            return CollectionMappingServiceResultEnum.PARAM_ERROR;
        }
        //collection exist
        if(null == this.queryCollection(collection)){
            return CollectionMappingServiceResultEnum.PARAM_ERROR;
        }
        //add item-mapping
        boolean addItemMapping = this.addItemMapping(item, collection);
        if (!addItemMapping) {
            return CollectionMappingServiceResultEnum.FAILED;
        }
        //add item in collection
        boolean addItemInCol = this.addItemInCollection(item, collection);
        if (!addItemInCol) {
            return CollectionMappingServiceResultEnum.FAILED;
        }
        //add col in List
        boolean addColInList = this.addCollectionInList(collection);
        if (!addColInList) {
            return CollectionMappingServiceResultEnum.FAILED;
        }
        return CollectionMappingServiceResultEnum.SUCCESS;
    }

    public CollectionMappingServiceResultEnum bindRetry(I item, C collection,int maxRetryTimes){
        if (this.checkItemCollectionKey(item, collection)) {
            return CollectionMappingServiceResultEnum.PARAM_ERROR;
        }
        CollectionMappingServiceResultEnum result = CollectionMappingServiceResultEnum.SUCCESS;
        int times = 0 ;
        boolean addItemMapping = false ;
        boolean addItemGroup = false ;
        boolean addGroupList = false ;
        while(times < maxRetryTimes && (!addItemMapping || !addItemGroup || !addGroupList)){
            times ++ ;
            if (!addItemMapping) {
                addItemMapping = this.addItemMapping(item, collection);
                if(!addItemMapping){
                    break ;
                }
            }
            if (!addItemGroup) {
                addItemGroup = this.addItemInCollection(item, collection);
                if (!addItemGroup) {
                    break ;
                }
            }
            if(!addGroupList){
                addGroupList = this.addCollectionInList(collection);
                if (!addGroupList) {
                    break  ;
                }
            }
        }
        if(addItemMapping && addItemGroup && addGroupList){
            result = CollectionMappingServiceResultEnum.SUCCESS;
        }else{
            result = CollectionMappingServiceResultEnum.FAILED;
        }
        return result;
    }
    
    private boolean addItemMapping(I item, C collection) {
        String key = this.getItemKey(item);
        String value = this.getCollectionKey(collection);
        cacheService.put(key, value);
        return true;
    }

    private boolean addItemInCollection(I item, C collection) {
        boolean result = true;
        C col = this.queryCollection(collection);
        if (null != col && !col.contains(item)) {
            col.add(item);
            String key = this.getCollectionKey(collection);
            String value = cacheService.serial(col);
            cacheService.put(key, value);
            result = true ;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean addCollectionInList(C collection) {
        boolean result = true;
        //collectionList
        String collectionsKey = this.getCollectionListKey();
        String certDeptsResult = cacheService.get(collectionsKey);
        List<C> cols = (List<C>) cacheService.getArrayBySerailString(certDeptsResult, collection.getClass());
        //add collection in List
        if (null != cols && !cols.contains(collection)) {
            //value
            cols.add(collection);
            String certDeptsStr = cacheService.serial(cols);
            //version check ...
            cacheService.put(collectionsKey, certDeptsStr);
        }
        return result;
    }

    private boolean checkItemCollectionKey(I item, C collection) {
        return null == this.getItemKey(item) || null == this.getCollectionKey(collection);
    }

    public CollectionMappingServiceResultEnum unbind(I item, C collection) {
        if (this.checkItemCollectionKey(item, collection)) {
            return CollectionMappingServiceResultEnum.PARAM_ERROR;
        }
        CollectionMappingServiceResultEnum result = CollectionMappingServiceResultEnum.SUCCESS;
        String itemKey = this.getItemKey(item);
        // del item
        cacheService.del(itemKey);
        // rm item from col
        boolean remItem = this.remItemInCollection(item, collection);
        if (!remItem) {
            return CollectionMappingServiceResultEnum.FAILED;
        }
        result = CollectionMappingServiceResultEnum.SUCCESS;
        return result;
    }

    private boolean remItemInCollection(I item, C collection) {
        boolean result = true;
        C col = this.queryCollection(collection);
        if (null != col && col.contains(item)) {
            col.remove(item);
            String key = this.getCollectionKey(collection);
            String value = cacheService.serial(col);
            cacheService.put(key, value);
            result = true;
        }
        return result;
    }

    @Deprecated
    public CollectionMappingServiceResultEnum remCollection(C collection){
        //check param
        String colKey = this.getCollectionKey(collection) ;
        if(null == colKey) {
            return CollectionMappingServiceResultEnum.PARAM_ERROR ;
        }
        //remove collection in tair
        cacheService.del(colKey) ;
        boolean remCol = true ;
        if(!remCol){
            return CollectionMappingServiceResultEnum.FAILED ;
        }
        //remove collection in list
        boolean remColInList = this.remCollectionInList(collection) ;
        if(remColInList){
            return CollectionMappingServiceResultEnum.SUCCESS ;
        }else{
            return CollectionMappingServiceResultEnum.FAILED ;
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean remCollectionInList(C collection){
        boolean updateSuccess = false ;
        //collectionList
        String collectionsKey = this.getCollectionListKey();
        String collsResult = cacheService.get(collectionsKey);
        List<C> cols = (List<C>) cacheService.getArrayBySerailString(collsResult, collection.getClass());
        //remove collection in list
        if (null != cols && cols.contains(collection)) {
            Iterator<C> itor = cols.iterator();
            while (itor.hasNext()) {
                if(itor.next().equals(collection)){
                    itor.remove();
                }
            }
            //value
            String colsValueStr = cacheService.serial(cols);
            //version
            cacheService.put(collectionsKey, colsValueStr);
        }
        return updateSuccess ;
    }
    

    /**
     * check collection's field value
     * @param collection
     * @return
     */
    public abstract boolean checkCollection(C collection) ;
    
    /**
     * copy some fields'(can not change) value from srcCol
     * @param srcCol
     * @param newCol
     * @return
     */
    public abstract C newCollectionBySrc(C srcCol,C newCol) ;
    
    /**
     * get itemKey in tair,such as : item.1
     * @param item
     * @return
     */
    public abstract String getItemKey(I item);

    /**
     * get collectionKey in tair,such as :collection.1
     * @param collection
     * @return
     */
    public abstract String getCollectionKey(C collection);

    /**
     * get CollectionList's Key in tair
     * @return
     */
    public abstract String getCollectionListKey();
    
    /**
     * get Sequence Key(Collection's Id) in tair
     * @return
     */
    public abstract String getCollectionSeqKey() ;

}
