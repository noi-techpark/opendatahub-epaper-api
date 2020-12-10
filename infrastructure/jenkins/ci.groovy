pipeline {
    agent none
	environment {
		API_URL = "https://api.epaper.opendatahub.testingmachine.eu"
    }
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
                sh '''
					echo "API_URL=$API_URL" > .env
					touch local-tunnel.log
					python3 proxy.py
				'''
            }
        }
    }
}
