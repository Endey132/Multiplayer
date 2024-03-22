package data.scripts.loading;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public class TempClassLoader extends URLClassLoader {
    static{
        ClassLoader.registerAsParallelCapable();
    }

    public TempClassLoader(URL[] urls, ClassLoader parent) { super(urls, parent); }

    public Class<?> defineMyClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain){
        return defineClass(name, b, off, len, protectionDomain);
    }
}
