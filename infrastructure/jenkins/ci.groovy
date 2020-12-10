pipeline {
    agent none
    stages {
        stage('Test Java API') {
			agent {
				dockerfile {
					filename 'infrastructure/docker/java.dockerfile'
					additionalBuildArgs '--build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)'
				}
			}
            steps {
                sh 'mvn -B -U clean test'
            }
        }
        stage('Test Python Proxy') {
			agent {
				dockerfile {
					filename 'infrastructure/docker/proxy.dockerfile'
				}
			}
            steps {
                sh 'python3 /code/proxy.py'
            }
        }
    }
}
