kubectl apply -f elastic-sts-svc.yaml -n $NAMESPACE
kubectl apply -f kibana-deploy-svc.yaml -n $NAMESPACE
kubectl apply -f logstash-sts-svc-cm.yaml -n $NAMESPACE