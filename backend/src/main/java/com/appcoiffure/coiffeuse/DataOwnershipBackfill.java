package com.appcoiffure.coiffeuse;

import java.util.List;

import com.appcoiffure.client.Client;
import com.appcoiffure.client.ClientRepository;
import com.appcoiffure.finance.Depense;
import com.appcoiffure.finance.DepenseRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataOwnershipBackfill implements CommandLineRunner {

    private final CoiffeuseRepository coiffeuseRepository;
    private final ClientRepository clientRepository;
    private final DepenseRepository depenseRepository;

    public DataOwnershipBackfill(
            CoiffeuseRepository coiffeuseRepository,
            ClientRepository clientRepository,
            DepenseRepository depenseRepository
    ) {
        this.coiffeuseRepository = coiffeuseRepository;
        this.clientRepository = clientRepository;
        this.depenseRepository = depenseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Coiffeuse owner = coiffeuseRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (owner == null) {
            return;
        }

        List<Client> orphanClients = clientRepository.findByCoiffeuseIsNull();
        for (Client client : orphanClients) {
            client.attribuerCoiffeuse(owner);
        }

        List<Depense> orphanExpenses = depenseRepository.findByCoiffeuseIsNull();
        for (Depense depense : orphanExpenses) {
            depense.attribuerCoiffeuse(owner);
        }
    }
}
