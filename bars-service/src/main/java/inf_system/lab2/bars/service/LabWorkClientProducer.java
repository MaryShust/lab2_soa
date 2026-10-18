package inf_system.lab2.bars.service;

import inf_system.lab2.bars.client.LabWorkClient;
import inf_system.lab2.bars.config.ServiceConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@ApplicationScoped
public class LabWorkClientProducer {

    private volatile LabWorkClient client;

    @Produces
    @Singleton
    public LabWorkClient client() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new LabWorkClient(ServiceConfig.labWorkBaseUrl());
                }
            }
        }
        return client;
    }
}
