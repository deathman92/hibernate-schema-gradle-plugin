# hibernate-schema-gradle-plugin
Gradle plugin for generate DDL scripts from JPA entities using Hibernate SchemaExport tool.

Supports only generation from annotated entities without persistence.xml or hibernate.cfg.xml.

Built and tested with Hibernate 5.2.2.Final.

# How-to Use
```groovy
buildscript {
  repositories {
    // not in any repo yet
  }
  dependencies {
    classpath("io.github.deathman.plugin:hibernate-schema-gradle-plugin:+")
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
  outputDir = file('src/main/resources/db/schema')
  outputFileName = 'schema.ddl'
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
No license
