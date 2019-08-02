package com.oxide.scanner;

import com.oxide.factory.ZkFactoryBean;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.Set;

@Getter
@Setter
public class ClassPathZkfScanner extends ClassPathBeanDefinitionScanner {

    private Class<? extends Annotation> annotationClass;


    public ClassPathZkfScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        beanDefinitionHolders.forEach(e -> {
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition) (e.getBeanDefinition());
            Class<?> clazz = null;
            try {
                clazz = beanDefinition.resolveBeanClass(this.getClass().getClassLoader());
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
            beanDefinition.setBeanClass(ZkFactoryBean.class);

            beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        });
        return beanDefinitionHolders;
    }

}

