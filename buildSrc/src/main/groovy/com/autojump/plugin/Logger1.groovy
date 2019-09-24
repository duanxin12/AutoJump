package com.autojump.plugin

import org.gradle.api.Project
import org.gradle.api.logging.Logger

class Logger1 {
    static Logger logger

    static void make(Project project) {
        logger = project.getLogger()
    }

    static void i(String info) {
        if (null != info && null != logger) {
            logger.info(info)
        }
    }

    static void e(String error) {
        if (null != error && null != logger) {
            logger.error(error)
        }
    }

    static void w(String warning) {
        if (null != warning && null != logger) {
            logger.warn(warning)
        }
    }
}
