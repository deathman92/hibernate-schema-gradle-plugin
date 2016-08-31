package com.github.deathman92.gradle.plugin


import com.github.deathman92.gradle.plugin.config.Configuration
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import spock.lang.Specification

/**
 * @author Victor Zhivotikov
 * @since 29.08.2016
 */
class HibernateSchemaGeneratePluginSpec extends Specification {

    @Rule TemporaryFolder testProjectDir
    String canonicalName
    Project project

    @Rule TestName testName = new TestName()

    String pluginName = 'hibernate-schema-generate'

    def setup() {
        canonicalName = testName.getMethodName().replaceAll(' ', '-')
        project = ProjectBuilder.builder().withName(canonicalName).withProjectDir(testProjectDir.root).build()
    }

    def 'apply does not throw exceptions'() {
        when:
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply is idempotent'() {
        when:
        project.apply plugin: pluginName
        project.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply adds task to project'() {
        when:
        project.apply plugin: pluginName

        then:
        project.tasks.generateSchema instanceof HibernateSchemaGenerateTask
        project.tasks.generateSchema.group == "database"

        project.generateSchema instanceof Configuration
        project.generateSchema.outputDir == new File(project.projectDir, "src/main/resources/db/schema")
        project.generateSchema.outputFileName == "schema.ddl"
        project.generateSchema.packageNames.isEmpty()
        project.generateSchema.properties.isEmpty()
    }

    def 'apply is fine on all levels of multiproject'() {
        def sub = createSubproject(project, 'sub')
        project.subprojects.add(sub)

        when:
        project.apply plugin: pluginName
        sub.apply plugin: pluginName

        then:
        noExceptionThrown()
    }

    def 'apply to multiple subprojects'() {
        def subprojectNames = ['sub1', 'sub2', 'sub3']

        subprojectNames.each { subprojectName ->
            def subproject = createSubproject(project, subprojectName)
            project.subprojects.add(subproject)
        }

        when:
        project.apply plugin: pluginName

        subprojectNames.each { subprojectName ->
            def subproject = project.subprojects.find { it.name == subprojectName }
            subproject.apply plugin: pluginName
        }

        then:
        noExceptionThrown()
    }

    Project createSubproject(Project parentProject, String name) {
        ProjectBuilder.builder().withName(name).withProjectDir(new File(testProjectDir.root, name)).withParent(parentProject).build()
    }
}