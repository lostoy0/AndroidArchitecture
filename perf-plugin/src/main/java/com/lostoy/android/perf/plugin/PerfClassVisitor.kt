package com.lostoy.android.perf.plugin

import com.ss.android.ugc.bytex.common.visitor.BaseClassVisitor

class PerfClassVisitor(var extension: PerfExtension) : BaseClassVisitor() {

    override fun visitSource(source: String?, debug: String?) {
        super.visitSource(source, debug)
    }
}
