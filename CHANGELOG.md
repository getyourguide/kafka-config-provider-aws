# Changelog

## v0.2

This release is maintained by [GetYourGuide](https://github.com/getyourguide) as a fork of [jcustenborder/kafka-config-provider-aws](https://github.com/jcustenborder/kafka-config-provider-aws).

### Changes

- **AWS SDK v1 → v2**: Migrated from `com.amazonaws:aws-java-sdk-*` to `software.amazon.awssdk` (version 2.29.52).
- **Kafka Connect API upgrade**: Bumped from 2.8.0 to 4.2.0.
- **Dropped parent BOM and connect-utils**: Removed the `jcustenborder` parent POM and `connect-utils` dependency. Plugin configuration (compiler, surefire, checkstyle, license) is now defined directly in `pom.xml`.
- **Log4j2**: Replaced logback with Log4j2 for test logging.
- **Native ConfigDef**: Replaced `connect-utils` `ConfigKeyBuilder` with native Kafka `ConfigDef.define()`.
