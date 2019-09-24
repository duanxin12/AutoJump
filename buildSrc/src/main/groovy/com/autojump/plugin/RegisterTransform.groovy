package com.autojump.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * 1、扫描文件夹：查询通过javapoet技术生成文件夹，并统计实现了相关接口的类
 * 2、扫描jar文件：a、查询通过javapoet技术生成的文件夹，并统计实现了相关接口的类 b、查询并获取需要插入代码的类文件所在的jar文件
 * 3、将相关接口类通过ASM技术插入到需要插入逻辑的相关类文件的相关方法中
 */
class RegisterTransform extends Transform {

    Project project
    static ArrayList<ScanSetting> registerList
    boolean leftSlash = File.separator == '/'

    /**
     * 需要插入代码的文件
     */
    static File fileContainsInitClass

    RegisterTransform(Project project) {
        this.project = project
    }

    /**
     * 插件名
     * @return
     */
    @Override
    String getName() {
        return ScanSetting.Plugin_Name
    }

    /**
     * 内容类型
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 扫描范围
     * @return
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 是否增量构建
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println("transform")
        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->

                String fileName = jarInput.name
                String hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)

                if (fileName.startsWith(".jar")) {
                    fileName = fileName.substring(0, fileName.length() - 4)
                }

                File src = jarInput.file
                //不知道原因
                File dest = transformInvocation.outputProvider.getContentLocation(fileName + "_" + hexName,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR)

                /**
                 *
                 */
                if (ScanUtil.shouldProcessPreDexJar(jarInput.file.absolutePath)) {
                    ScanUtil.scanJar(src, dest)
                }

                FileUtils.copyFile(src, dest)
            }

            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                     directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                //文件夹路径
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator

                directoryInput.file.eachFileRecurse {File file ->

                    //文件名(包路径)
                    def path = file.absolutePath.replace(root, '')
                    if (!leftSlash) {
                        path = path.replaceAll("\\\\", "/")
                    }
                    if(file.isFile() && ScanUtil.shouldProcessClass(path)){
                        println("directoryFile1 = " + path)
                        ScanUtil.scanClass(file)
                    } /*else if (path == ScanSetting.General_To_Class_File_Name) {
                        println("directoryFile2 = " + path)
                        RegisterTransform.fileContainsInitClass = file
                    }*/
                }

                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }

        if (fileContainsInitClass) {
            registerList.each { ext ->
                println("fileContainsInitClass")
                if (ext.classList.isEmpty()) {
                    //该interface未被使用
                } else {
                    RegisterCodeGenerator.insertRegisterCodeTo(ext)
                }
            }
        }

    }
}
