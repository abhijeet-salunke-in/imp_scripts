/*
====================================================================
PROJECT : Java CI/CD Pipeline with SonarQube, Nexus & Tomcat
AUTHOR  : Abhijeet Salunke
====================================================================

OVERVIEW

This project demonstrates a complete CI/CD workflow using:

1. GitHub (Source Code Management)
2. Jenkins (Automation Server)
3. Maven (Build Tool)
4. SonarQube (Code Quality Analysis)
5. Nexus Repository (Artifact Storage)
6. Apache Tomcat (Application Deployment)

Pipeline Flow:

GitHub
   ↓
Jenkins
   ↓
Maven Build
   ↓
SonarQube Analysis
   ↓
Nexus Artifact Upload
   ↓
Tomcat Deployment

====================================================================
PREREQUISITE 1 : START SONARQUBE
====================================================================

If SonarQube is running inside Docker:

docker start sonarqube

Verify:

http://10.0.2.15:9000

Generate SonarQube Token:

Administration
→ Security
→ Users
→ Tokens
→ Generate Token

====================================================================
PREREQUISITE 2 : START NEXUS REPOSITORY
====================================================================

docker start nexus

Verify:

http://10.0.2.15:8081

Create Hosted Repository:

Repository Name : java3
Format          : Maven2
Type            : Hosted

====================================================================
PREREQUISITE 3 : START APACHE TOMCAT
====================================================================

/opt/tomcat/bin/startup.sh

Verify:

http://10.0.2.15:8082

====================================================================
TOMCAT CONFIGURATION
====================================================================

Edit file:

/opt/tomcat/conf/tomcat-users.xml

Add:

<role rolename="manager-gui"/>
<role rolename="manager-script"/>

<user
    username="tomcat"
    password="tomcat123"
    roles="manager-gui,manager-script"/>

Restart Tomcat:

/opt/tomcat/bin/shutdown.sh
/opt/tomcat/bin/startup.sh

====================================================================
JENKINS CONFIGURATION
====================================================================

Manage Jenkins
→ Plugins

Install:

1. Git Plugin
2. Pipeline Plugin
3. Maven Integration Plugin
4. SonarQube Scanner Plugin
5. Nexus Artifact Uploader Plugin
6. Deploy to Container Plugin

====================================================================
ADD SONARQUBE TOKEN IN JENKINS
====================================================================

Manage Jenkins
→ Credentials
→ Global
→ Add Credentials

Kind : Secret Text

ID     : sonar-token-s
Secret : <Generated SonarQube Token>

Save

====================================================================
CONFIGURE SONARQUBE SERVER IN JENKINS
====================================================================

Manage Jenkins
→ System
→ SonarQube Servers

Add SonarQube

Name        : sonarqube
Server URL  : http://10.0.2.15:9000
Credentials : sonar-token-s

Save

====================================================================
ADD NEXUS CREDENTIALS
====================================================================

Manage Jenkins
→ Credentials
→ Global
→ Add Credentials

Kind : Username with Password

ID       : nexus_id
Username : admin
Password : ********

Save

====================================================================
ADD TOMCAT CREDENTIALS
====================================================================

Manage Jenkins
→ Credentials
→ Global
→ Add Credentials

Kind : Username with Password

Username : tomcat
Password : tomcat123

Copy Generated Credential ID

Example:

2a316380-8729-4e9f-8349-68b1137f2b24

====================================================================
EXPECTED RESULT
====================================================================

✔ Source Code Checkout from GitHub
✔ Maven Build Success
✔ SonarQube Analysis Success
✔ WAR File Uploaded to Nexus
✔ Application Deployed to Tomcat

Application URL:

http://10.0.2.15:8082/java3

====================================================================
JENKINS PIPELINE
====================================================================
*/

pipeline {
    agent any

    stages {

        /*
        ----------------------------------------------------------
        Stage: Source Code Checkout
        ----------------------------------------------------------
        */
        stage('code') {
            steps {
                git branch: 'main',
                url: 'https://github.com/abhijeet-salunke-in/java3.git'
            }
        }

        /*
        ----------------------------------------------------------
        Stage: Build Artifact
        ----------------------------------------------------------
        */
        stage('Build Artifact') {
            steps {
                sh "mvn clean package"
            }
        }

        /*
        ----------------------------------------------------------
        Stage: SonarQube Analysis
        ----------------------------------------------------------
        */
        stage('Test Using SonarQube') {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube') {

                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=java3 \
                    -Dsonar.projectName=java3
                    '''
                }
            }
        }

        /*
        ----------------------------------------------------------
        Stage: Upload Artifact to Nexus
        ----------------------------------------------------------
        */
        stage('Save Artifact to Nexus') {
            steps {

                nexusArtifactUploader(
                    artifacts: [[
                        artifactId: 'webapp',
                        classifier: '',
                        file: 'target/webapp.war',
                        type: 'war'
                    ]],
                    credentialsId: 'nexus_id',
                    groupId: 'abhi',
                    nexusUrl: '10.0.2.15:8081',
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    repository: 'java3',
                    version: '1.0.0'
                )
            }
        }

        /*
        ----------------------------------------------------------
        Stage: Deploy to Tomcat
        ----------------------------------------------------------
        */
        stage('Deploy to Tomcat') {
            steps {

                deploy(
                    adapters: [
                        tomcat9(
                            alternativeDeploymentContext: '',
                            credentialsId: '2a316380-8729-4e9f-8349-68b1137f2b24',
                            path: '',
                            url: 'http://10.0.2.15:8082/'
                        )
                    ],
                    contextPath: 'java3',
                    war: 'target/*.war'
                )
            }
        }
    }
}
