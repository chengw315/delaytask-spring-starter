package tech.chengw.www.task;

import tech.chengw.www.annocation.DelayTaskExecutorAnnotationBeanPostProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * description 代理使用了@TaskExecutor注解的方法
 *
 * @author chengwj
 * @version 1.0
 * @date 2021/2/5
 **/
public class MethodTaskExecutorProxy extends TaskExecutor {

    private Method method;
    private Object bean;

    public MethodTaskExecutorProxy(DelayTaskExecutorAnnotationBeanPostProcessor.MethodEndpoint methodEndpoint) {
        this.setAutoAck(methodEndpoint.getAnnotation().autoAck());
        this.method = methodEndpoint.getMethod();
        this.bean = methodEndpoint.getBean();
    }


    @Override
    public void doExecute(Long tag, TaskBody taskBody) {
        try {
            method.invoke(bean,generateParams(method, new Object[] {tag,taskBody,getAcker()}));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 填充参数
     * @param method
     * @param objects
     * @return
     */
    private Object[] generateParams(Method method, Object[] objects) {
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();
            //基础类型的参数，转换成包装类型
            if (paramType.isPrimitive()) {
                paramType = convertBoxType(paramType);
            }

            for (int j = 0; j < objects.length; j++) {
                //参数类型相同即可进行填充
                if (paramType.isInstance(objects[j])) {
                    params[i] = objects[j];
                }
            }
        }
        return params;
    }

    /**
     *基础类型转包装类型
     */
    private Class<?> convertBoxType(Class<?> paramType) {
        if (int.class.equals(paramType)) {
            return Integer.class;
        } else if (long.class.equals(paramType)) {
            return Long.class;
        } else if (double.class.equals(paramType)) {
            return Double.class;
        } else if (float.class.equals(paramType)) {
            return Float.class;
        } else if (byte.class.equals(paramType)) {
            return Byte.class;
        } else if (short.class.equals(paramType)) {
            return Short.class;
        } else if (boolean.class.equals(paramType)) {
            return Boolean.class;
        } else if (char.class.equals(paramType)) {
            return Character.class;
        }
        return Object.class;
    }

}
