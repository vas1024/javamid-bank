helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update prometheus-community

helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
  --namespace $NAMESPACE \
  --set prometheusOperator.admissionWebhooks.enabled=false \
  --set prometheusOperator.tls.enabled=false \
  --set prometheusOperator.tlsProxy.enabled=false \
  --set grafana.sidecar.dashboards.enabled=true \
  --set grafana.sidecar.dashboards.label=grafana_dashboard \
  --set grafana.adminPassword=admin \
  --set kubeStateMetrics.enabled=false \
  --set nodeExporter.enabled=false \
  --set prometheus-node-exporter.enabled=false \
  --wait \
  --timeout 10m

kubectl apply -f servicemonitor.yaml -n $NAMESPACE
kubectl apply -f prometheusrule.yaml -n $NAMESPACE
kubectl apply -f grafana-dashboard-configmap.yaml -n $NAMESPACE

export GRAFANA_PW=`kubectl --namespace $NAMESPACE get secrets prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 -d ; echo`