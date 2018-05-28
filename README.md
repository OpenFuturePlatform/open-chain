# Archetype of enterprise application

## 1. Introduction

### 1.1. Background

The archetype was born out of the need to reduce the waste of time to create a new project 
structure.

### 1.2. Principles

The core principle of archetype is searching and pageable in web api, simplicity of work 
with the database, consistency of enums with tables of databases and maximum test coverage.

## 2. API Specification

### 2.1. Marks

* `pageable`

> Target - URL
> 
> Indicate - URL marked by `pageble` supports request parameters described below
> 
> Request params:
>   1. offset - describes the offset for the set of results (only positive integers, default value - `0`)
>   2. limit - describes the limit of entities in the result set (only positive integers, default value - `100`)

* `sorting`

> Target - URL
> 
> Indicate - allow sorting by acceptable fields
> 
> Request params:
>   1. sortBy - describes the sorting field of entity (default value - `id`)
>   2. sortDirection - describes the sorting direction(`ASC` or `DESC`, default value - `ASC`) 

* `optional`

> Target - dto's fields
> 
> Indicate - nullable fields

### 2.2. User
#### SortBy

* `id` (default)
* `name`
* `age`

#### SearchBy

* `name`
* `age`

#### Get

`GET /api/users?name=Jone` or `GET /api/users?age=18`

```json
{
  "totalCount": 1,
  "list": [
    {
      "email": "jone@example.io",
      "name": "Jone",
      "age": 18,
      "authorities": [
        {
          "id": 1,
          "key": "ROLE_ADMIN",
          "authority": "ROLE_ADMIN"
        }
      ],
      "id": 1,
      "username": "jone@example.io",
      "credentialsNonExpired": true,
      "accountNonExpired": true,
      "accountNonLocked": true,
      "enabled": true
    }
  ]
}
```

`GET /api/users?limit=2`

```json
{
  "totalCount": 5,
  "list": [
    {
      "email": "jone@example.io",
      "name": "Jone",
      "age": 18,
      ...
    },
    {
      "email": "karl@example.io",
      "name": "Karl",
      "age": 23,
      ...
    }
  ]
}
```

## 3. Service and repository layers

### 3.1. Background
In order to work comfortably with the database was added __Querydsl__ library 
and __kapt__ plugin for it. Querydsl was born out of the need to maintain HQL queries in 
a typesafe way. Incremental construction of HQL queries requires String 
concatenation and results in hard to read code. Unsafe references to domain 
types and properties via plain Strings were another issue with String based 
HQL construction.

__Flyway__ was also added to work with the database to simplify migration in it.
           
### 3.2. Gradle integration

```groovy
plugins {
    id "org.jetbrains.kotlin.kapt" version '1.2.20'
}
dependencies {
    // QueryDSL
    compile("com.querydsl:querydsl-jpa:1.2.20")
    kapt("com.querydsl:querydsl-apt:4.1.4:jpa")
    // Flyway
    compile('org.flywaydb:flyway-core')
}
// IDEA
idea {
    module {
        def kaptMain = file('build/generated/source/kapt/main')
        sourceDirs += kaptMain
        generatedSourceDirs += kaptMain
    }
}
```

### 3.3.  Using Querydsl

```kotlin
@Transactional(readOnly = true)
override fun getUsers(searchRequest: UserDtoSearchRequest, pageRequest: PageRequest): Page<User> {
    val builder = BooleanBuilder()
    searchRequest.name?.let { builder.and(user.name.eq(it)) }
    searchRequest.age?.let { builder.and(user.age.eq(it)) }

    return userRepository.findAll(builder, pageRequest)
}
```

### 3.4. Using Flyway

The migrations are scripts in the form _V\<VERSION\>__\<NAME\>.sql_ 
(with <VERSION> an underscore-separated version, e.g. ‘1’ or ‘2_1’). 
By default they live in a folder _classpath:db/migration_ but you can modify 
that using flyway.locations.

