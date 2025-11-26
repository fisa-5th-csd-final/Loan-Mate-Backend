pipeline {
    agent any

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        DEPLOY_HOST = "${DEPLOY_HOST}"
        SSH_USER="${SSH_USER}"
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
                        credentialsId: 'js-package',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )
                ]) {
                    dir('loan-mate') {
                        sh """
                        ./gradlew build -x test --no-daemon \
                        -PGITHUB_USERNAME=$GITHUB_USERNAME \
                        -PGITHUB_TOKEN=$GITHUB_TOKEN
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

        stage('Deploy to EC2') {
               when {
                   branch 'develop'
               }
               steps {
                   echo 'Deploying to EC2...'
                   sshagent(credentials: ['from-jenkins-to-aws-ec2-access-key']) {
                       sh """
                           scp -o StrictHostKeyChecking=yes deploy.sh ${env.SSH_USER}@${env.DEPLOY_HOST}:/home/${env.SSH_USER}/deploy.sh

                           ssh -o StrictHostKeyChecking=yes ${env.SSH_USER}@${env.DEPLOY_HOST} "
                               chmod +x /home/${env.SSH_USER}/deploy.sh
                               /home/${env.SSH_USER}/deploy.sh
                           "
                       """
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
