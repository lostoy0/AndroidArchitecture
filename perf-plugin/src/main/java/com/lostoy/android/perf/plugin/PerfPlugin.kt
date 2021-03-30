package com.lostoy.android.perf.plugin

import com.android.build.gradle.AppExtension
import com.ss.android.ugc.bytex.common.CommonPlugin
import com.ss.android.ugc.bytex.common.TransformConfiguration
import com.ss.android.ugc.bytex.common.visitor.ClassVisitorChain
import org.gradle.api.Project

class PerfPlugin : CommonPlugin<PerfExtension, PerfContext>() {

    override fun getContext(project: Project?, android: AppExtension?, extension: PerfExtension?): PerfContext {
        return PerfContext(project, android, extension)
    }

    override fun transform(relativePath: String, chain: ClassVisitorChain): Boolean {
        //我们需要修改字节码，所以需要注册一个ClassVisitor
        //We need to modify the bytecode, so we need to register a ClassVisitor
        chain.connect(PerfClassVisitor(extension))
        return super.transform(relativePath, chain)
    }

    override fun transformConfiguration(): TransformConfiguration {
        return object : TransformConfiguration {
            override fun isIncremental(): Boolean {
                //插件默认是增量的，如果插件不支持增量，需要返回false
                //The plugin is incremental by default.It should return false if incremental is not supported by the plugin
                return true
            }
        }
    }
}
