package com.ag04.notifications;

import com.ag04.notifications.kubernetes.service.ArgoCDService;
import com.ag04.notifications.kubernetes.service.KubernetesService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
        while (true) {
            Scanner scanner = new Scanner(System.in);
            scanner.useDelimiter("\\n");

            System.out.println("Enter argocd-notifications-cm yaml file to apply to the cluster: ");
            System.out.println(">> ");

            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sb.append(line).append("\n");
                if (line.isEmpty()) {
                    break;
                }
            }
            String result = kubernetesService.applyYaml(sb.toString());

            System.out.println("Result: " + result);

            List<String> appNames = argoCDService.listApplications();
            System.out.println("Applications to subscribe: ");
            if (!appNames.isEmpty()) {
                for (String app : appNames) {
                    System.out.println("- " + app);
                }
                System.out.println("Enter application you want to subscribe: ");
                System.out.print(">> ");
                String application;
                while (true) {
                    application = scanner.nextLine();
                    if (appNames.contains(application.trim())) {
                        break;
                    }
                    System.out.println("Application not recognized! Try again: ");
                }
                System.out.println("Enter webhook name: ");
                System.out.print(">> ");
                String webhook = scanner.nextLine();
                String subscriptionResult = argoCDService.subscribeApplication(application, webhook);
                System.out.println(subscriptionResult);
            } else {
                System.out.println("No applications to subscribe!");
            }
            System.exit(0);
        }
    }
}
