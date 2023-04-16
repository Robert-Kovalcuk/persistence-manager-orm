package sk.tuke.meta.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;
public class LazyLoadingProxy<T> implements InvocationHandler {

    private T delegate;
    private final Supplier<T> supplier;
    private boolean accessed;

    public LazyLoadingProxy(Supplier<T> supplier) {
        this.supplier = supplier;
        this.accessed = false;
    }


    public static <T> T createProxy(Class<T> clazz, Supplier<T> supplier) {
        return clazz.cast(Proxy.newProxyInstance(
                clazz.getClassLoader(),
                clazz.getInterfaces(),
                new LazyLoadingProxy<>(supplier)
        ));
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("invoked");
        if (!accessed) {
            this.delegate = supplier.get();
            accessed = true;
        }

        return method.invoke(delegate, args);
    }
}
