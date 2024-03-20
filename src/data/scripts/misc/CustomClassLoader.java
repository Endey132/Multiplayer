package data.scripts.misc;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

public class CustomClassLoader extends URLClassLoader {
    static{
        ClassLoader.registerAsParallelCapable();
    }

    public CustomClassLoader(URL[] urls, ClassLoader parent) { super(urls, parent); }

    public Class<?> defineMyClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain){
        return defineClass(name, b, off, len, protectionDomain);
    }
}
