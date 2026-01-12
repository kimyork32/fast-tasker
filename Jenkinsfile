pipeline {
    agent none 

    stages {
        // with any agent, clean enviroment and download repo github backend and save source-code
        stage('Setup Code') {
            agent any 
            steps {
                cleanWs()
                checkout scm
                stash name: 'source-code', includes: '**' 
            }
        }
        
        // flow DEVELOP
        stage('CI Flow (Develop)') {
            // when exists PR for develop
            when {
                anyOf {
                    branch 'develop'
                    changeRequest target: 'develop'
                }
            }
            // then perform the following steps
            stages {
                // in parallel, perform the following stages:
                stage('Parallel Analysis') {
                    parallel {
                        // integration-unit tests: monolithic
                        stage('Monolith Build') {
                            agent any 
                            tools { maven 'maven-3' } 
                            steps {
                                cleanWs()
                                unstash 'source-code' 
                                dir('monolith-app') {
                                    sh 'rm -rf .scannerwork target'
                                    withSonarQubeEnv('sonar-server') {
                                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=fast-tasker-monolith -Dsonar.ws.timeout=300'
                                    }
                                }
                            }
                        }

                        // integration-unit tests: notification service
                        stage('Notification Build') {
                            agent any 
                            tools { maven 'maven-3' }
                            steps {
                                cleanWs()
                                unstash 'source-code'
                                dir('fast-tasker-notification') {
                                    sh 'rm -rf .scannerwork target'
                                    withSonarQubeEnv('sonar-server') {
                                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=fast-tasker-notification -Dsonar.ws.timeout=300'
                                    }
                                }
                            }
                        }
                    }
                }
                
                // listen quality gate from sonarqube
                stage('Quality Gate') {
                    agent any 
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                }
            }
        }

        // flow STATING
        stage('Staging Checks') {
            // when exists PR for STATING 
            when {
                changeRequest target: 'staging'
            }
            // in parallel:
            parallel {
                // security test
                stage('Security Scan (SAST/DAST)') {
                    agent any
                    steps {
                        cleanWs()
                        unstash 'source-code'
                        echo "--- RUN SECURITY TEST ---"
                        sh 'echo "Running Trivy or OWASP..."'
                    }
                }

                // performance test
                stage('Performance Tests') {
                    agent any
                    steps {
                        cleanWs()
                        unstash 'source-code'
                        echo "--- RUN PERFORMANCE TEST ---"
                        sh 'echo "Running k6 / JMeter tests..."'
                    }
                }
            }
        }
    }
}
