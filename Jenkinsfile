pipeline {
    agent none 

    parameters {
        booleanParam(name: 'RUN_SECURITY', defaultValue: true, description: 'Ejecutar escaneo de seguridad (ZAP/Trivy)')
        booleanParam(name: 'RUN_PERFORMANCE', defaultValue: true, description: 'Ejecutar pruebas de carga (JMeter)')
    }

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

        // branch validator 
        stage('branch validator of the merge') {
            // only run this if it's a PR
            when { 
                changeRequest() 
            }
            steps {
                script {
                    echo "analyzing PR from '${env.CHANGE_BRANCH}' to '${env.CHANGE_TARGET}'"

                    // rule: any branch except main or staging can be included in 'develop'
                    if (env.CHANGE_TARGET == 'develop') {
                        if (env.CHANGE_BRANCH == 'main') {
                            error "BLOCk!!: 'main' cannot enter 'develop'"

                        }
                        if (env.CHANGE_BRANCH == 'staging') {
                            error "BLOCk!!: 'staging' cannot enter 'develop'"

                        }
                    }

                    // rule: to 'staging' only enters 'develop'
                    if (env.CHANGE_TARGET == 'staging') {
                        // if (env.CHANGE_BRANCH != 'develop') {
                        if (env.CHANGE_BRANCH != 'task51-perfomance') { // changing this
                            error "BLOCk!!: to 'staging' enters 'develop'"

                        }
                    }
                }
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
                                // build and install 'common' project
                                dir('common') {
                                    sh 'mvn clean install -DskipTests'
                                }
                                dir('monolith-app') {
                                    sh 'rm -rf .scannerwork target'
                                    withSonarQubeEnv('sonar-server') {
                                        withCredentials([file(credentialsId: 'fast-tasker-env', variable: 'ENV_FILE')]) {
                                            sh '''
                                                cp $ENV_FILE .env 
                                                sed -i 's/\r$//' .env
                                                set -a 
                                                . ./.env
                                                set +a
                                                mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                                    -Dsonar.projectKey=fast-tasker-monolith \
                                                    -Dsonar.projectName="Fast Tasker Monolith" \
                                                    -Dsonar.ws.timeout=300
                                            '''
                                        }
                                    }
                                    // wait for qualitygate for monolith
                                    timeout(time: 10, unit: 'MINUTES') {
                                        waitForQualityGate abortPipeline: true
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
                                // build and install 'common' project
                                dir('common') {
                                    sh 'mvn clean install -DskipTests'
                                }
                                dir('notification-service') {
                                    sh 'rm -rf .scannerwork target'
                                    withSonarQubeEnv('sonar-server') {
                                        withCredentials([file(credentialsId: 'fast-tasker-env', variable: 'ENV_FILE')]) {
                                            sh '''
                                                cp $ENV_FILE .env
                                                sed -i 's/\r$//' .env
                                                set -a 
                                                . ./.env
                                                set +a
                                                mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                                    -Dsonar.projectKey=fast-tasker-notification \
                                                    -Dsonar.projectName="Notification Service" \
                                                    -Dsonar.ws.timeout=300
                                            '''
                                        }

                                    }
                                    // wait for qualitygate for notification service
                                    timeout(time: 10, unit: 'MINUTES') {
                                        waitForQualityGate abortPipeline: true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // flow STATING
        stage('Staging Checks') {
            // when exists PR for STATING 
            when {
                anyOf {
                    branch 'staging'
                    changeRequest target: 'staging'
                }
            }
            // in parallel:
            parallel {
                // security test
                stage('Security Scan (SAST/DAST)') {
                    when {
                        expression { return params.RUN_SECURITY }
                    }
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
                    when {
                        expression { return params.RUN_PERFORMANCE }
                    }
                    agent any
                    environment {
                        SCRIPT_PATH = 'tests/performance/fasttasker2.jmx'
                        RESULT_PATH = 'tests/performance/result.jtl'
                        REPORT_DIR  = 'tests/performance/report-html'
                        
                        JMETER_VERSION = '5.6.3'
                    }
                    steps {
                        cleanWs()
                        unstash 'source-code'
                        
                        script {
                            // install jmeter
                            def jmeterDir = "/tmp/jmeter-${JMETER_VERSION}"
                            def jmeterBin = "${jmeterDir}/bin/jmeter"
                            
                            if (!fileExists(jmeterBin)) {
                                echo "Instalando JMeter en ${jmeterDir}..."
                                sh "mkdir -p ${jmeterDir}"
                                
                                // download jmeter
                                sh "curl -Lks https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz | tar -xz -C ${jmeterDir} --strip-components=1"
                                
                            } else {
                                echo "using jmeter in cache"
                            }
                            
                            // ejecute tests
                            
                            try {
                                sh """
                                    ${jmeterBin} -n \
                                    -t ${SCRIPT_PATH} \
                                    -Jhost=host.docker.internal \
                                    -l ${RESULT_PATH} \
                                    -e -o ${REPORT_DIR}
                                """
                            } catch (Exception e) {
                                echo "finish"
                            }
                        }
                    }
                    post {
                        always {
                            publishHTML target: [
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'tests/performance/report-html',
                                reportFiles: 'index.html',
                                reportName: 'Reporte Performance'
                            ]
                        }
                    }
                }
            }
        }
    }
}
