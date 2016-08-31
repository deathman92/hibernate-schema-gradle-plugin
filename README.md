[![Build Status](https://travis-ci.org/deathman92/hibernate-schema-gradle-plugin.svg?branch=master)](https://travis-ci.org/deathman92/hibernate-schema-gradle-plugin)
[ ![Download](https://api.bintray.com/packages/deathman92/maven-release/hibernate-schema-gradle-plugin/images/download.svg) ](https://bintray.com/deathman92/maven-release/hibernate-schema-gradle-plugin/_latestVersion)

# hibernate-schema-gradle-plugin
Gradle plugin for generate DDL scripts from JPA entities using Hibernate SchemaExport tool.

Supports only generation from annotated entities without persistence.xml or hibernate.cfg.xml.

Built and tested with Hibernate 5.2.2.Final.

# How-to Use
```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath("io.github.deathman.plugin:hibernate-schema-gradle-plugin:1.0.0")
  }
}

apply plugin: 'java'
apply plagin: 'hibernate-schema-generate' // or 'io.github.deathman.plugin.hibernate-schema-generate'

generateSchema {
  // properties (see Setting later)
}
```
To generate schema, run
```
gradle generateSchema
```
or
```
./gradlew generateSchema
```
# Settings
```groovy
generateSchema {
  outputDir = file('src/main/resources/db/schema') // folder where output file will be written
  outputFileName = 'schema.ddl' // name of output file
  packageNames = ['com.example.domain'] // required
  dialect = 'org.hibernate.dialect.PostgreSQLDialect' // required
  implicitStrategy = 'org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl'
  physicalStrategy = 'org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl'
  properties = [
    'key' : 'value'
  ]
}
```
# License
[MIT](/LICENSE.md)
