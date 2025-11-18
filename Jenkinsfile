pipeline {
    agent any

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Spotless Check') {
            steps {
                echo 'Running Spotless format check...'
                dir('loan-mate') {
                    sh './gradlew spotlessCheck --no-daemon'
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Building project (tests skipped)...'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'js-packages',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )
                ]) {
                    dir('loan-mate') {
                        sh """
                        ./gradlew build -x test --no-daemon \
                        -PGITHUB_USERNAME=${GITHUB_USERNAME} \
                        -PGITHUB_TOKEN=${GITHUB_TOKEN}
                        """
                    }
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
