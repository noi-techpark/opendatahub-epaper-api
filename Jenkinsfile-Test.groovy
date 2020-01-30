pipeline {
    agent any

    environment {
        AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
		DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/e-paper-displays-api'
		DOCKER_TAG = "test-$BUILD_NUMBER"

        DB_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/epaper"
        DB_USERNAME = "epaper"
        DB_PASSWORD = credentials('epaper-api-test-db-password')
        SERVER_ADDR = "0.0.0.0"
        SERVER_PORT = 8080
        APP_DEBUG = false
    }

	stages {
        stage('Configure') {
            steps {
                sh 'cp .env.example .env'
            }
        }

        stage('Test') {
            steps {
				sh '''
					docker-compose build --pull --build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)
					docker-compose run --rm -u $(id -u jenkins):$(id -g jenkins) app "mvn -B -U clean test"
				'''
            }
        }
		stage('Build') {
            steps {
				sh '''
					aws ecr get-login --region eu-west-1 --no-include-email | bash
					docker-compose -f docker-compose.build.yml build --pull
					docker-compose -f docker-compose.build.yml push
				'''
            }
        }
		// stage('Deploy') {
        //     steps {
		// 		sh '''

		// 		'''
        //     }
        // }
    }
}
