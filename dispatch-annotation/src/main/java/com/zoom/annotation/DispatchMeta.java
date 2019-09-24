package com.zoom.annotation;

public class DispatchMeta {
    String methodName;
    Class<?> methodAssociatedClass;

    public DispatchMeta(Class<?> methodAssociatedClass, String methodName) {
        this.methodAssociatedClass = methodAssociatedClass;
        this.methodName = methodName;
    }

    public Class<?> getMethodAssociatedClass() {
        return methodAssociatedClass;
    }

    public String getMethodName() {
        return methodName;
    }
}
