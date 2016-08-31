package com.github.deathman92.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.model.naming.ImplicitNamingStrategy
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType
import org.reflections.Reflections

import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * @author Victor Zhivotikov
 * @since 13.05.2016
 */
class HibernateSchemaGenerateTask extends DefaultTask {

    @TaskAction
    void generate() {
        def config = project.generateSchema
        if (config.outputDir != null) {
            config.outputDir.mkdirs()
        }
        def regBuilder = new StandardServiceRegistryBuilder()
        if (config.dialect != null) {
            regBuilder.applySetting(AvailableSettings.DIALECT, config.dialect)
        }
        regBuilder.applySettings(config.properties)
        def metadataSources = new MetadataSources(regBuilder.build())
        def classLoader = getProjectClassLoader()
        config.packageNames.each {
            logger.info("  * package: " + it)
            def reflections = new Reflections(it, classLoader)
            def allClasses = [] as Set
            allClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class))
            allClasses.addAll(reflections.getTypesAnnotatedWith(MappedSuperclass.class))
            allClasses.each {
                logger.info("  * classes: " + it.name)
                metadataSources.addAnnotatedClass(it)
            }
        }
        def tmpCld = Thread.currentThread().contextClassLoader
        def backUpCreated = false;
        try {
            def schemaOutput = findOutputFile(config.outputDir, config.outputFileName)
            backUpCreated = backUpSchema(schemaOutput)
            if (backUpCreated) {
                schemaOutput.delete()
            }
            Thread.currentThread().contextClassLoader = classLoader
            ImplicitNamingStrategy implicitNamingStrategy = new ImplicitNamingStrategyJpaCompliantImpl()
            PhysicalNamingStrategy physicalNamingStrategy = new PhysicalNamingStrategyStandardImpl()
            if (config.implicitStrategy != null) {
                try {
                    implicitNamingStrategy = (ImplicitNamingStrategy) classLoader.loadClass(config.implicitStrategy).newInstance()
                    logger.info('Selected implicit naming strategy with classname ' + implicitNamingStrategy.class.name)
                } catch (Exception e) {
                    logger.info('Cannot instantiate implicit naming strategy with classname ' + config.implicitStrategy + '. Fallback to default', e)
                    implicitNamingStrategy = new ImplicitNamingStrategyJpaCompliantImpl()
                }
            } else {
                logger.info('Used default implicit naming strategy with classname ' + implicitNamingStrategy.class.name)
            }
            if (config.physicalStrategy != null) {
                try {
                    physicalNamingStrategy = (PhysicalNamingStrategy) classLoader.loadClass(config.physicalStrategy).newInstance()
                    logger.info('Selected physical naming strategy with classname ' + physicalNamingStrategy.class.name)
                } catch (Exception e) {
                    logger.info('Cannot instantiate physical naming strategy with classname ' + config.physicalStrategy + '. Fallback to default', e)
                    physicalNamingStrategy = new PhysicalNamingStrategyStandardImpl()
                }
            } else {
                logger.info('Used default physical naming strategy with classname ' + physicalNamingStrategy.class.name)
            }
            def metadata = metadataSources.getMetadataBuilder()
                    .applyTempClassLoader(classLoader)
                    .applyImplicitNamingStrategy(implicitNamingStrategy)
                    .applyPhysicalNamingStrategy(physicalNamingStrategy)
                    .build()
            def export = new SchemaExport()
            export.setDelimiter(";")
            export.setFormat(true)
            export.setOutputFile(new File(config.outputDir, config.outputFileName).path)

            export.create(EnumSet.of(TargetType.SCRIPT), metadata)

            deleteBackUp(config.outputDir, config.outputFileName)
        } catch (Exception e) {
            e.printStackTrace()
            if (backUpCreated) {
                restoreBackUp(config.outputDir, config.outputFileName)
            }
        } finally {
            Thread.currentThread().contextClassLoader = tmpCld
        }
    }

    static boolean backUpSchema(File schema) throws Exception {
        if (schema != null && schema.exists()) {
            def backUp = new File(schema.path + "_backUp");
            Files.copy(schema.toPath(), backUp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        return false;
    }

    static void restoreBackUp(File outputDir, String outputFileName) {
        def schema = findOutputFile(outputDir, outputFileName);
        def backUp = new File(schema.path + "_backUp");
        backUp.renameTo(schema);
    }

    static void deleteBackUp(File outputDir, String outputFileName) {
        def schema = findOutputFile(outputDir, outputFileName);
        def backUp = new File(schema.path + "_backUp");
        backUp.delete();
    }

    static File findOutputFile(File outputDir, String outputFileName) {
        def directory = new File(outputDir.path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, outputFileName);
    }


    ClassLoader getProjectClassLoader() {
        def classfiles = [] as Set

        classfiles += [
                project.sourceSets.main.output.classesDir,
                project.sourceSets.main.output.resourcesDir
        ]

        def classURLs = []
        classfiles.each {
            classURLs << it.toURI().toURL()
        }

        project.configurations.compile.each {
            classURLs << it.toURI().toURL()
        }

        project.configurations.runtime.each {
            classURLs << it.toURI().toURL()
        }

        classURLs.each {
            logger.debug("  * classpath: " + it)
        }

        return new URLClassLoader(classURLs.toArray(new URL[0]), this.class.classLoader)
    }
}
