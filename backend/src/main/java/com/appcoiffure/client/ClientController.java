package com.appcoiffure.client;

import java.util.List;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CurrentCoiffeuseService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final CurrentCoiffeuseService currentCoiffeuseService;

    public ClientController(ClientRepository clientRepository, CurrentCoiffeuseService currentCoiffeuseService) {
        this.clientRepository = clientRepository;
        this.currentCoiffeuseService = currentCoiffeuseService;
    }

    @GetMapping
    public List<ClientResponse> list(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "") String recherche
    ) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);
        String query = recherche.trim();
        List<Client> clients = query.isBlank()
                ? clientRepository.findTop50ByCoiffeuseIdOrderByNomAsc(coiffeuse.getId())
                : clientRepository.findTop50ByCoiffeuseIdAndNomContainingIgnoreCaseOrCoiffeuseIdAndTelephoneContainingIgnoreCaseOrCoiffeuseIdAndEmailContainingIgnoreCaseOrderByNomAsc(
                        coiffeuse.getId(),
                        query,
                        coiffeuse.getId(),
                        query,
                        coiffeuse.getId(),
                        query
                );

        return clients.stream()
                .map(ClientResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(HttpServletRequest httpRequest, @RequestBody ClientRequest request) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(httpRequest);

        if (isBlank(request.nom())) {
            return ResponseEntity.badRequest().build();
        }

        Client client = new Client(
                coiffeuse,
                cleanRequired(request.nom()),
                cleanOptional(request.telephone()),
                cleanOptional(request.email()),
                cleanInstagram(request.instagram()),
                cleanOptional(request.notes()),
                request.smsActif()
        );

        Client savedClient = clientRepository.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientResponse.from(savedClient));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ClientResponse> update(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody ClientRequest request
    ) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(httpRequest);

        if (isBlank(request.nom())) {
            return ResponseEntity.badRequest().build();
        }

        Client client = clientRepository.findByIdAndCoiffeuseId(id, coiffeuse.getId()).orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        client.modifier(
                cleanRequired(request.nom()),
                cleanOptional(request.telephone()),
                cleanOptional(request.email()),
                cleanInstagram(request.instagram()),
                cleanOptional(request.notes()),
                request.smsActif()
        );

        return ResponseEntity.ok(ClientResponse.from(client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);

        if (!clientRepository.existsByIdAndCoiffeuseId(id, coiffeuse.getId())) {
            return ResponseEntity.notFound().build();
        }

        clientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String cleanRequired(String value) {
        return value.trim();
    }

    private String cleanOptional(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String cleanInstagram(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        String cleaned = value.trim();

        if (cleaned.startsWith("@")) {
            cleaned = cleaned.substring(1);
        }

        if (cleaned.startsWith("https://instagram.com/")) {
            cleaned = cleaned.substring("https://instagram.com/".length());
        }

        if (cleaned.startsWith("https://www.instagram.com/")) {
            cleaned = cleaned.substring("https://www.instagram.com/".length());
        }

        int slashIndex = cleaned.indexOf('/');
        if (slashIndex >= 0) {
            cleaned = cleaned.substring(0, slashIndex);
        }

        return cleaned.isBlank() ? null : cleaned;
    }
}
