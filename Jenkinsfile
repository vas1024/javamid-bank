pipeline {
    agent any
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(false)
    }
    stages {

stage('Discover Services') {
    steps {
        script {
            def servicesOutput = sh(
                script: '''
                grep "name:" chart/Chart.yaml | awk '{print $NF}' | paste -sd ','
                ''',
                returnStdout: true
            ).trim()
           
            env.SERVICES = servicesOutput
            echo "Services discovered from chart/Chart.yaml: ${env.SERVICES}"
        }
    }
}
        
        stage('Validate Charts') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Validating Helm charts..."
                    def services = env.SERVICES.split(',')
                    
                    // ��������� ������������� chart'�� ����� ����������
                    services.each { service ->
                        if (fileExists("${service}/chart/Chart.yaml")) {
                            echo "Validating ${service} chart..."
                            sh "helm lint ${service}/chart"
                        } else {
                            echo "?? No chart found for ${service}, skipping validation"
                        }
                    }
                }
            }
        }
        
        stage('Build Services') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    def services = env.SERVICES.split(',')
                    
                    // �������� ��� ������� �����������
                    def buildStages = [:]
                    
                    services.each { service ->
                        buildStages["Build ${service}"] = {
                            dir(service) {
                                echo "??? Building ${service}..."
                                
                                // ��������� ������������� pom.xml ����� �������
                                if (fileExists('pom.xml')) {
                                    sh "mvn clean package -DskipTests"
                                    sh "docker build -t ${service}:latest ."
                                    sh "kind load docker-image ${service}:latest"
                                    echo "? ${service} built successfully!"
                                } else {
                                    echo "?? No pom.xml found in ${service}, skipping build"
                                }
                            }
                        }
                    }
                    
                    parallel buildStages
                }
            }
        }
        
        stage('Deploy Services') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Deploying services individually..."
                    def services = env.SERVICES.split(',')
                    
                    // ������� ������ ������ �����������
                    def deployStages = [:]
                    
                    services.each { service ->
                        deployStages["Deploy ${service}"] = {
                            script {
                                if (fileExists("${service}/chart/Chart.yaml")) {
                                    echo "?? Deploying ${service}..."
                                    
                                    // ��������� ������������� values.yaml
                                    def valuesFile = "${service}/chart/values.yaml"
                                    def valuesArg = fileExists(valuesFile) ? "-f ${valuesFile}" : ""
                                    
                                    sh """
                                    helm upgrade --install ${service} ${service}/chart \
                                        --namespace default \
                                        --set image.tag=latest \
                                        --wait \
                                        --timeout 3m \
                                        ${valuesArg}
                                    """
                                    echo "? ${service} deployed successfully!"
                                } else {
                                    echo "?? No chart found for ${service}, skipping deployment"
                                }
                            }
                        }
                    }
                    
                    parallel deployStages
                }
            }
        }
        
        stage('Verify Deployment') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Checking deployment status..."
                    def services = env.SERVICES.split(',')
                    
                    services.each { service ->
                        try {
                            echo "Checking ${service}..."
                            sh """
                            kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=${service} --namespace default --timeout=60s
                            echo "? ${service} is ready"
                            """
                        } catch (Exception e) {
                            echo "?? ${service} is not ready yet: ${e.message}"
                        }
                    }
                    
                    // ���������� ����� ������
                    sh "kubectl get pods -l app.kubernetes.io/name -n default --no-headers | wc -l"
                    sh "kubectl get deployments -n default"
                }
            }
        }
        
 

stage('Smoke Tests') {
    steps {
        script {
            echo "?? Running Smoke Tests..."
            
            // ������� ������ �������� ���
            sh 'kubectl delete pod smoke-test-bank --ignore-not-found=true --namespace default'
            
            // ��������� ������� ��� ���� ��������
            def services = env.SERVICES.split(',')
            def curlCommands = services.collect { service ->
                "curl -f http://${service}:8080/actuator/health && echo \"? ${service} healthy\""
            }.join(' && ')
            
            // ������� ��� � ����� ��������
            sh """
            kubectl run smoke-test-bank \
                --image=curlimages/curl \
                --namespace default \
                --restart=Never \
                -- \
                sh -c '
                  echo "?? Starting comprehensive smoke tests..." &&
                  ${curlCommands} &&
                  echo "?? All services are healthy!"
                '
            """
            
            // ���� � ��������
            sh '''
            echo "logs for smoke-test-bank..."
            kubectl logs smoke-test-bank --namespace default -f
            '''
            
            // ��������� ���������
            def result = sh(
                script: 'kubectl get pod smoke-test-bank -o jsonpath="{.status.phase}" --namespace default',
                returnStdout: true
            ).trim()

           // ������� ��� ����� ������
            sh 'kubectl delete pod smoke-test-bank --namespace default --ignore-not-found=true'
            
            if (result != "Succeeded") {
                error "? Smoke tests failed"
            } else {
                echo "? All smoke tests passed!"
            }
        }
    }
}



stage('Helm Tests') {
    steps {
        script {
            echo "?? Running Helm tests..."
            sh "helm test bank --namespace default --timeout 3m"
        }
    }
}




    }
    post {
        always {
            echo "?? Build ${currentBuild.result} - ${env.BUILD_URL}"
            // ������� �������� ��������
            sh "kubectl delete pod --field-selector=status.phase==Succeeded -n default --ignore-not-found=true"
            sh "kubectl delete pod -l run=smoke-test -n default --ignore-not-found=true"
        }
        success {
            echo "?? All services deployed successfully!"
            // ���������� ��������� ������
            sh "kubectl get pods -n default"
            sh "kubectl get services -n default"
        }
        failure {
            echo "? Deployment failed - check logs above"
            sh "kubectl get pods -n default"
            sh "kubectl describe pods -n default | grep -A 10 -B 5 Error || true"
        }
    }
}

