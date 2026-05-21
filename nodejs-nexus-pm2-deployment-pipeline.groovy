/*
------------------------------------------------------------
Pipeline Name : Node.js CI/CD Pipeline with Nexus + PM2
Description   : 
    - Clones Node.js application source code
    - Installs dependencies
    - Creates deployable tar.gz artifact
    - Uploads artifact to Nexus Repository
    - Deploys application using PM2 process manager
    - Maintains version-based deployments
    
------------------------------------------------------------
*/

pipeline {

    // Run pipeline on any available Jenkins agent
    agent any

    // Global environment variables
    environment {

        // Application details
        APP_NAME = "nodeapp"
        VERSION  = "3.0.0"

        // Nexus Repository configuration
        NEXUS_URL  = "http://10.0.2.15:8081"
        NEXUS_REPO = "nodejs-repo"

        // Deployment directories
        DEPLOY_DIR  = "/var/lib/jenkins/deployments"
        CURRENT_LINK = "/var/lib/jenkins/current"
    }

    stages {

        /*
        ------------------------------------------------------------
        Stage: Checkout Source Code
        Description:
            Pull source code from GitHub repository
        ------------------------------------------------------------
        */
        stage('Checkout') {
            steps {

                git branch: 'main',
                url: 'https://github.com/abhijeet-salunke-in/node1.git'
            }
        }

        /*
        ------------------------------------------------------------
        Stage: Install Dependencies
        Description:
            Install Node.js project dependencies
        ------------------------------------------------------------
        */
        stage('Install Dependencies') {
            steps {

                sh 'npm install'
            }
        }

        /*
        ------------------------------------------------------------
        Stage: Clean Old Artifacts
        Description:
            Remove previously generated tar.gz files
        ------------------------------------------------------------
        */
        stage('Clean Old Artifacts') {
            steps {

                sh 'rm -f *.tar.gz'
            }
        }

        /*
        ------------------------------------------------------------
        Stage: Create Build Artifact
        Description:
            Create compressed deployment package
            Excludes unnecessary files and folders
        ------------------------------------------------------------
        */
        stage('Create Artifact') {
            steps {

                sh """
                mkdir -p build

                tar \
                --exclude='.git' \
                --exclude='node_modules' \
                --exclude='*.tar.gz' \
                -czf build/${APP_NAME}-${VERSION}.tar.gz .
                """
            }
        }

        /*
        ------------------------------------------------------------
        Stage: Upload Artifact to Nexus
        Description:
            Upload generated artifact to Nexus repository
            using secured Jenkins credentials
        ------------------------------------------------------------
        */
        stage('Upload to Nexus') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-node',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )
                ]) {

                    sh """
                    curl -v -u $NEXUS_USER:$NEXUS_PASS \
                    --upload-file build/${APP_NAME}-${VERSION}.tar.gz \
                    ${NEXUS_URL}/repository/${NEXUS_REPO}/${APP_NAME}-${VERSION}.tar.gz
                    """
                }
            }
        }

        /*
        ------------------------------------------------------------
        Stage: Deploy Application Using PM2
        Description:
            - Create version-based deployment directory
            - Extract artifact
            - Install production dependencies
            - Update symbolic link for current version
            - Restart application using PM2
        ------------------------------------------------------------
        */
        stage('Deploy with PM2') {
            steps {

                sh """
                # Create deployment directory
                mkdir -p ${DEPLOY_DIR}/${APP_NAME}-${VERSION}

                # Extract application artifact
                tar -xzf build/${APP_NAME}-${VERSION}.tar.gz \
                -C ${DEPLOY_DIR}/${APP_NAME}-${VERSION}

                # Move into deployment directory
                cd ${DEPLOY_DIR}/${APP_NAME}-${VERSION}

                # Install only production dependencies
                npm install --production

                # Update current symbolic link
                ln -sfn ${DEPLOY_DIR}/${APP_NAME}-${VERSION} ${CURRENT_LINK}

                # Stop old PM2 process if running
                pm2 delete ${APP_NAME} || true

                # Start new application version
                pm2 start ${CURRENT_LINK}/index.js --name ${APP_NAME}

                # Save PM2 process list
                pm2 save
                """
            }
        }

        /*
        ------------------------------------------------------------
        Stage: Verify Deployment
        Description:
            Verify PM2 process status after deployment
        ------------------------------------------------------------
        */
        stage('Verify Deployment') {
            steps {

                sh "pm2 list"

                echo "Deployment Completed Successfully 🚀"
            }
        }
    }

    /*
    ------------------------------------------------------------
    Post Build Actions
    ------------------------------------------------------------
    */
    post {

        // Executes if pipeline succeeds
        success {
            echo "Pipeline executed successfully."
        }

        // Executes if pipeline fails
        failure {
            echo "Pipeline execution failed."
        }
    }
}
