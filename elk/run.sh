kubectl apply -f elastic-sts-svc.yaml
kubectl apply -f kibana-deploy-svc.yaml
rm -rf helm-charts
git clone https://github.com/elastic/helm-charts.git
docker pull logstash:9.1.7
kind load docker-image logstash:9.1.7
helm upgrade --install logstash ./helm-charts/logstash -f ./logstash-values.yaml
