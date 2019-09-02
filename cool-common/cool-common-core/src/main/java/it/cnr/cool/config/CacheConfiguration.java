/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.GroupProperty;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Optional;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final Logger LOGGER = LoggerFactory.getLogger(CacheConfiguration.class);


    private @Value("${hazelcast.port}") int hazelcastPort;

    private @Value("${hazelcast.instance.name}") String hazelcastInstanceName;

    private @Value("${hazelcast.mancenter}") String mancenter;

    private @Value("${hazelcast.members}") String members;

    private @Value("${hazelcast.multicast.port}") Integer hazelcastMulticastPort;

    private @Value("${hazelcast.ttl:86400}") int ttl;

    private @Value("${hazelcast.port.autoincrement:false}") boolean portAutoincrement;

    @PreDestroy
    public void destroy() {
        LOGGER.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }


    @Bean
    public CacheManager cacheManager() {

        LOGGER.info("Starting HazelcastCacheManager");


        final Config config = new Config();


        if (mancenter != null) {
          LOGGER.info("using mancenter: " + mancenter);
          ManagementCenterConfig mc = new ManagementCenterConfig();
          mc.setEnabled(true);
          mc.setUrl(mancenter);
          config.setManagementCenterConfig(mc);
        } else {
          LOGGER.info("no mancenter configured");
        }

        Optional.ofNullable(hazelcastInstanceName)
                .ifPresent(s -> {
                    config.setInstanceName(s);
                });
        config.getNetworkConfig().setPort(hazelcastPort);
        config.getNetworkConfig().setPortAutoIncrement(portAutoincrement);

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);

        if (Optional.ofNullable(members).filter(s -> s.length() > 0).isPresent()) {
            LOGGER.info("TCP members: " + members);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
            config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(members);
        } else if (hazelcastMulticastPort != null) {
            LOGGER.info("multicast on port " + hazelcastMulticastPort);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
            config.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort(hazelcastMulticastPort);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        } else {
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        }


        config.setProperty(GroupProperty.ENABLE_JMX, Boolean.TRUE.toString());
        config.setProperty(GroupProperty.ENABLE_JMX_DETAILED, Boolean.TRUE.toString());

        config.getMapConfigs().put("default", initializeDefaultMapConfig());

        HazelcastInstance hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config);

        return new HazelcastCacheManager(hazelcastInstance);
    }


    @Bean
    public HazelcastInstance hazelcastInstance(CacheManager cacheManager) {
        HazelcastCacheManager hazelcastCacheManager = (HazelcastCacheManager) cacheManager;
        return hazelcastCacheManager.getHazelcastInstance();
    }

    @Bean
    public Cluster cluster(HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getCluster();
    }


    private MapConfig initializeDefaultMapConfig() {
        MapConfig mapConfig = new MapConfig();

        /*
            Number of backups. If 1 is set as the backup-count for example,
            then all entries of the map will be copied to another JVM for
            fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
         */
        mapConfig.setBackupCount(1);

        /*
            Valid values are:
            NONE (no eviction),
            LRU (Least Recently Used),
            LFU (Least Frequently Used).
            NONE is the default.
         */
        mapConfig.setEvictionPolicy(EvictionPolicy.LRU);

        /*
            Maximum size of the map. When max size is reached,
            map is evicted based on the policy defined.
            Any integer between 0 and Integer.MAX_VALUE. 0 means
            Integer.MAX_VALUE. Default is 0.
         */
        mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE));

        /*
            When max. size is reached, specified percentage of
            the map will be evicted. Any integer between 0 and 100.
            If 25 is set for example, 25% of the entries will
            get evicted.
         */
        mapConfig.setEvictionPercentage(25);

        LOGGER.info("time to live: " + ttl);
        mapConfig.setTimeToLiveSeconds(ttl);

        return mapConfig;
    }

}
