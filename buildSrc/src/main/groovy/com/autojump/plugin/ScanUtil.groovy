package com.autojump.plugin

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import sun.rmi.runtime.Log

import java.util.jar.JarEntry
import java.util.jar.JarFile

class ScanUtil {

    /**
     * 判断是否为Android的jar包
     * @param path
     * @return
     */
    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(ScanSetting.Auto_Class_Package_Name);
    }

    /**
     * 获取jar文件中的相关接口类、确认需要插入代码的类是否存在及确认存在于哪个jar包
     * @param srcFile
     * @param destFile
     */
    static void scanJar(File srcFile, File destFile) {
       if (srcFile) {
           def jarFile = new JarFile(srcFile)
           Enumeration enumeration = jarFile.entries()
           while(enumeration.hasMoreElements()) {
               JarEntry jarEntry = (JarEntry)enumeration.nextElement()
               String entryName = jarEntry.name
               if (entryName.startsWith(ScanSetting.Auto_Class_Package_Name)) {
                   InputStream inputStream = jarFile.getInputStream(jarEntry)
                   scanClass(inputStream)
                   inputStream.close()
               } else if (entryName == ScanSetting.General_To_Class_File_Name) {
                   println("scanJar2 = " + entryName)
                   RegisterTransform.fileContainsInitClass = destFile
               }

           }
       }
    }

    static void scanClass(File file) {
        scanClass(new FileInputStream(file))
    }

    static void scanClass(InputStream fileInputStream) {

        ClassReader classReader = new ClassReader(fileInputStream)
        ClassWriter classWriter = new ClassWriter(classReader, 0)
        ScanClassVisitor classVisitor = new ScanClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        fileInputStream.close()
    }

    static class ScanClassVisitor extends ClassVisitor {

        ScanClassVisitor(int api, ClassWriter classWriter) {
            super(api, classWriter)
        }
        /**
         * 查询该类实现的接口是否包含在registerList中的接口名称中 一致将类名添加到classList
         * 类版本 ,修饰符 , 类名 , 泛型信息 , 继承的父类 , 实现的接口
         * @param version
         * @param access
         * @param name
         * @param signature
         * @param superName
         * @param interfaces
         */
        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)

            RegisterTransform.registerList.each { ext ->
                if (ext.interfaceName && interfaces != null) {
                    interfaces.each { interName ->
                        if (ext.interfaceName == interName) {
                            println("visit11 " + name)
                            ext.classList.add(name)
                        }
                    }
                }

            }

        }
    }
}
