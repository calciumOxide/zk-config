package com.oxide.factory;

import com.oxide.ZkConnection;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.FactoryBean;

import static org.springframework.util.Assert.notNull;

@Data
@ToString
public class ZkFactoryBean<T> implements FactoryBean<T> {

    private Class<T> zkfProperty;

    private ZkFactoryBean() {

    }

    private ZkFactoryBean(Class<T> zkfProperty) {
        this.zkfProperty = zkfProperty;
    }

    @Override
    public T getObject() {
        notNull(this.zkfProperty, "Property 'zkfProperty' is required");
        return ZkConnection.getZkfProperty(zkfProperty);
    }

    @Override
    public Class<?> getObjectType() {
        return zkfProperty;
    }
}
