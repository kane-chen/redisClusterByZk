package cn.kane.redisCluster.mapping;

import java.util.List;

import cn.kane.redisCluster.mapping.model.AbstractCollection4Mapping;
import cn.kane.redisCluster.mapping.model.CollectionMappingServiceResultEnum;


public interface CollectionMappingService<C extends AbstractCollection4Mapping<I>,I> {

    /* list-all-collection */
    List<C> listAllCollections() ;

    /* collection add/update/query */
    C queryCollectionByItem(I item) ;
    
    C queryCollection(C c) ;

    CollectionMappingServiceResultEnum addOrUpdateCollection(C collection) ;
    
    /* collection&item mapping */
    CollectionMappingServiceResultEnum bind(I item, C collection) ;

    CollectionMappingServiceResultEnum bindRetry(I item, C collection,int maxRetryTimes);
    
    CollectionMappingServiceResultEnum unbind(I item, C collection) ;

    @Deprecated
    CollectionMappingServiceResultEnum remCollection(C collection) ;
    
}
