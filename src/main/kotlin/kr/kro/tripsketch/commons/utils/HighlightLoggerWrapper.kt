package kr.kro.tripsketch.commons.utils

import io.highlight.sdk.Highlight
import io.highlight.sdk.common.Severity
import org.slf4j.Logger

class HighlightLoggerWrapper(private val logger: Logger) : Logger by logger {

    override fun warn(msg: String) {
        logger.warn(msg)
        Highlight.captureLog(Severity.WARN, msg)
    }

    override fun warn(format: String, arg: Any) {
        logger.warn(format, arg)
        Highlight.captureLog(Severity.WARN, format.format(arg))
    }
}
