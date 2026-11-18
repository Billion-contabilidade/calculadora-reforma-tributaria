package br.com.billion.tela.cliente;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Component
public class ClienteStore {

    private final ObjectMapper mapper;
    private final Path arquivo;
    private final List<Cliente> clientes = new ArrayList<>();

    public ClienteStore(ObjectMapper mapper, @Value("${clientes.arquivo}") String arquivo) {
        this.mapper = mapper;
        this.arquivo = Path.of(arquivo);
    }

    @PostConstruct
    void carregar() throws IOException {
        if (!Files.exists(arquivo)) {
            return;
        }
        List<Cliente> lidos = mapper.readValue(arquivo.toFile(), new TypeReference<List<Cliente>>() {
        });
        clientes.addAll(lidos);
        clientes.sort(Comparator.comparing(Cliente::getRazaoSocial, String.CASE_INSENSITIVE_ORDER));
    }

    public synchronized List<Cliente> listar() {
        return List.copyOf(clientes);
    }

    public synchronized Optional<Cliente> buscar(String id) {
        return clientes.stream().filter(cliente -> cliente.getId().equals(id)).findFirst();
    }

    public synchronized Cliente salvar(Cliente cliente) throws IOException {
        if (cliente.getId() == null || cliente.getId().isBlank()) {
            cliente.setId(UUID.randomUUID().toString());
            clientes.add(cliente);
        } else {
            clientes.removeIf(atual -> atual.getId().equals(cliente.getId()));
            clientes.add(cliente);
        }
        clientes.sort(Comparator.comparing(Cliente::getRazaoSocial, String.CASE_INSENSITIVE_ORDER));
        gravar();
        return cliente;
    }

    public synchronized void excluir(String id) throws IOException {
        clientes.removeIf(cliente -> cliente.getId().equals(id));
        gravar();
    }

    private void gravar() throws IOException {
        Path pasta = arquivo.getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(arquivo.toFile(), clientes);
    }
}
