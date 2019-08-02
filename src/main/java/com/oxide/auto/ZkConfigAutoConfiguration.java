package com.oxide.auto;

import com.oxide.ZkConnection;
import com.oxide.annotation.Zkf;
import com.oxide.config.ZookeeperConfig;
import com.oxide.scanner.ClassPathZkfScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties
@Import({ZookeeperConfig.class, ZkConfigAutoConfiguration.AutoConfiguredZkScannerRegistrar.class})
public class ZkConfigAutoConfiguration {

    @Autowired
    ZookeeperConfig zookeeperConfig;

    @PostConstruct
    public void init() throws InterruptedException, IOException, KeeperException {
        ZkConnection.connect(zookeeperConfig);
    }

    public static class AutoConfiguredZkScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

            log.debug("Searching for zkf annotated with @Zkf");

            ClassPathZkfScanner scanner = new ClassPathZkfScanner(registry);

            try {
                if (this.resourceLoader != null) {
                    scanner.setResourceLoader(this.resourceLoader);
                }

                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                if (log.isDebugEnabled()) {
                    for (String pkg : packages) {
                        log.debug("Using auto-configuration base package '{}'", pkg);
                    }
                }

                scanner.setAnnotationClass(Zkf.class);
                scanner.doScan(StringUtils.toStringArray(packages));
            } catch (IllegalStateException ex) {
                log.debug("Could not determine auto-configuration package, automatic ectd scanning disabled.", ex);
            }

        }
    }
}

