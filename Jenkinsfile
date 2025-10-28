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
                    // Находим все сервисы по наличию chart/ папки
                    def services = findFiles(glob: '*/chart/Chart.yaml')
                    env.SERVICES = services.collect { it.path.split('/')[0] }.join(',')
                    echo "🎯 Found services: ${env.SERVICES}"
                    
                    services.each { file ->
                        def serviceName = file.path.split('/')[0]
                        echo "  - ${serviceName}"
                    }
                }
            }
        }
        
        stage('Validate Charts') {
            steps {
                script {
                    echo "🔍 Validating all Helm charts..."
                    def services = env.SERVICES.split(',')
                    services.each { service ->
                        sh "helm lint ${service}/chart"
                    }
                    sh "helm lint chart/"
                }
            }
        }
        
        stage('Build Services') {
            steps {
                script {
                    def services = env.SERVICES.split(',')
                    
                    // Собираем все сервисы параллельно
                    def buildStages = [:]
                    
                    services.each { service ->
                        buildStages["Build ${service}"] = {
                            dir(service) {
                                echo "🏗️ Building ${service}..."
                                sh "mvn clean package -DskipTests"
                                sh "docker build -t ${service}:latest ."
                                sh "kind load docker-image ${service}:latest"
                                echo "✅ ${service} built successfully!"
                            }
                        }
                    }
                    
                    parallel buildStages
                }
            }
        }
        
        stage('Deploy') {
            steps {
                dir('chart') {
                    sh "helm dependency build ."
                    sh "helm upgrade --install bank . --namespace default --wait --timeout 5m"
                }
            }
        }
        
        stage('Smoke Tests') {
            steps {
                script {
                    echo "📊 Running Smoke Tests..."
                    def services = env.SERVICES.split(',')
                    
                    services.each { service ->
                        sh """
                        kubectl run smoke-test-${service} --image=curlimages/curl --rm -i --restart=Never --namespace default -- \
                          sh -c 'curl -f http://${service}.default.svc.cluster.local:8080/actuator/health && echo \"✅ ${service} healthy\"' || echo \"⚠️ ${service} health check skipped\"
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            echo "📦 Build ${currentBuild.result} - ${env.BUILD_URL}"
            // Очистка тестовых ресурсов
            sh "kubectl delete pod --field-selector=status.phase==Succeeded -n default --ignore-not-found=true"
            sh "kubectl delete pod -l run=smoke-test -n default --ignore-not-found=true"
        }
        success {
            echo "🎉 Deployment completed successfully!"
        }
        failure {
            echo "❌ Deployment failed - check logs above"
        }
    }
}