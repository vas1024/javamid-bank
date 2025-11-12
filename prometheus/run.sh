helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update prometheus-community
helm install prometheus prometheus-community/kube-prometheus-stack --set admissionWebhooks.enabled=false
kubectl apply -f servicemonitor.yaml
export GRAFANA_PW=`kubectl --namespace default get secrets prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 -d ; echo`