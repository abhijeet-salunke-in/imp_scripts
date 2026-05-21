/*
=====================================================================
JENKINS JOB CONFIGURATION
=====================================================================

Job Name:
    nodejs-rollback

Job Type:
    Pipeline

Configuration Steps:

1. Open Jenkins Dashboard

2. Create New Item
    → Enter Job Name: nodejs-rollback
    → Select: Pipeline

3. Enable Parameterized Build
    ✔ Check:
        This project is parameterized

4. Add String Parameter

    Name:
        ROLLBACK_VERSION

    Default Value:
        Leave Empty

    Description:
        Enter version to rollback (Example: 1.0.0)

5. Pipeline Section
    Definition:
        Pipeline script

6. Paste This Complete Groovy Script

7. Save Job

8. Build Process
    → Click "Build with Parameters"
    → Enter rollback version
    Example:
        1.0.0
        2.0.0
        3.0.0

=====================================================================
PIPELINE SCRIPT STARTS HERE
=====================================================================
*/

pipeline {

    // Run pipeline on any available Jenkins agent
    agent any

    /*
    -----------------------------------------------------------------
    Build Parameters
    -----------------------------------------------------------------
    */
    parameters {

        string(
            name: 'ROLLBACK_VERSION',
            defaultValue: '',
            description: 'Enter version to rollback (Example: 1.0.0)'
        )
    }

    /*
    -----------------------------------------------------------------
    Global Environment Variables
    -----------------------------------------------------------------
    */
    environment {

        // Application name
        APP_NAME = "nodeapp"

        // Deployment directory
        DEPLOY_DIR = "/var/lib/jenkins/deployments"

        // Nexus Repository URL
        NEXUS_URL = "http://10.0.2.15:8081/repository/nodejs-repo"
    }

    stages {

        /*
        -----------------------------------------------------------------
        Stage: Validate Rollback Version
        Description:
            Ensure rollback version is provided
        -----------------------------------------------------------------
        */
        stage('Validate Input') {
            steps {

                script {

                    if (!params.ROLLBACK_VERSION?.trim()) {

                        error("ROLLBACK_VERSION parameter cannot be empty.")
                    }
                }
            }
        }

        /*
        -----------------------------------------------------------------
        Stage: Clean Existing Deployment
        Description:
            Remove old rollback deployment folder if present
        -----------------------------------------------------------------
        */
        stage('Clean Old Deployment') {
            steps {

                sh """
                rm -rf ${DEPLOY_DIR}/${APP_NAME}-${params.ROLLBACK_VERSION}
                """
            }
        }

        /*
        -----------------------------------------------------------------
        Stage: Download Artifact from Nexus
        Description:
            Download selected version from Nexus repository
        -----------------------------------------------------------------
        */
        stage('Download Selected Version from Nexus') {
            steps {

                sh """
                cd ${DEPLOY_DIR}

                wget -O ${APP_NAME}-${params.ROLLBACK_VERSION}.tar.gz \
                ${NEXUS_URL}/${APP_NAME}-${params.ROLLBACK_VERSION}.tar.gz
                """
            }
        }

        /*
        -----------------------------------------------------------------
        Stage: Extract Artifact
        Description:
            Extract downloaded artifact package
        -----------------------------------------------------------------
        */
        stage('Extract Artifact') {
            steps {

                sh """
                cd ${DEPLOY_DIR}

                mkdir -p ${APP_NAME}-${params.ROLLBACK_VERSION}

                tar -xzf ${APP_NAME}-${params.ROLLBACK_VERSION}.tar.gz \
                -C ${APP_NAME}-${params.ROLLBACK_VERSION}
                """
            }
        }

        /*
        -----------------------------------------------------------------
        Stage: Restart Application Using PM2
        Description:
            Restart selected rollback version using PM2
        -----------------------------------------------------------------
        */
        stage('Restart with PM2') {
            steps {

                sh """

                # Stop currently running application
                pm2 delete ${APP_NAME} || true

                # Navigate to rollback deployment folder
                cd ${DEPLOY_DIR}/${APP_NAME}-${params.ROLLBACK_VERSION}

                # Install production dependencies
                npm install --production

                # Start application using PM2
                pm2 start index.js --name ${APP_NAME}

                # Save PM2 process configuration
                pm2 save
                """
            }
        }

        /*
        -----------------------------------------------------------------
        Stage: Verify Rollback
        Description:
            Verify PM2 process status
        -----------------------------------------------------------------
        */
        stage('Verify Rollback') {
            steps {

                sh "pm2 list"

                echo "Rollback to version ${params.ROLLBACK_VERSION} completed successfully 🚀"
            }
        }
    }

    /*
    -----------------------------------------------------------------
    Post Build Actions
    -----------------------------------------------------------------
    */
    post {

        success {

            echo "Rollback pipeline executed successfully."
        }

        failure {

            echo "Rollback pipeline failed."
        }

        always {

            echo "Rollback process completed."
        }
    }
}
