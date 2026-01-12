:: FOR WINDOWS DEVS
@echo off
echo starting SonarQube testing and scanning...
mvn clean verify sonar:sonar ^
  -Dsonar.projectKey=fast-tasker-monolith ^
  -Dsonar.projectName='fast-tasker-monolith' ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.token=${SONAR_TOKEN_MONOLITH}
echo.
echo scan complete. Check http://localhost:9000
pause