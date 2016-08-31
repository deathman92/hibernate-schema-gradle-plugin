package com.github.deathman92.gradle.plugin

import com.github.deathman92.gradle.plugin.config.Configuration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * @author Victor Zhivotikov
 * @since 13.05.2016
 */
class HibernateSchemaGeneratePlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        def task = project.task("generateSchema", type: HibernateSchemaGenerateTask, group: "database")
        task.dependsOn(project.tasks.classes)

        project.extensions.create("generateSchema", Configuration)
        project.generateSchema {
            outputDir = new File(project.projectDir, "src/main/resources/db/schema")
            outputFileName = "schema.ddl"
        }
        project.generateSchema.extensions.targets = project.container(Configuration)
    }
}
