pipeline {
    agent none
    stages {
		agent {
	        dockerfile {
    	        filename 'infrastructure/docker/java.dockerfile'
        	    additionalBuildArgs '--build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)'
        	}
    	}
        stage('Test Java API') {
            steps {
                sh 'mvn -B -U clean test'
            }
        }
    }
    stages {
		agent {
	        dockerfile {
    	        filename 'infrastructure/docker/proxy.dockerfile'
        	}
    	}
        stage('Test Python Proxy') {
            steps {
                sh 'python3 /code/proxy.py'
            }
        }
    }
}
