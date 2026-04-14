# Database Migrations (Flyway)

This project uses Flyway for version-controlled database migrations.

## How it works
- Migrations are SQL files in `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql` (e.g., `V1__initial_schema.sql`)
- Migrations are applied in order on application startup
- The `flyway_schema_history` table tracks applied migrations

## Creating a new migration
1. Create a new file: `V{next_version}__{description}.sql`
2. Write standard SQL (MySQL dialect)
3. Test locally by running the application
4. Commit the migration file with your code changes

## Configuration
See `application.properties.example` for Flyway settings:
- `spring.flyway.enabled=true` - Enable/disable Flyway
- `spring.flyway.locations=classpath:db/migration` - Migration file location
- `spring.flyway.baseline-on-migrate=true` - Baseline existing databases
- `spring.flyway.clean-disabled=true` - Prevent accidental data loss
