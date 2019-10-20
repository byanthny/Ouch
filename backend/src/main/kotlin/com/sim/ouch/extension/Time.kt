package com.sim.ouch.extension

import com.soywiz.klock.DateTime
import com.soywiz.klock.ISO8601

/** The current UTC time as [ISO8601.DATETIME_COMPLETE]. */
val DateTime.iso get() = DateTime.now().format(ISO8601.DATETIME_COMPLETE)

/** This String as a DateTime from [ISO8601.DATETIME_COMPLETE]. */
val String.asDateTime get() = DateTime.parse(this).utc
