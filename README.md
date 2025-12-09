# Aphrodite

<img src="./docs/concept.png" alt="Application Concept" title="Application Concept" width="500" height="500">

## [Terraform](./aphrodite-backend/README.md#terraform)

## [API Documentation](./aphrodite-backend/docs/swagger.json)

## Variables

[Spring Boot Controlled Variables:](https://docs.spring.io/spring-boot/reference/features/external-config.html)

- `server.port` - Port to run on, default `8083`
- `spring.profiles.active` - Spring profile, default `default`
    - Production profile configuration, `-Dspring.profiles.active=default,production`
- `spring.datasource.url` - Database URL, default `jdbc:sqlite:./db.sqlite`
- `spring.mvc.servlet.path` - Servlet path, default `/aphrodite-web-api`
- `aphrodite.unlock-keys` - Names of keys to unlock namespaces, default `[]`
    - Keys are comma separated, e.g. `./secrets/namespace1.json,./secrets/namespace2.json`
- `aphrodite.root-passwords` - Root passwords for namespace, default `{}`
    - `aphrodite.root-passwords.<NameSpace>=<Password>` - Root password for a namespace

SSL Configuration:

- `server.ssl.enabled` - Enable SSL, default `false`
- `server.ssl.certificate` - Path to pem certificate or content of certificate.
- `server.ssl.certificate-private-key` - Path to pem key or content of the key.

This can be configured using multiple types of certificate files, including PEM, DER, PKCS12, JKS, PKCS8, and OpenSSH.

- [Securing Spring Boot Applications With SSL](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl)
- [SSL :: Spring Boot](https://docs.spring.io/spring-boot/reference/features/ssl.html)

Environment Variables:

- `APHRODITE_LOG_LEVEL` - Log level, default `INFO`
    - Also applies to Spring Boot `logging.level.{web,sql,root}`
- `APHRODITE_LOG_DIR` - Log directory, default `./log/aphrodite`

## Deployment

Download the latest release from [Releases](https://github.com/arpanrec/aphrodite/releases)

```shell
curl https://github.com/arpanrec/aphrodite/releases/download/${APHRODITE_VERSION}/aphrodite-backend-boot-${APHRODITE_VERSION}.jar \
    -o aphrodite-backend-boot-${APHRODITE_VERSION}.jar
java -jar aphrodite-backend-boot-${APHRODITE_VERSION}.jar -Dspring.profiles.active=default,production
```

With Docker:

```shell
docker run -p 8083:8083 --rm --name aphrodite \
    -v "$(pwd)/app/data:/app/data:rw" \
    -e "APHRODITE_LOG_DIR=/app/data/log/aphrodite" \
    -e "APHRODITE_UNLOCK_KEYS=/app/data/secrets/namespace1.json,/app/data/secrets/namespace2.json" \
    -e "APHRODITE_ROOT_PASSWORDS_namespace1=password" \
    -e "SPRING_PROFILES_ACTIVE=default,production" \
    -e "APHRODITE_ROOTPASSWORDS_namespace2=password" \
    -e "SPRING_DATASOURCE_URL=jdbc:sqlite:/app/data/db.sqlite" \
     ghcr.io/arpanrec/aphrodite:${APHRODITE_VERSION:-latest}
```

## Dependency

Use with Gradle:

In `build.gradle.kts`

```kotlin
repositories {
    mavenCentral()
    if (System.getenv("GITHUB_TOKEN") != null) {
        maven {
            url = uri("https://maven.pkg.github.com/arpanrec/aphrodite")
            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "Bearer ${System.getenv("GITHUB_TOKEN")}"
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
}

dependencies {
    implementation("com.arpanrec:aphrodite-backend:${System.getenv("APHRODITE_VERSION")}")
}
```

Use with Maven:

In `~/.m2/settings.xml`

```xml

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <activeProfiles>
        <activeProfile>github</activeProfile>
    </activeProfiles>
    <profiles>
        <profile>
            <id>github</id>
            <repositories>
                <repository>
                    <id>central</id>
                    <url>https://repo.maven.apache.org/maven2</url>
                </repository>
                <repository>
                    <id>github</id>
                    <url>https://maven.pkg.github.com/arpanrec/aphrodite</url>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>
    <servers>
        <server>
            <id>github</id>
            <username>USERNAME</username>
            <password>TOKEN</password>
        </server>
    </servers>
</settings>
```

In `pom.xml`

```xml
<dependency>
    <groupId>com.arpanrec</groupId>
    <artifactId>aphrodite-backend</artifactId>
    <version>${APHRODITE_VERSION}</version>
</dependency>
```

## Development Git hooks

```bash
git config --local core.hooksPath .git-hooks
```
