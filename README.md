# pathtopasskeys-migration-vaadin-25
**Migration to Vaadin 25.0.6 and Spring-Boot 4.0.2**

There are a number of breaking items in the migration. I got most of them solved and the application runs, but with some residual problems.

1. Spring Security - updated to VaadinSecurityConfigurer.
2. Upgrade to Jackson 3 (from com.fasterxml.jackson to tools.jackson). This includes replacing ObjectMapper with JsonMapper, including setting the Base64Variants.MODIFIED_FOR_URL to handle base64URL strings.
3. There are changes to the json format of Spring Webauthn (i.e. springframework.security.web.webauthn). Specifically the date items have been switched from Long values to Time Stamps and the json array structure of base64 values and Transports variable has changed. This required changes to how CredentialRecord is built.
4. Required upgrading SqlDataSourceScriptDatabaseInitializer to DataSourceScriptDatabaseInitializer for datasource initialisation. However, I don't think I've got the code right as the database initialisation keeps repeating, leading to this error "org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "sample_book_pkey". I have to keep clearing the container in Docker to make this work. Can log in with OTT and see all the views, but attempt to log in with passkey fails. I think this could also be related to the database issues.
5. Libraries updated in pom file.

**Entityexplorer**

Visualisation of PostgreSQL TestContainer data is broken by these changes. Unable to visualise the data in this demo via http://localhost:8080/entityexplorer/. It conflicts with the older Vaadin and Spring versions in the EntityExplorer (https://github.com/viritin/entityexplorer).

1. Entityexplorer needs to be migrated to Spring Boot 4 and Vaadin 25 compiler.
2. Spring 4 now seems to require security annotation @AnonymousAllowed for parent layout (TopLayout in this case).

**Docker**

This version was tested against Docker engine version 29.