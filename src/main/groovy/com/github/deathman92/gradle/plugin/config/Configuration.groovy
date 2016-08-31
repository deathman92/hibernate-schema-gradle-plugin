package com.github.deathman92.gradle.plugin.config

/**
 * @author Victor Zhivotikov
 * @since 13.05.2016
 */
class Configuration {

    File outputDir
    String outputFileName
    List packageNames = []
    Map properties = [:]
    String dialect
    String implicitStrategy
    String physicalStrategy
}
