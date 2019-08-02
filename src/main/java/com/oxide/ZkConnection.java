package com.oxide;



import com.oxide.annotation.Zkf;
import com.oxide.config.ZookeeperConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ZkConnection {

    private static String propertyPrefix = "";

    private static ZooKeeper client;

    private static Yaml yaml = new Yaml();

    private static Map<String, Object> propertyKey = new HashMap<>();

    private static Map<Class, Object> propertyCatch = new HashMap<>();

    private static Map<Object, Long> propertyVersion = new HashMap<>();

    public static <T> T getZkfProperty(Class<T> clazz) {
        Object property = propertyCatch.get(clazz);
        if (property == null) {
            try {
                property = clazz.newInstance();
                propertyCatch.put(clazz, property);
                propertyKey.put(clazz.getAnnotation(Zkf.class).WatchKey(), property);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (T) property;
    }

    public static void connect(ZookeeperConfig zookeeperConfig) throws IOException, KeeperException, InterruptedException {
        if (!StringUtils.isEmpty(zookeeperConfig.getPropertyPrefix())) {
            propertyPrefix = zookeeperConfig.getPropertyPrefix();
        }
        if (zookeeperConfig.isEnabled()) {
            client = new ZooKeeper(zookeeperConfig.getUrl(), zookeeperConfig.getTimeout(), new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    String path = watchedEvent.getPath();
                    Event.EventType type = watchedEvent.getType();
                    if (Event.EventType.NodeDataChanged.equals(type) && path.startsWith(propertyPrefix)) {
                        try {
                            inflateProperty(
                                    path.replaceAll(propertyPrefix, ""),
                                    new String(client.getData(path, true, null))
                            );
                        } catch (KeeperException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            initProperty();
        }
    }

    private static void initProperty() throws KeeperException, InterruptedException {
        for (Object obj : propertyCatch.values()) {
            String key = obj.getClass().getAnnotation(Zkf.class).WatchKey();
            String value = new String(client.getData(propertyPrefix + key, true, null));
            inflateProperty(key, value);
        }
    }

    private static void inflateProperty(String key, String value) {
        if (value == null) {
            return;
        }
        Object property = propertyKey.get(key);
        Object o = yaml.loadAs(value, property.getClass());
        BeanUtils.copyProperties(o, property);
    }
}