package br.com.billion.tela.calculo;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CalculadoraClient {

    private static final DateTimeFormatter FATO_GERADOR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private final RestClient http;
    private final ObjectMapper mapper;

    public CalculadoraClient(RestClient.Builder builder, ObjectMapper mapper,
            @Value("${calculadora.base-url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
        this.mapper = mapper;
    }

    public List<UfOption> ufs() {
        try {
            UfOption[] lista = http.get().uri("/dados-abertos/ufs").retrieve().body(UfOption[].class);
            return lista == null ? List.of() : List.of(lista);
        } catch (RestClientException ex) {
            throw new CalculadoraIndisponivelException(
                    "A calculadora não respondeu em http://localhost:8080. Deixe o regime regular rodando e tente de novo.");
        }
    }

    public List<MunicipioOption> municipios(String uf) {
        if (uf == null || uf.isBlank()) {
            return List.of();
        }
        try {
            MunicipioOption[] lista = http.get()
                    .uri(uri -> uri.path("/dados-abertos/ufs/municipios").queryParam("siglaUf", uf).build())
                    .retrieve()
                    .body(MunicipioOption[].class);
            return lista == null ? List.of() : List.of(lista);
        } catch (RestClientException ex) {
            throw new CalculadoraIndisponivelException(
                    "Não foi possível carregar os municípios de " + uf + ".");
        }
    }

    public ResultadoConta calcular(CalculoForm form, String municipioNome) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("numero", 1);
        if (temTexto(form.getNbs())) {
            item.put("nbs", form.getNbs().trim());
        }
        if (temTexto(form.getNcm())) {
            item.put("ncm", form.getNcm().trim());
        }
        item.put("cst", form.getCst().trim());
        item.put("cClassTrib", form.getCClassTrib().trim());
        item.put("baseCalculo", form.getBaseCalculo());
        item.put("quantidade", form.getQuantidade());
        item.put("unidade", form.getUnidade().trim());
        if (form.exigeAliquotasNominais()) {
            Map<String, Object> aliquotas = new LinkedHashMap<>();
            aliquotas.put("cbs", form.getAliquotaCbs());
            aliquotas.put("ibsEstadual", form.getAliquotaIbsUf());
            aliquotas.put("ibsMunicipal", form.getAliquotaIbsMun());
            item.put("aliquotasNominais", aliquotas);
        }

        String id = "portal-" + (form.getCnpj() == null ? "avulso" : form.getCnpj().replaceAll("\\D", ""))
                + "-" + form.getData();
        OffsetDateTime fato = OffsetDateTime.of(form.getData(), LocalTime.of(10, 0), ZoneOffset.ofHours(-3));
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("id", id);
        corpo.put("versao", "1.0.0");
        corpo.put("dhFatoGerador", fato.format(FATO_GERADOR));
        corpo.put("municipio", form.getMunicipio());
        corpo.put("uf", form.getUf());
        corpo.put("tpDoc", form.getTpDoc());
        corpo.put("itens", List.of(item));

        try {
            String json = http.post()
                    .uri("/regime-geral")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(String.class);
            return ler(json, form, municipioNome);
        } catch (RestClientResponseException ex) {
            throw new CalculadoraIndisponivelException(mensagemDeErro(ex));
        } catch (RestClientException ex) {
            throw new CalculadoraIndisponivelException(
                    "A calculadora não respondeu em http://localhost:8080. Deixe o regime regular rodando e tente de novo.");
        }
    }

    private ResultadoConta ler(String json, CalculoForm form, String municipioNome) {
        try {
            JsonNode raiz = mapper.readTree(json);
            JsonNode objeto = raiz.path("objetos").path(0);
            JsonNode grupo = objeto.path("tribCalc").path("IBSCBS").path("gIBSCBS");
            JsonNode uf = grupo.path("gIBSUF");
            JsonNode mun = grupo.path("gIBSMun");
            JsonNode cbs = grupo.path("gCBS");

            ResultadoConta resultado = new ResultadoConta();
            resultado.setCliente(form.getRazaoSocial());
            resultado.setDocumento(nomeDocumento(form.getTpDoc()));
            resultado.setMunicipio(municipioNome);
            resultado.setUf(form.getUf());
            resultado.setBase(decimal(grupo.path("vBC")));
            resultado.setAliquotaIbsUf(decimal(uf.path("pIBSUF")));
            resultado.setValorIbsUf(decimal(uf.path("vIBSUF")));
            resultado.setMemoriaIbsUf(uf.path("memoriaCalculo").asText(""));
            resultado.setAliquotaIbsMun(decimal(mun.path("pIBSMun")));
            resultado.setValorIbsMun(decimal(mun.path("vIBSMun")));
            resultado.setMemoriaIbsMun(mun.path("memoriaCalculo").asText(""));
            resultado.setValorIbs(decimal(grupo.path("vIBS")));
            resultado.setAliquotaCbs(decimal(cbs.path("pCBS")));
            resultado.setValorCbs(decimal(cbs.path("vCBS")));
            resultado.setMemoriaCbs(cbs.path("memoriaCalculo").asText(""));
            BigDecimal ibs = resultado.getValorIbs() == null ? BigDecimal.ZERO : resultado.getValorIbs();
            BigDecimal valorCbs = resultado.getValorCbs() == null ? BigDecimal.ZERO : resultado.getValorCbs();
            resultado.setTotal(ibs.add(valorCbs));
            resultado.setSimulado(objeto.path("calculoSimulado").asBoolean(false));
            resultado.setJson(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(raiz));
            return resultado;
        } catch (Exception ex) {
            throw new CalculadoraIndisponivelException("A calculadora respondeu num formato que a tela não reconheceu.");
        }
    }

    private String mensagemDeErro(RestClientResponseException ex) {
        try {
            JsonNode corpo = mapper.readTree(ex.getResponseBodyAsString());
            String titulo = corpo.path("title").asText("");
            String detalhe = corpo.path("detail").asText("");
            if (!titulo.isBlank() && !detalhe.isBlank()) {
                return titulo + ". " + detalhe;
            }
            if (!detalhe.isBlank()) {
                return detalhe;
            }
            if (!titulo.isBlank()) {
                return titulo;
            }
        } catch (Exception ignored) {
            // cai na mensagem genérica
        }
        return "A calculadora recusou a conta (HTTP " + ex.getStatusCode().value() + ").";
    }

    private static BigDecimal decimal(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(node.asText("0"));
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static String nomeDocumento(Integer tipo) {
        if (tipo == null) {
            return "Documento";
        }
        return switch (tipo) {
            case 55 -> "NF-e";
            case 65 -> "NFC-e";
            case 91 -> "NFS-e";
            default -> "Documento " + tipo;
        };
    }

    public record UfOption(String sigla, String nome, Long codigo) {
    }

    public record MunicipioOption(Long codigo, String nome) {
    }
}
