package com.autojump.plugin

import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RegisterCodeGenerator {
    ScanSetting scanSetting;
    RegisterCodeGenerator(ScanSetting scanSetting) {
        this.scanSetting = scanSetting;
    }

    static insertRegisterCodeTo(ScanSetting scanSetting1 ) {

        if (scanSetting1 != null && !scanSetting1.classList.isEmpty()) {
            RegisterCodeGenerator registerCodeGenerator = new RegisterCodeGenerator(scanSetting1)
            File file = RegisterTransform.fileContainsInitClass
            /*if (file.getName().endsWith(".class")) {
                registerCodeGenerator.insertRegisterCodeToClassFile(file)
            } else*/ if (file.getName().endsWith(".jar")) {
                registerCodeGenerator.insertRegisterCodeToJarFile(file)
            }
        }
    }

    private File insertRegisterCodeToClassFile(File classFile) {
        if (classFile) {
            def optFile = new File(classFile.name + ".opt")
            if (optFile.exists())
                optFile.delete()
            FileInputStream fileInputStream = new FileInputStream(classFile)
            def bytes = insertMethodCode(fileInputStream)
            FileOutputStream fileOutputStream = new FileOutputStream(optFile)
            fileOutputStream.write(bytes)
            fileInputStream.close()
            fileOutputStream.close()

            if (classFile.exists()) {
                classFile.delete()
            }

            optFile.renameTo(classFile)
        }
        return classFile
    }

    private File insertRegisterCodeToJarFile(File jarFile) {
        if (jarFile) {
            //临时文件
            def optFile = new File(jarFile.parent, jarFile.name + ".opt")
            if (optFile.exists())
                optFile.delete()

            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optFile))

            while(enumeration.hasMoreElements()) {

                JarEntry jarEntry = (JarEntry)enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (entryName == ScanSetting.General_To_Class_File_Name) {
                    def bytes = insertMethodCode(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }

            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }

            optFile.renameTo(jarFile)
        }
        return jarFile
    }

    byte[] insertMethodCode(InputStream inputStream) {
        ClassReader classReader = new ClassReader(inputStream)
        ClassWriter classWriter = new ClassWriter(classReader, 0)
        MyClassVisitor myClassVisitor = new MyClassVisitor(Opcodes.ASM5, classWriter)
        classReader.accept(myClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    class MyClassVisitor extends ClassVisitor {

        MyClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            println("visitMethod == " + name)
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == ScanSetting.General_To_Method_Name) {
                methodVisitor = new MyMethodVisitor(Opcodes.ASM5, methodVisitor)
            }
            return methodVisitor
        }
    }


    class MyMethodVisitor extends MethodVisitor {

        MyMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitInsn(int opcode) {
            println("visitInsn == ")
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                scanSetting.classList.each { ext ->
                    String name = ext.replaceAll("/", ".")
                    println("visitInsn == " + name)
                    mv.visitLdcInsn(name)
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            ScanSetting.General_To_Class_Name,
                            ScanSetting.Auto_Class_Method_Name,
                            "(Ljava/lang/String;)V",
                            false)
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // 手动挡需要计算栈空间，这里两个long型变量的操作需要4个slot
            mv.visitMaxs(maxStack + 4, maxLocals);
        }
    }
}