package data.scripts.loading;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.loading.scripts.OoOO;
import org.apache.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;

/**
 * Edits Starsector's class loader in memory to disable restrictions
 */
public class ClassLoaderEditor implements Opcodes {
    private static final Logger LOGGER = Global.getLogger(ClassLoaderEditor.class);
    private static final String className = "com.fs.starfarer.loading.scripts.OoOO";
    private static final ClassLoader cl = OoOO.getSystemClassLoader();
    public ClassLoaderEditor() throws Throwable {
        LOGGER.info("Running constructor");
        ClassReader cr = new ClassReader(className);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassLoaderTransform cv = new ClassLoaderTransform(cw);
        cr.accept(cv, 0);
        byte[] data = cw.toByteArray();

        MethodHandle m = MethodHandles.lookup().findVirtual(cl.getClass(), "defineMyClass", MethodType.methodType(
                Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class));
        try{
            m.invoke(cl, className, data, 0, data.length, ClassLoaderEditor.class.getProtectionDomain());
        }  catch (RuntimeException | Error e){
            throw e;
        } catch (Throwable t) {
            throw new AssertionError("unreachable", t);
        }
        ClassReader debugReader = new ClassReader(className);
        File f = new File("grug.txt");
        try{
            f.createNewFile();
        } catch(Exception e){

        }
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(f);
        } catch (Exception e){

        }
        TraceClassVisitor tcv = new TraceClassVisitor(pw);
        debugReader.accept(tcv, 0);
        LOGGER.info("Completed constructor");
    }
    private class ClassLoaderTransform extends ClassVisitor implements Opcodes{
        ClassLoaderTransform(ClassVisitor cv){
            super(ASM9, cv);
            LOGGER.info("Constructed ClassLoaderTransform");
        }
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions){
            LOGGER.debug("ClassLoaderEditor Visiting method name: ["+name+"], desc: ["+desc+"], signature: ["+signature+"]");
            if(name.contains("loadClass")){
                return new LoadClassMethodVisitor(cv.visitMethod(access, name, desc, signature, exceptions));
            }
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }
    }
    private class LoadClassMethodVisitor extends MethodVisitor implements Opcodes {
        LoadClassMethodVisitor(MethodVisitor mv){
            super(ASM9, mv);
            LOGGER.info("Constructed LoadClassMethodVisitor");
        }
        @Override
        public void visitLineNumber(int line, Label start){
            LOGGER.debug("LoadClassMethodVisitor Visiting line number: ["+line+"]");
            if(!(line >= 23 && line < 33)){
                super.visitLineNumber(line, start);
            }
        }
    }

    /**
     * "loadClass(String var1, boolean var2)"
     *
     *             Global.getLogger(ClassLoaderEditor.class).debug("AMNOGUS ClassLoader handler invoked!!!!");
     *
     *             if(StarfarerSettings.öÒ0000()){
     *                 Logger.getLogger(OoOO.class).info("Loading class: " + args[0]);
     *             }
     *
     *             return ClassLoader.class.getMethod("loadClass", new Class[]{String.class, boolean.class}).invoke(((OoOO)self).getParent(), args);
     */
}
