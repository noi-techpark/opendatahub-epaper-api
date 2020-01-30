pipeline {
    agent any

    environment {
        AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
		DOCKER_PROJECT_NAME = "e-ink-displays-api"
		DOCKER_SERVER_IP = "63.33.73.203"
        DOCKER_SERVER_DIRECTORY = "/var/docker/e-ink-displays-api"
		DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/e-ink-displays-api'
		DOCKER_TAG = "test-$BUILD_NUMBER"

        DB_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/epaper"
        DB_USERNAME = "epaper"
        DB_PASSWORD = credentials('epaper-api-test-db-password')
    }

	stages {
        stage('Configure') {
            steps {
                sh """
					rm -f .env
					cp .env.example .env
					echo -e "\n" >> .env
                	echo 'COMPOSE_PROJECT_NAME=${DOCKER_PROJECT_NAME}' >> .env
                	echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                	echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env
					echo 'DB_URL=${DB_URL}' >> .env
					echo 'DB_USERNAME=${DB_USERNAME}' >> .env
					echo 'DB_PASSWORD=${DB_PASSWORD}' >> .env
				"""
            }
        }

        stage('Test') {
            steps {
				sh '''
					docker-compose --no-ansi build --pull --build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)
					docker-compose --no-ansi run --rm --no-deps -u $(id -u jenkins):$(id -g jenkins) app "mvn -B -U clean test"
				'''
            }
        }
		stage('Build') {
            steps {
				sh '''
					aws ecr get-login --region eu-west-1 --no-include-email | bash
					docker-compose --no-ansi -f docker-compose.build.yml build --pull
					docker-compose --no-ansi -f docker-compose.build.yml push
				'''
            }
        }
		stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
					    ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" bash -c "aws ecr get-login --region eu-west-1 --no-include-email | bash"'

						ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'mkdir -p ${DOCKER_SERVER_DIRECTORY}'

                    	ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'ls -1t ${DOCKER_SERVER_DIRECTORY}/releases/ | tail -n +10 | grep -v `readlink -f ${DOCKER_SERVER_DIRECTORY}/current | xargs basename --` -- | xargs -r printf \"${DOCKER_SERVER_DIRECTORY}/releases/%s\\n\" | xargs -r rm -rf --'

                    	ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'mkdir -p ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}'
						pv docker-compose.run.yml | ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'tee ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}/docker-compose.yml'
						pv .env | ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'tee ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}/.env'
                    	ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'cd ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER} && docker-compose --no-ansi pull'

                    	ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} '[ -d \"${DOCKER_SERVER_DIRECTORY}/current\" ] && (cd ${DOCKER_SERVER_DIRECTORY}/current && docker-compose --no-ansi down) || true'
                    	ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'ln -sfn ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER} ${DOCKER_SERVER_DIRECTORY}/current'
                    	ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} 'cd ${DOCKER_SERVER_DIRECTORY}/current && docker-compose --no-ansi up --detach'
					"""
                }
            }
        }
    }
}
