pipeline {
    agent none
	environment {
		API_URL = "https://api.epaper.opendatahub.testingmachine.eu"
		WS_URL = "ws://localhost/ws"
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
        stage('Test Python Tunnel Proxy') {
			agent {
				dockerfile {
					filename 'infrastructure/docker/proxy.dockerfile'
				}
			}
            steps {
                sh '''
					cd proxy
					echo "API_URL=$API_URL" > .env
					touch local-tunnel.log
					echo "Run proxy.py. If the timeout must kill it, everything should be fine"
					timeout -s SIGKILL 5 python3 proxy.py || test "137" = "$?"
				'''
            }
        }
        stage('Test Python Web Socket Proxy') {
			agent {
				dockerfile {
					filename 'infrastructure/docker/proxy.dockerfile'
				}
			}
            steps {
                sh '''
					cd websocket-proxy
					echo "API_URL=$API_URL" > .env
					echo "WS_URL=$WS_URL" >> .env
					echo "Run websocket-proxy.py. If the timeout must kill it, everything should be fine"
					timeout -s SIGKILL 5 python3 websocket-proxy.py || test "137" = "$?"
				'''
            }
        }
    }
}
