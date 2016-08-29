package io.github.deathman.gradle.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author Victor Zhivotikov
 * @since 29.08.2016
 */
class HibernateSchemaGenerateTaskSpec extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder(new File(new File(".").getAbsoluteFile().getParent()))
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def 'should generate'() {
        given:
        buildFile << """
plugins {
    id 'io.github.deathman.plugin.hibernate-schema-generate'
}

repositories {
    jcenter()
}

dependencies {
    compile("org.hibernate:hibernate-entitymanager:5.2.2.Final")
}

sourceSets {
    main {
        java {
            srcDir file("../src/test/resources/unit/src")
        }
        output.resourcesDir output.classesDir
    }
}

generateSchema {
    packageNames = ['io.github.deathman.model']
    dialect = 'org.hibernate.dialect.PostgreSQLDialect'
}
"""
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('generateSchema')
                .withPluginClasspath()
                .build()
        then:
        result.task(":generateSchema").outcome == TaskOutcome.SUCCESS
        def expect = file("../src/test/resources/expected-result.txt").text
        fileExists("src/main/resources/db/schema/schema.ddl")
        def actual = file("src/main/resources/db/schema/schema.ddl").text
        actual == expect
    }

    protected File file(String path) {
        def baseDir = testProjectDir.root
        def splitted = path.split('/')
        def directory = splitted.size() > 1 ? directory(splitted[0..-2].join('/'), baseDir) : baseDir
        def file = new File(directory, splitted[-1])
        file.createNewFile()
        file
    }

    protected File directory(String path, File baseDir = testProjectDir.root) {
        new File(baseDir, path).with {
            mkdirs()
            it
        }
    }

    protected boolean fileExists(String path) {
        new File(testProjectDir.root, path).exists()
    }
}