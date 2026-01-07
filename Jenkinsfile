pipeline {
    agent any
    tools { maven 'maven-3' }

    stages {
        stage('clean and download code') {
            steps {
                cleanWs()
                checkout scm
            }
        }
        
        stage('compile and analize monolith') {
            steps {
                dir('monolith-app') {
                    sh 'rm -rf .scannerwork target' // remove trash before
                    withSonarQubeEnv('sonar-server') {
                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=fast-tasker-monolith -Dsonar.ws.timeout=300'
                    }
                }
            }
        }

        stage('compile and analize notification service') {
            steps {
                dir('fast-tasker-notification') {
                    sh 'rm -rf .scannerwork target' // remove trash before
                    withSonarQubeEnv('sonar-server') {
                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=fast-tasker-notification -Dsonar.ws.timeout=300'
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
