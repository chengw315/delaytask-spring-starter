package tech.chengw.www.annocation;

import tech.chengw.www.task.MethodTaskExecutorProxy;
import tech.chengw.www.task.TaskExecutor;
import tech.chengw.www.task.TaskExecutorRegistry;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * description 注册所有的TaskExecutor，包括：
 * 1. TaskExecutor类型的bean
 * 2. 所有使用了@TaskExecutor注解的方法（仅限bean的方法）
 * 注：一个TaskExecutor类型的bean，只会注册其本身，而不会注册其内部使用了@TaskExecutor注解的方法
 * 一个非TaskExecutor类型的bean，其多个方法都使用了@TaskExecutor注解，每个方法都会被注册
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/2
 **/
public class DelayTaskExecutorAnnotationBeanPostProcessor implements
        ApplicationContextAware,BeanPostProcessor, SmartInitializingSingleton, BeanFactoryAware, BeanClassLoaderAware, Ordered {

    private TaskExecutorRegistry taskExecutorRegistry;
    private BeanFactory beanFactory;
    private ClassLoader beanClassLoader;
    private final ConcurrentHashMap<Class<?>, String> classSet = new ConcurrentHashMap();

    private final ConcurrentLinkedQueue<String> executorBeanNames = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<MethodEndpoint> executorMethods = new ConcurrentLinkedQueue<>();
    private ApplicationContext applicationContext;

    /**
     * 注册所有隐式注册的TaskExecutor
     */
    @Override
    public void afterSingletonsInstantiated() {
        //注入注册器
        if (this.taskExecutorRegistry == null) {
            this.taskExecutorRegistry = beanFactory.getBean(TaskExecutorRegistry.class);
        }
        //首先注册TaskExecutor类型的对象
        executorBeanNames.forEach(beanName -> taskExecutorRegistry.registry((TaskExecutor) beanFactory.getBean(beanName)));
        //对于@TaskExecutor注解的方法，生成代理对象，并进行注册
        for (MethodEndpoint methodEndpoint : executorMethods) {
            methodEndpoint.setBean(beanFactory.getBean(methodEndpoint.getBeanName()));
            taskExecutorRegistry.registry(new MethodTaskExecutorProxy(methodEndpoint));
        }
    }

    /**
     * 收集所有隐式注册的TaskExecutor
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {

        //优先注册TaskExecutor类型的对象（TaskExecutor类型的对象会无视其@TaskExecutor注解）
        if (TaskExecutor.class.isInstance(bean)) {
            executorBeanNames.offer(beanName);
        }

        //同一个class只处理一次
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (beanName == null || beanName.equals(classSet.putIfAbsent(targetClass, beanName))) {
            return bean;
        }
        //收集携带@TaskExecutor的方法
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            tech.chengw.www.annocation.TaskExecutor annotation = declaredMethods[i].getDeclaredAnnotation(tech.chengw.www.annocation.TaskExecutor.class);
            if (annotation != null) {
                executorMethods.offer(new MethodEndpoint()
                        .setAnnotation(annotation)
                        .setMethod(declaredMethods[i])
                        .setBeanName(beanName));
            }
        }
        return bean;
    }

    /**
     * @param o
     * @param s
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public class MethodEndpoint {
        private Method method;
        private tech.chengw.www.annocation.TaskExecutor annotation;
        private String beanName;
        private Object bean;

        public Method getMethod() {
            return method;
        }

        public MethodEndpoint setMethod(Method method) {
            this.method = method;
            return this;
        }

        public tech.chengw.www.annocation.TaskExecutor getAnnotation() {
            return annotation;
        }

        public MethodEndpoint setAnnotation(tech.chengw.www.annocation.TaskExecutor annotation) {
            this.annotation = annotation;
            return this;
        }

        public String getBeanName() {
            return beanName;
        }

        public MethodEndpoint setBeanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Object getBean() {
            return bean;
        }

        public MethodEndpoint setBean(Object bean) {
            this.bean = bean;
            return this;
        }
    }

}
