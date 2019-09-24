package com.autojump.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginLaunch implements Plugin<Project> {

    @Override
    void apply(Project project) {
        /**
         * 判断是否为apk模块
         */
        def isApp = project.plugins.hasPlugin(AppPlugin)

        if (isApp) {
            Logger1.make(project)
            println("PluginLaunch")
            //插件类型：AppExtension、LibraryExtension
            def android = project.extensions.findByType(AppExtension)
            def transferImpl = new RegisterTransform(project)

            List<ScanSetting> list = new ArrayList<>(1)
            list.add(new ScanSetting("IAutoDispatch"))
            RegisterTransform.registerList = list
            //在AppExtension注册一个Transfrom流程
            android.registerTransform(transferImpl)
        }
    }
}