```text
flyway.locations=db/migration
```

Spring Boot will call Flyway.migrate() to perform the database migration.
 If you would like more control, provide a @Bean that implements 
 FlywayMigrationStrategy:

 ```kotlin
@Bean
fun flywayMigrationStrategy(): FlywayMigrationStrategy {
    return FlywayMigrationStrategy { flyway ->
        flyway.clean()
        flyway.migrate()
    }
}
 ```
 
 ### 3.5. Logging
 
According the standard, we should use slf4j for logging. So, there is an extension that helps to get the logger from any place in the code:

```kotlin
val Any.log: Logger
    get() = LoggerFactory.getLogger(this.javaClass)
```

## 4. Testing
 
In order to conveniently test all application layers the application has configured 
configs of different types:

### 4.1. Repository layer

This configuration __RepositoryTests.kt__ must be inherited by any class that wants to perform 
integration testing.
```kotlin
@RunWith(SpringRunner::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class RepositoryTests {

    @Autowired
    protected lateinit var entityManager: TestEntityManager

}
```

This configuration __DictionaryTests.kt__ is needed to check whether the data in the enumerations with 
the database is consistent.

```kotlin
abstract class DictionaryTests(private val clazz: Class<out Enum<*>>, private val tableName: String) : RepositoryTests() {

    @Autowired
    private lateinit var em: EntityManager

    @Test
    fun dictionaryTest() {
        val dbValues = em.createNativeQuery("SELECT * FROM $tableName").resultList
        val enumValues = clazz.enumConstants

        assertThat(dbValues).hasSize(enumValues.size)
        for (value in enumValues) {
            val matched = dbValues.first { (it as Array<*>)[0] == (value as Dictionary).getId() }
            assertThat((matched as Array<*>)[1]).isEqualTo(value.name)
        }
    }

}
```

This configuration __DataSourceConfiguration.kt__ is needed if you need an embedded database.

```kotlin
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DataSourceConfiguration {

    @Bean
    fun dataSource(): DataSource {
        return EmbeddedPostgres.start().postgresDatabase
    }

}
```

### 4.2. Service layer

Inheritance configuration of __ServiceTests.kt__ is necessary for a unit of testing.

```kotlin
@RunWith(MockitoJUnitRunner::class)
abstract class ServiceTests
```

These functions are designed in __Mockito.kt__ config to exclude null types

```kotlin
fun <T> any(clazz: Class<T>): T = Mockito.any<T>(clazz)

fun anyLong(): Long = Mockito.anyLong()

fun anyString(): String = Mockito.anyString()

fun anyObject(): Any = Mockito.anyObject()

fun <T> eq(any: T): T = Mockito.eq<T>(any)
```

### 4.3. Controller layer

Setting up a __SecurityContextFactory__ to take full control over the 
security context.

```kotlin
class SecurityContextFactory : WithSecurityContextFactory<WithUser> {

    override fun createSecurityContext(customUser: WithUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()

        val principal = User(customUser.email, "name", 20)
        principal.addRoles(customUser.value.roles)

        context.authentication = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
        return context
    }

}
```

This class will depend on annotation, which we created:

```kotlin
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, CLASS, FILE)
@Retention(RUNTIME)
@Inherited
@WithSecurityContext(factory = SecurityContextFactory::class)
annotation class WithUser(
        val value: AccountType,
        val email: String = "local@host"
)

enum class AccountType(vararg val roles: Role.Dictionary) {
    ADMIN(Role.Dictionary.ADMIN),
    USER(Role.Dictionary.USER)
}
```

Then this annotation we can use in our tests for web api.

Inheritance configuration of __ControllerTests.kt__ is necessary for testing web api.

```kotlit
@RunWith(SpringRunner::class)
abstract class ControllerTests {

    @Autowired
    protected lateinit var mvc: MockMvc

    @MockBean
    protected lateinit var userService: UserService

}
```