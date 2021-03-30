package com.lostoy.android.perf.plugin

import com.android.build.gradle.AppExtension
import com.ss.android.ugc.bytex.common.BaseContext
import org.gradle.api.Project

class PerfContext(project: Project?, android: AppExtension?, extension: PerfExtension?) : BaseContext<PerfExtension>(project, android, extension)
