package tech.chengw.www.annocation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * description
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/22
 **/
public class DelayTaskBeanPostProcessorRegistry implements ImportBeanDefinitionRegistrar {

    final String ANNOTATION_BEAN_POST_PROCESSOR_BEAN_NAME = "delayTaskExecutorPostProcessor";
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(ANNOTATION_BEAN_POST_PROCESSOR_BEAN_NAME)) {
            registry.registerBeanDefinition(ANNOTATION_BEAN_POST_PROCESSOR_BEAN_NAME,
                    new RootBeanDefinition(DelayTaskExecutorAnnotationBeanPostProcessor.class));
        }
    }
}
