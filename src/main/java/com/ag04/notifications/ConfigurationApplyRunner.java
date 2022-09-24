package com.ag04.notifications;

import com.ag04.notifications.kubernetes.EnvironmentVariable;
import com.ag04.notifications.kubernetes.SubscriptionTrigger;
import com.ag04.notifications.kubernetes.service.ArgoCDService;
import com.ag04.notifications.kubernetes.service.KubernetesService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ConfigurationApplyRunner implements CommandLineRunner {

    KubernetesService kubernetesService;
    ArgoCDService argoCDService;

    public ConfigurationApplyRunner(KubernetesService kubernetesService, ArgoCDService argoCDService) {
        this.kubernetesService = kubernetesService;
        this.argoCDService = argoCDService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter(System.lineSeparator());

        do {
            overrideEnvironmentVariables();
            System.out.println("---------------------------------");
            System.out.print("Do you want to apply argocd-notifications-cm ConfigMap? [Y/N] >> ");
            if (scanner.nextLine().equalsIgnoreCase("Y")) {
                applyNotificationsConfigMap();
            }
            System.out.println("---------------------------------");
            generateApplicationOptions();
            System.out.print("Do you want to repeat the process? [Y/N]? >> ");
        } while (scanner.nextLine().equalsIgnoreCase("Y"));
        System.exit(0);
    }

    private void applyNotificationsConfigMap() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter path to argocd-notifications-cm yaml file to apply to the cluster >> ");

        String yamlFilePath = scanner.nextLine();
        ResponseEntity result = kubernetesService.applyYaml(yamlFilePath);

        System.out.println(result.getStatusCode());
    }

    private void generateApplicationOptions() {
        Scanner scanner = new Scanner(System.in);

        List<String> appNames = argoCDService.listApplications();
        System.out.println("Applications to subscribe: ");
        if (!appNames.isEmpty()) {
            for (String app : appNames) {
                System.out.println("- " + app);
            }
            System.out.print("Enter application you want to subscribe >> ");
            String application;
            while (true) {
                application = scanner.nextLine();
                if (appNames.contains(application.trim())) {
                    break;
                }
                System.out.print("Application not recognized! Try again >> ");
            }
            handleApplicationSubscriptions(application);
        } else {
            System.out.println("No applications to subscribe!");
        }
    }

    private void handleApplicationSubscriptions(String application) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter service name >> ");
        String webhook = scanner.nextLine();
        System.out.print("Enter subscription value >> ");
        String subscriptionValue = scanner.nextLine();
        List<SubscriptionTrigger> triggers = new ArrayList<>();
        String answer;
        for (SubscriptionTrigger triggerOption : SubscriptionTrigger.class.getEnumConstants()) {
            System.out.print(String.format("Do you want to add '%s' trigger? [Y/N] >> ", triggerOption));
            answer = scanner.next();
            if (answer.equalsIgnoreCase("Y")) triggers.add(triggerOption);
        }
        ResponseEntity subscriptionResult = argoCDService.subscribeApplication(application, webhook, triggers, subscriptionValue);
        System.out.println(subscriptionResult.getStatusCode());
    }

    private void overrideEnvironmentVariables() {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\r?\\n");

        String input;
        System.out.print("Enter ArgoCD url (if you want to use default [" +
                argoCDService.getArgoProperty(EnvironmentVariable.ARGO_URL) + "], press Enter) >> ");
        if (!(input = scanner.nextLine()).isEmpty())
            argoCDService.updateArgoProperty(EnvironmentVariable.ARGO_URL, input);

        System.out.print("Enter ArgoCD username (if you want to use default [" +
                argoCDService.getArgoProperty(EnvironmentVariable.ARGO_USERNAME) + "], press Enter) >> ");
        if (!(input = scanner.nextLine()).isEmpty())
            argoCDService.updateArgoProperty(EnvironmentVariable.ARGO_USERNAME, input);

        System.out.print("Enter ArgoCD password (if you want to use default [" +
                argoCDService.getArgoProperty(EnvironmentVariable.ARGO_PASSWORD) + "], press Enter) >> ");
        if (!(input = scanner.nextLine()).isEmpty())
            argoCDService.updateArgoProperty(EnvironmentVariable.ARGO_PASSWORD, input);

        System.out.print("Enter Kubernetes API url (if you want to use default [" +
                kubernetesService.getK8sProperty(EnvironmentVariable.KUBE_URL) + "], press Enter) >> ");
        if (!(input = scanner.nextLine()).isEmpty())
            kubernetesService.updateK8sProperty(EnvironmentVariable.KUBE_URL, input);

        System.out.print("Enter Kubernetes token (if you want to use default [" +
                kubernetesService.getK8sProperty(EnvironmentVariable.KUBE_TOKEN) + "], press Enter) >> ");
        if (!(input = scanner.nextLine()).isEmpty())
            kubernetesService.updateK8sProperty(EnvironmentVariable.KUBE_TOKEN, input);
    }
}
