pipeline {
    agent any

	stages {
        stage('Configure') {
            steps {
                sh 'cp .env.example .env'
            }
        }
    }

	stages {
        stage('Build') {
            steps {
				sh '''
					docker-compose build --pull --build-arg JENKINS_USER_ID=`id -u jenkins` --build-arg JENKINS_GROUP_ID=`id -g jenkins`
				'''
            }
        }
    }

    stages {
        stage('Test') {
            steps {
				sh '''
					docker-compose run --rm -u `id -u jenkins`:`id -g jenkins` java mvn -B -U clean test
				'''
            }
        }
    }
}
