# X - Dropbox Business OAuth Demo (Placeholders)

This is a sample Spring Boot project demonstrating:
- OAuth2 Authorization Code flow with Dropbox (placeholders used)
- Calling Dropbox Business APIs:
  - /team/get_info (team/organization details)
  - /team/members/list (list users)
  - /team_log/get_events (fetch sign-in events)

## Setup
1. Replace the placeholders in `src/main/resources/application.properties`:
   - dropbox.client.id
   - dropbox.client.secret
   - dropbox.redirect.uri

2. Build & run:
   ```bash
   mvn clean package
   java -jar target/X-0.0.1-SNAPSHOT.jar
   ```

3. Open browser:
   - To start OAuth authorization: http://localhost:8080/auth/start
   - Dropbox will redirect to callback (configured redirect URI), which exchanges code and calls sample endpoints.

## Notes
- This project uses placeholders for credentials. Do not commit real secrets.
- The code is intentionally simple for assessment/demo purposes.
