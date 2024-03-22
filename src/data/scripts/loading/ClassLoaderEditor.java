package data.scripts.loading;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.io.IOException;

/**
 * Edits starsector's class loader during runtime to disable loading restrictions
 */
public class ClassLoaderEditor implements Opcodes {
    static String className = "com.fs.starfarer.loading.scripts.OoOO";
    public ClassLoaderEditor() throws IOException {
        ClassReader cr = new ClassReader(className);
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new ClassVisitor(ASM9, cw){
            @Override
            public MethodVisitor visitMethod(
                    int access,
                    String name,
                    String desc,
                    String signature,
                    String[] exceptions){
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if(name.contains("loadClass(String var1, boolean var2)")){
                    return new EditedClassLoader(access, desc, mv);
                }
                return mv;
            }
        }, 0);
    }
    public class EditedClassLoader extends LocalVariablesSorter implements Opcodes {
        EditedClassLoader(int access, String desc, MethodVisitor mv){
            super(ASM9, access, desc, mv);
        }
        @Override
        public void visitLineNumber(int line, Label start){
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
