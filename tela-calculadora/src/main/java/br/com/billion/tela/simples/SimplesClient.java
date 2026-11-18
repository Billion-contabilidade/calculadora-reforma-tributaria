package br.com.billion.tela.simples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.billion.tela.calculo.CalculadoraIndisponivelException;

@Component
public class SimplesClient {

    private final RestClient http;
    private final ObjectMapper mapper;

    public SimplesClient(ObjectMapper mapper, @Value("${calculadora.simples-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
        this.mapper = mapper;
    }

    public List<Atividade> atividades(String data, String tpDoc, String cClassTrib) {
        try {
            String json = http.get()
                    .uri(uri -> uri.path("/calculadora/simples-nacional/atividades")
                            .queryParam("data", data)
                            .queryParam("tpDoc", tpDoc)
                            .queryParamIfPresent("cClassTrib", texto(cClassTrib))
                            .build())
                    .retrieve()
                    .body(String.class);
            return lerAtividades(json);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 400 && temTexto(cClassTrib)) {
                return atividades(data, tpDoc, null);
            }
            throw new CalculadoraIndisponivelException(mensagem(ex));
        } catch (RestClientException ex) {
            throw new CalculadoraIndisponivelException(
                    "A calculadora do Simples Nacional não respondeu em http://localhost:8082. Deixe essa API rodando e tente de novo.");
        }
    }

    public ResultadoSimples calcular(SimplesForm form) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("numero", 1);
        item.put("cAtivSN", normalizarAtividade(form.getAtividade()));
        item.put("cClassTrib", form.getCClassTrib().trim());
        item.put("baseCalculo", dinheiro(form.getBaseCalculo()));
        item.put("valorImpostoSeletivo", dinheiro(form.getValorImpostoSeletivo()));
        if ("1".equals(form.getCrt())) {
            item.put("pIBSSN", aliquota(form.getAliquotaIbs()));
        }
        item.put("pCBSSN", aliquota(form.getAliquotaCbs()));

        String cnpj = form.getCnpj() == null ? "avulso" : form.getCnpj().replaceAll("\\D", "");
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("id", "portal-sn-" + cnpj + "-" + form.getData());
        corpo.put("versao", "2.0.0");
        corpo.put("data", form.getData().toString());
        corpo.put("tpDoc", form.getTpDoc());
        corpo.put("CRT", form.getCrt());
        corpo.put("itens", List.of(item));

        try {
            String json = http.post()
                    .uri("/calculadora/observabilidade/simples-nacional")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(String.class);
            return lerResultado(json, form);
        } catch (RestClientResponseException ex) {
            throw new CalculadoraIndisponivelException(mensagem(ex));
        } catch (RestClientException ex) {
            throw new CalculadoraIndisponivelException(
                    "A calculadora do Simples Nacional não respondeu em http://localhost:8082. Deixe essa API rodando e tente de novo.");
        }
    }

    private List<Atividade> lerAtividades(String json) {
        try {
            List<Atividade> atividades = new ArrayList<>();
            for (JsonNode node : mapper.readTree(json)) {
                atividades.add(new Atividade(
                        node.path("codigo").asText(),
                        node.path("descricao").asText(),
                        node.path("rotuloCurtoAnexo").asText(""),
                        node.path("disponivel").asBoolean(true)));
            }
            return atividades;
        } catch (Exception ex) {
            throw new CalculadoraIndisponivelException("A lista de atividades do Simples veio num formato inesperado.");
        }
    }

    private ResultadoSimples lerResultado(String json, SimplesForm form) {
        try {
            JsonNode raiz = mapper.readTree(json);
            JsonNode objeto = raiz.path("objetos").path(0);
            JsonNode trib = objeto.path("tribCalc");
            JsonNode total = raiz.path("total");
            String estado = objeto.path("estadoItem").asText("");

            ResultadoSimples resultado = new ResultadoSimples();
            resultado.setCliente(form.getRazaoSocial());
            resultado.setDocumento("55".equals(form.getTpDoc()) ? "NF-e" : "NFC-e");
            resultado.setEstado(estado);
            resultado.setCalculado("CALCULADO".equals(estado));
            resultado.setReceitaBruta(decimal(trib.path("vRBSNitem")));
            resultado.setTipoReceita(trib.path("tpRBSN").asText("") + " " + trib.path("tpRBSNDescricao").asText(""));
            resultado.setBase(decimal(trib.path("vBC")));
            resultado.setAliquotaIbs(decimal(trib.path("pIBSSN")));
            resultado.setValorIbs(decimal(trib.path("vIBSSN")));
            resultado.setAliquotaCbs(decimal(trib.path("pCBSSN")));
            resultado.setValorCbs(decimal(trib.path("vCBSSN")));
            resultado.setValorIs(decimal(trib.path("IS").path("valor")));
            BigDecimal totalTributos = decimal(total.path("totalTributos"));
            if (total.path("totalTributos").isMissingNode() || total.path("totalTributos").isNull()) {
                totalTributos = resultado.getValorIbs().add(resultado.getValorCbs()).add(resultado.getValorIs());
            }
            resultado.setTotal(totalTributos);
            for (JsonNode erro : objeto.path("erros")) {
                String codigo = erro.path("code").asText("");
                String detalhe = erro.path("detail").asText(erro.path("title").asText(""));
                resultado.getMensagens().add((codigo.isBlank() ? "" : codigo + " ") + detalhe);
            }
            if ("NAO_CALCULADO".equals(estado)) {
                resultado.getMensagens().add(
                        "A atividade não foi calculada. Ela pode estar ausente, fora de vigência ou incompatível com este documento.");
            }
            resultado.setJson(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(raiz));
            return resultado;
        } catch (Exception ex) {
            throw new CalculadoraIndisponivelException("A calculadora do Simples respondeu num formato que a tela não reconheceu.");
        }
    }

    private String mensagem(RestClientResponseException ex) {
        try {
            JsonNode corpo = mapper.readTree(ex.getResponseBodyAsString());
            String detalhe = corpo.path("detail").asText("");
            String titulo = corpo.path("title").asText("");
            if (!titulo.isBlank() && !detalhe.isBlank()) {
                return titulo + ". " + detalhe;
            }
            if (!detalhe.isBlank()) {
                return detalhe;
            }
        } catch (Exception ignored) {
            // cai na mensagem genérica
        }
        return "A calculadora do Simples recusou a conta (HTTP " + ex.getStatusCode().value() + ").";
    }

    private static BigDecimal decimal(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.asText().isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(node.asText("0"));
    }

    private static String dinheiro(BigDecimal valor) {
        return String.format(Locale.US, "%.2f", valor == null ? BigDecimal.ZERO : valor);
    }

    private static String aliquota(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private static String normalizarAtividade(String atividade) {
        String codigo = atividade.trim();
        if (codigo.length() == 1 && Character.isDigit(codigo.charAt(0))) {
            return "0" + codigo;
        }
        return codigo;
    }

    private static java.util.Optional<String> texto(String valor) {
        return temTexto(valor) ? java.util.Optional.of(valor.trim()) : java.util.Optional.empty();
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    public record Atividade(String codigo, String descricao, String anexo, boolean disponivel) {
    }
}
