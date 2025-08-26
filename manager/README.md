System Design
For detailed system design documentation, [click here](docs/system_design.txt).



Run App
    ```
    Local environment: gradlew bootrun 
    Using Docker Compose: docker-compose up --build
    ```

Run Tests:
```
    To run regular tests:
    ./gradlew test

    To run only the acceptance tests:
    ./gradlew acceptanceTest
```
Environment Variables:
```declarative
APPUSER_PASSWORD=<DBPassword>
```
Configuring the Application:
```
-- Create the database: tasks and generate a user for the application as:
CREATE USER 'app_user'@'%' IDENTIFIED BY 'secure_password';

-- Grant permissions needed to run migrations on the entire database schema:
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
ON taskdb.* TO 'app_user'@'%';

-- Additionally, to have full control over the 'tasks' table, grant all privileges on it explicitly
GRANT ALL PRIVILEGES ON taskdb.tasks TO 'app_user'@'%';

-- Apply the privileges
FLUSH PRIVILEGES;
```

For this example you've to enable the liquibase migration to run it (since we will do it just once) in real life you will disable it and create a script within the build to run it