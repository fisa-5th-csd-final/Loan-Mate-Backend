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
                       sh '''
                       ssh -o StrictHostKeyChecking=no ubuntu@172.31.10.208 '
                       set -e # 명령어 실패 시 즉시 종료

                       echo "작업 디렉토리로 이동: ~/Loan-Mate-Backend/docker"
                       cd ~/Loan-Mate-Backend/docker

                       echo "Git 최신 develop 브랜치로 Hard Reset 수행"
                       git fetch origin develop
                       git reset --hard origin/develop

                       echo "Docker 이미지 재빌드 및 서비스 재시작"
                       docker-compose up -d --build

                       echo "배포 완료!"
                       '
                       '''
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
