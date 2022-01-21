pipeline {
    agent any
    environment {
        COMPOSE_PROJECT_NAME = "e-ink-display-api"
        DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/e-ink-displays-api'
        DOCKER_TAG = "test-$BUILD_NUMBER"
		ANSIBLE_LIMIT = "test"

		SERVER_PORT = "1012"

        DB_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/epaper"
        DB_USERNAME = "epaper"
        DB_PASSWORD = credentials('epaper-api-test-db-password')

		APP_DEBUG = true
		PROXY_ENABLED = false
		PROXY_URL = "http://localhost:19998"
		WEBSOCKET_ENABLED = true
		NOI_EVENTS_ENABLED = true
		NOI_CRON_EVENTS = "0 0 0/12 * * ?"
		NOI_CRON_DISPLAYS = "0 0/10 6-22 * * ?"
		CRON_HEARTBEAT = "0 0 0/1 * * ?"

        S3_REGION = "eu-west-1"
        S3_BUCKET_NAME = "it.bz.opendatahub.epaper.images-test"
        S3_ACCESS_KEY = credentials('epaper-test-s3-access-key')
        S3_SECRET_KEY = credentials('epaper-test-s3-secret-key')
    }
    stages {
        stage('Configure') {
            steps {
                sh """
                    rm -f .env
                    echo 'COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT_NAME}' >> .env
                    echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                    echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env
					echo 'SERVER_PORT=${SERVER_PORT}' >> .env
                    echo 'DB_URL=${DB_URL}' >> .env
                    echo 'DB_USERNAME=${DB_USERNAME}' >> .env
                    echo 'DB_PASSWORD=${DB_PASSWORD}' >> .env
					echo 'APP_DEBUG=${APP_DEBUG}' >> .env
					echo 'PROXY_ENABLED=${PROXY_ENABLED}' >> .env
					echo 'PROXY_URL=${PROXY_URL}' >> .env
					echo 'WEBSOCKET_ENABLED=${WEBSOCKET_ENABLED}' >> .env
					echo 'NOI_EVENTS_ENABLED=${NOI_EVENTS_ENABLED}' >> .env
					echo 'NOI_CRON_EVENTS=${NOI_CRON_EVENTS}' >> .env
					echo 'NOI_CRON_DISPLAYS=${NOI_CRON_DISPLAYS}' >> .env
					echo 'CRON_HEARTBEAT=${CRON_HEARTBEAT}' >> .env
                    echo 'S3_REGION=${S3_REGION}' >> .env
                    echo 'S3_BUCKET_NAME=${S3_BUCKET_NAME}' >> .env
                    echo 'S3_ACCESS_KEY=${S3_ACCESS_KEY}' >> .env
                    echo 'S3_SECRET_KEY=${S3_SECRET_KEY}' >> .env
                """
            }
        }
        stage('Test') {
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
        stage('Build') {
            steps {
                sh '''
                    aws ecr get-login --region eu-west-1 --no-include-email | bash
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml build --pull
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml push
                '''
            }
        }
        stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
						cd infrastructure/ansible
                        ansible-galaxy install -f -r requirements.yml
                        ansible-playbook --limit=${ANSIBLE_LIMIT} deploy.yml --extra-vars "release_name=${BUILD_NUMBER}"
                    """
                }
            }
        }
    }
}
