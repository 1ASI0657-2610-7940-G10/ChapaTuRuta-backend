pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'RUN_SONAR',
            defaultValue: false,
            description: 'Ejecutar analisis SonarQube y Quality Gate'
        )
    }

    environment {
        TEST_SELECTOR = '*Test,!CucumberTestRunner'
        SONARQUBE_ENV = 'SonarQube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    services().each { service ->
                        dir(service) {
                            runMaven('clean test -Dtest="' + env.TEST_SELECTOR + '"')
                        }
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/site/jacoco/jacoco.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { params.RUN_SONAR }
            }
            steps {
                script {
                    withSonarQubeEnv(env.SONARQUBE_ENV) {
                        services().each { service ->
                            dir(service) {
                                runMaven(
                                    'sonar:sonar ' +
                                    "-Dsonar.projectKey=chapaturuta-${service} " +
                                    "-Dsonar.projectName=\"ChapaTuRuta ${service}\" " +
                                    '-Dsonar.sources=src/main/java ' +
                                    '-Dsonar.tests=src/test/java ' +
                                    '-Dsonar.junit.reportPaths=target/surefire-reports ' +
                                    '-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
                                )
                            }
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

def services() {
    return ['identity-service', 'routing-service', 'tracking-service', 'api-gateway']
}

def runMaven(String goals) {
    if (isUnix()) {
        sh "chmod +x ./mvnw && ./mvnw -B ${goals}"
    } else {
        bat "mvnw.cmd -B ${goals}"
    }
}
