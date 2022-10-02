### Prerequisites:
* Cluster
* ArgoCD installed in the <code>argocd</code> namespace of the cluster

* To enable applying configMap manifest files to your cluster, do the following:

    1. Create a Cluster role with the resource you need (configmaps in this example)

    ```
    kubectl create clusterrole deployer --verb=get,list,watch,create,delete,patch,update --resource=configmaps
    ```
    2. Bind it to your service account
    ```
  	kubectl create clusterrolebinding deployer-srvacct-default-binding --clusterrole=deployer --serviceaccount=default:default
    ```

* Set environment variable<code>KUBE_API</code>

    - After running 
      ```
      kubectl cluster-info
      ```
      you will get the following:
      ```
      Kubernetes control plane is running at: xxxxxxxx.
      ```
* Set environment variable <code>TOKEN</code> - Service Account Token
    - To get the JWT, run the following command: 
    ```
    kubectl get secrets \
      $(kubectl get serviceaccounts/default -o jsonpath='{.secrets[0].name}') \
      -o jsonpath='{.data.token}' | base64 --decode
    ```
<i> Note: For Kubernetes >= 1.24, tokens are no longer auto-generated for every ServiceAccount. You should create them manually.</i>

* Set environment variables <code>ARGO_URL</code>, <code>ARGO_PASSWORD</code>
* Set environment variable <code>ARGO_USERNAME</code> or use default 'admin'
* All environment variables can be overridden inside the application

* Enter path name to <code>argocd-notifications-cm</code> yaml file to apply to the cluster or skip it if you already have one
  
    Example of the <code>argocd-notifications-cm</code> yaml:

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
* List of Argo CD applications is shown below
* Enter application you want to subscribe
* Enter service name, value and triggers you want to apply

### Build and run
* Build the application: <code>./gradlew clean build</code>
* Run the application: 
  <code>KUBE_API=$KUBE_API KUBE_TOKEN=$KUBE_TOKEN ARGO_URL=$ARGO_URL ARGO_PASSWORD=$ARGO_PASSWORD ./gradlew run 
  --console=plain</code>
