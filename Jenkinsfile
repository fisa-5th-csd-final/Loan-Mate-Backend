pipeline {
    agent any

    parameters {
        string(
            name: 'GIT_URL',
            defaultValue: 'https://github.com/fisa-5th-csd-final/Loan-Mate-Backend.git',
            description: 'Git 저장소 URL'
        )
        string(
            name: 'GIT_CREDENTIAL',
            defaultValue: 'js',
            description: 'Git Credentials ID'
        )
    }

    environment {
        // Gradle 캐시 디렉토리 (속도 향상)
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Checking out source code...'
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${env.BRANCH_NAME}"]],
                    userRemoteConfigs: [[
                        url: params.GIT_URL,
                        credentialsId: params.GIT_CREDENTIAL  // 🔗 Jenkins Credentials ID
                    ]]
                ])
            }
        }

        stage('Spotless Check') {
            steps {
                echo '✨ Running Spotless format check...'
                dir('loan-mate') {
                    sh './gradlew spotlessCheck --no-daemon'
                }
            }
        }

        stage('Build') {
            steps {
                echo '🏗️ Building project (tests skipped)...'
                dir('loan-mate') {
                    sh './gradlew build --no-daemon'
                }
            }
        }

        stage('SonarQube Analysis') {
                    environment {
                        SONAR_SCANNER_HOME = tool 'SonarScanner'
                    }
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            dir('loan-mate') {
                                sh "${SONAR_SCANNER_HOME}/bin/sonar-scanner"
                            }
                        }
                    }
                }
    }

    post {
        success {
            echo 'Spotless & Build succeeded! Merge allowed.'
        }
        failure {
            echo 'Spotless or Build failed. Merge not allowed!'
        }
    }
}