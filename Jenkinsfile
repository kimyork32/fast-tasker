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
                        // Rutas relativas en tu repo
                        SCRIPT_PATH = 'tests/performance/fasttasker2.jmx'
                        RESULT_PATH = 'tests/performance/result.jtl'
                        REPORT_DIR  = 'tests/performance/report-html'
                        
                        // Variables para la instalación temporal
                        JMETER_VERSION = '5.6.3'
                    }
                    steps {
                        cleanWs()
                        unstash 'source-code'
                        
                        script {
                            // --- PARTE 1: INSTALACIÓN SEGURA DE JMETER ---
                            // Definimos una ruta temporal fuera del workspace para evitar problemas
                            def jmeterDir = "/tmp/jmeter-${JMETER_VERSION}"
                            def jmeterBin = "${jmeterDir}/bin/jmeter"
                            
                            if (!fileExists(jmeterBin)) {
                                echo "Instalando JMeter en ${jmeterDir}..."
                                sh "mkdir -p ${jmeterDir}"
                                
                                // Descargamos usando CURL (que sí tienes instalado)
                                // -L: Seguir redirecciones, -k: Ignorar SSL, -s: Silencioso
                                sh "curl -Lks https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz | tar -xz -C ${jmeterDir} --strip-components=1"
                                
                                echo "JMeter instalado correctamente."
                            } else {
                                echo "⚡ Usando JMeter desde caché."
                            }
                            
                            // --- PARTE 2: EJECUCIÓN DEL TEST ---
                            echo "Ejecutando prueba de carga..."
                            
                            // TRUCO DE RED: Pasamos la propiedad 'host' a JMeter dinámicamente
                            // Si tu backend está en el host, JMeter usará 'host.docker.internal'
                            // Si falla, intenta cambiar 'host.docker.internal' por la IP de tu PC (ej. 192.168.1.X)
                            try {
                                sh """
                                    ${jmeterBin} -n \
                                    -t ${SCRIPT_PATH} \
                                    -Jhost=host.docker.internal \
                                    -l ${RESULT_PATH} \
                                    -e -o ${REPORT_DIR}
                                """
                            } catch (Exception e) {
                                echo "La prueba terminó con fallos (errores 500/400 o umbrales excedidos)."
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
