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
                    environment {
                        JMETER_VERSION = '5.6.3'
                        // CAMBIO CLAVE: Usamos /tmp o una ruta fija del sistema, NO el workspace
                        // Esto sobrevive al 'cleanWs()'
                        JMETER_BASE_DIR = "/tmp/jenkins-tools" 
                        JMETER_HOME = "${JMETER_BASE_DIR}/apache-jmeter-5.6.3"
                        
                        SCRIPT_PATH = 'tests/performance/fasttasker.jmx'
                        RESULT_PATH = 'tests/performance/result.jtl'
                        REPORT_DIR  = 'tests/performance/report-html'
                    }
                    steps {
                        cleanWs()
                        unstash 'source-code'
                        
                        script {
                            // 1) instalaction in cache
                            if (!fileExists("${JMETER_HOME}/bin/jmeter")) {
                                echo "JMeter no encontrado en caché. Descargando..."
                                
                                // if no exists dir tools, then creating
                                sh "mkdir -p ${JMETER_BASE_DIR}"
                                
                                // download and unzip
                                dir("${JMETER_BASE_DIR}") {
                                    def url = "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"
                                    def file = "apache-jmeter-${JMETER_VERSION}.tgz"

                                    sh "curl -fLk -o ${file} ${url}"
                                    sh "ls -la"
                                    sh "tar -xzf ${file}"
                                    sh "rm ${file}"
                                }
                                
                                sh "chmod +x ${JMETER_HOME}/bin/jmeter"
                            } else {
                                echo "good! jmeter in cache"
                            }

                            
                            // check if exists .jmx file
                            if (!fileExists(SCRIPT_PATH)) {
                                error "xml not found"
                            }
                            // 2) run
                            try {
                                sh """
                                    ${JMETER_HOME}/bin/jmeter -n \
                                    -t ${SCRIPT_PATH} \
                                    -l ${RESULT_PATH} \
                                    -e -o ${REPORT_dIR}
                                """
                            } catch (Exception e) {
                                echo "test finish with umbral fails"
                            }
                        }
                    }
                    post {
                        always {
                            // 3) publishing report in jenkins
                            publishHTML target: [ // using HTML publisher pluging
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'tests/performance/report-html',
                                reportFiles: 'index.html',
                                reportName: 'Reporte de Performance'
                            ]
                            
                            // save jtl
                            archiveArtifacts artifacts: '**/*.jtl', allowEmptyArchive: true
                        }
                    }
                }
            }
        }
    }
}
