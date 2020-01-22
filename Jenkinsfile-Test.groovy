pipeline {
    agent {
        dockerfile {
            filename 'docker/dockerfile-java'
            additionalBuildArgs '--build-arg JENKINS_USER_ID=`id -u jenkins` --build-arg JENKINS_GROUP_ID=`id -g jenkins`'
        }
    }

    environment {
        TESTSERVER_TOMCAT_ENDPOINT = "http://api.epaper.tomcat02.testingmachine.eu:8080/manager/text"
        TESTSERVER_TOMCAT_CREDENTIALS = credentials('testserver-tomcat8-credentials')

        DB_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/epaper"
        DB_USERNAME = "epaper"
        DB_PASSWORD = credentials('epaper-api-test-db-password')
        SERVER_ADDR = "0.0.0.0"
        SERVER_PORT = 8081
        APP_DEBUG = false
    }

    stages {
        stage('Configure') {
            steps {
                sh '''
                    sed -i -e "s/<\\/settings>$//g\" ~/.m2/settings.xml
                    echo "    <servers>" >> ~/.m2/settings.xml
                    echo "        ${TESTSERVER_TOMCAT_CREDENTIALS}" >> ~/.m2/settings.xml
                    echo "    </servers>" >> ~/.m2/settings.xml
                    echo "</settings>" >> ~/.m2/settings.xml

                    sed -i -e "s%\\(spring.datasource.url\\s*=\\).*\\$%\\1${DB_URL}%" src/main/resources/application.properties
                    sed -i -e "s%\\(spring.datasource.username\\s*=\\).*\\$%\\1${DB_USERNAME}%" src/main/resources/application.properties
                    sed -i -e "s%\\(spring.datasource.password\\s*=\\).*\\$%\\1${DB_PASSWORD}%" src/main/resources/application.properties
                    sed -i -e "s%\\(debug\\s*=\\).*\\$%\\1${APP_DEBUG}%" src/main/resources/application.properties
                    sed -i -e "s%\\(server.address\\s*=\\).*\\$%\\1${SERVER_ADDR}%" src/main/resources/application.properties
                    sed -i -e "s%\\(server.port\\s*=\\).*\\$%\\1${SERVER_PORT}%" src/main/resources/application.properties
                '''
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B -U clean test'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -U -Dmaven.test.skip=true clean package'
            }
        }
        stage('Deploy') {
            steps{
                sh 'mvn -B -U -Dmaven.test.skip=true tomcat:redeploy -Dmaven.tomcat.url=${TESTSERVER_TOMCAT_ENDPOINT} -Dmaven.tomcat.server=testServer -Dmaven.tomcat.path=/'
            }
        }
    }
}
