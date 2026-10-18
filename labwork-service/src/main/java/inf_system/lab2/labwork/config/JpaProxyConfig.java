package inf_system.lab2.labwork.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class JpaProxyConfig {

    @Bean
    static BeanPostProcessor entityManagerFactoryInterfaceCustomizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof LocalContainerEntityManagerFactoryBean factory) {
                    factory.setEntityManagerFactoryInterface(EntityManagerFactory.class);
                }
                return bean;
            }
        };
    }
}
