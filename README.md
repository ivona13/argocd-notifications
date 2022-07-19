### Prerequisites:
* cluster (minikube)
* set <code>kube_api</code> in application.yaml
  ```
   kubectl cluster-info
  ```
* set JWT for minikube - environment variable <code>$TOKEN</code>
  ```
  JWT_TOKEN_DEFAULT_DEFAULT=$(kubectl get secrets \
    $(kubectl get serviceaccounts/default -o jsonpath='{.secrets[0].name}') \
    -o jsonpath='{.data.token}' | base64 --decode)
  ```

* set argo url in application.yaml

* set argo password as environment variable <code>$ARGO_PASSWORD</code>

* to enable applying configMap manifest files to your minikube cluster, you have to do the following:

    1. Create a Cluster role with the resource you need
    
    ```
    kubectl create clusterrole deployer --verb=get,list,watch,create,delete,patch,update --resource=configmaps
    ```
    2. Bind it to your service account
    ```
  	kubectl create clusterrolebinding deployer-srvacct-default-binding --clusterrole=deployer --serviceaccount=default:default
    ```
  
### Flow:
* Enter argocd-notifications-cm yaml file to apply to the cluster

For example:
```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: argocd-notifications-cm
  namespace: argocd
data:
  context: |
    argocdUrl: https://localhost:8080
  service.webhook.github-webhook: |
    url: https://api.github.com
    headers:
    - name: Authorization
      value: token $TOKEN
    subscriptions: |
      - recipients
        - github-webhook
        triggers:
        - on-sync-succeeded
  template.app-sync-succeeded: |
    webhook:
      github-webhook:
        method: POST
        path: /repos/{{call .repo.FullNameByRepoURL .app.spec.source.repoURL}}/statuses/{{.app.status.operationState.operation.sync.revision}}
        body: |
          {
            {{if eq .app.status.operationState.phase "Running"}} "state": "pending"{{end}}
            {{if eq .app.status.operationState.phase "Succeeded"}} "state": "success"{{end}}
            {{if eq .app.status.operationState.phase "Error"}} "state": "error"{{end}}
            {{if eq .app.status.operationState.phase "Failed"}} "state": "error"{{end}},
            "description": "ArgoCD",
            "target_url": "{{.context.argocdUrl}}/applications/{{.app.metadata.name}}",
            "context": "continuous-delivery/{{.app.metadata.name}}"
          }
  trigger.on-sync-succeeded: |
    - description: Application syncing has succeeded
      send:
      - app-sync-succeeded
      when: app.status.operationState.phase in ['Succeeded']
```
  * List of Argo CD applications is listed below
  * Enter application you want to subscribe:
  * Enter webhook name:
