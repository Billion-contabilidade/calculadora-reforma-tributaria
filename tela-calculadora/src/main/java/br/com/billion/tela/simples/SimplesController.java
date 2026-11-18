package br.com.billion.tela.simples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.billion.tela.calculo.CalculadoraClient;
import br.com.billion.tela.calculo.CalculadoraClient.MunicipioOption;
import br.com.billion.tela.calculo.CalculadoraClient.UfOption;
import br.com.billion.tela.calculo.CalculadoraIndisponivelException;
import br.com.billion.tela.cliente.Cliente;
import br.com.billion.tela.cliente.ClienteStore;
import br.com.billion.tela.simples.SimplesClient.Atividade;

@Controller
public class SimplesController {

    private final ClienteStore clientes;
    private final CalculadoraClient cadastro;
    private final SimplesClient simples;

    public SimplesController(ClienteStore clientes, CalculadoraClient cadastro, SimplesClient simples) {
        this.clientes = clientes;
        this.cadastro = cadastro;
        this.simples = simples;
    }

    @GetMapping("/simples")
    public String inicio(@RequestParam(required = false) String cliente, Model model) {
        SimplesForm form = new SimplesForm();
        if (cliente != null) {
            clientes.buscar(cliente).ifPresent(salvo -> copiar(SimplesForm.deCliente(salvo), form));
        }
        preparar(model, form, null, null);
        return "simples";
    }

    @GetMapping("/simples/atividades")
    @ResponseBody
    public List<Atividade> atividades(@RequestParam String data, @RequestParam String tpDoc,
            @RequestParam(required = false) String cClassTrib) {
        try {
            return simples.atividades(data, tpDoc, cClassTrib);
        } catch (CalculadoraIndisponivelException ex) {
            return List.of();
        }
    }

    @PostMapping("/simples/clientes")
    public String guardar(SimplesForm form, Model model) {
        List<String> erros = validarCliente(form);
        if (!erros.isEmpty()) {
            preparar(model, form, erros, null);
            return "simples";
        }
        try {
            Cliente salvo = clientes.salvar(paraCliente(form));
            form.setClienteId(salvo.getId());
            preparar(model, form, null, null);
            model.addAttribute("aviso", "Cliente guardado.");
            return "simples";
        } catch (Exception ex) {
            preparar(model, form, List.of("Não foi possível gravar o cliente neste computador."), null);
            return "simples";
        }
    }

    @PostMapping("/simples/clientes/{id}/excluir")
    public String excluir(@PathVariable String id) {
        try {
            clientes.excluir(id);
        } catch (Exception ignored) {
            // a lista volta a aparecer no próximo carregamento
        }
        return "redirect:/simples";
    }

    @PostMapping("/simples/calcular")
    public String calcular(SimplesForm form, Model model) {
        List<String> erros = validarCalculo(form);
        if (!erros.isEmpty()) {
            preparar(model, form, erros, null);
            return "simples";
        }
        try {
            ResultadoSimples resultado = simples.calcular(form);
            List<String> mensagens = new ArrayList<>(resultado.getMensagens());
            preparar(model, form, mensagens, resultado);
            return "simples";
        } catch (CalculadoraIndisponivelException ex) {
            preparar(model, form, List.of(ex.getMessage()), null);
            return "simples";
        }
    }

    private void preparar(Model model, SimplesForm form, List<String> erros, ResultadoSimples resultado) {
        List<UfOption> ufs = List.of();
        List<MunicipioOption> municipios = List.of();
        List<Atividade> atividades = List.of();
        String falhaApi = null;
        try {
            ufs = cadastro.ufs().stream().sorted(Comparator.comparing(UfOption::sigla)).toList();
            municipios = cadastro.municipios(form.getUf()).stream()
                    .sorted(Comparator.comparing(MunicipioOption::nome, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (CalculadoraIndisponivelException ex) {
            falhaApi = ex.getMessage();
        }
        if (form.getData() != null) {
            try {
                atividades = simples.atividades(form.getData().toString(), form.getTpDoc(), form.getCClassTrib());
            } catch (CalculadoraIndisponivelException ex) {
                falhaApi = ex.getMessage();
            }
        }
        model.addAttribute("form", form);
        model.addAttribute("clientes", clientes.listar());
        model.addAttribute("ufs", ufs);
        model.addAttribute("municipios", municipios);
        model.addAttribute("atividades", atividades);
        model.addAttribute("erros", erros == null ? List.of() : erros);
        model.addAttribute("falhaApi", falhaApi);
        model.addAttribute("resultado", resultado);
    }

    private List<String> validarCliente(SimplesForm form) {
        List<String> erros = new ArrayList<>();
        if (!temTexto(form.getRazaoSocial())) {
            erros.add("Informe a razão social.");
        }
        if (!temTexto(form.getCnpj())) {
            erros.add("Informe o CNPJ.");
        }
        if (!temTexto(form.getUf()) || form.getMunicipio() == null) {
            erros.add("Informe a UF e o município do cliente.");
        }
        return erros;
    }

    private List<String> validarCalculo(SimplesForm form) {
        List<String> erros = validarCliente(form);
        if (!"SIMPLES NACIONAL".equals(form.getRegime())) {
            erros.add("Esta tela calcula só o Simples Nacional. Lucro Presumido e Lucro Real ficam na outra calculadora.");
        }
        if (form.getData() == null) {
            erros.add("Informe a data do fato gerador.");
        }
        if (!temTexto(form.getAtividade())) {
            erros.add("Informe a atividade do Simples Nacional.");
        }
        if (!temTexto(form.getCClassTrib())) {
            erros.add("Informe a classificação tributária.");
        }
        if (form.getBaseCalculo() == null || form.getBaseCalculo().signum() <= 0) {
            erros.add("Informe a base de cálculo maior que zero.");
        }
        if (form.getAliquotaCbs() == null) {
            erros.add("Informe a alíquota efetiva de CBS do Simples.");
        }
        if ("1".equals(form.getCrt()) && form.getAliquotaIbs() == null) {
            erros.add("Informe a alíquota efetiva de IBS do Simples.");
        }
        if (form.getValorImpostoSeletivo() == null) {
            form.setValorImpostoSeletivo(BigDecimal.ZERO);
        }
        return erros;
    }

    private Cliente paraCliente(SimplesForm form) {
        Cliente cliente = new Cliente();
        cliente.setId(form.getClienteId());
        cliente.setRazaoSocial(form.getRazaoSocial().trim());
        cliente.setCnpj(form.getCnpj().trim());
        cliente.setRegime("SIMPLES NACIONAL");
        cliente.setUf(form.getUf());
        cliente.setMunicipio(form.getMunicipio());
        cliente.setMunicipioNome(nomeMunicipio(form));
        return cliente;
    }

    private void copiar(SimplesForm origem, SimplesForm destino) {
        destino.setClienteId(origem.getClienteId());
        destino.setRazaoSocial(origem.getRazaoSocial());
        destino.setCnpj(origem.getCnpj());
        destino.setRegime(origem.getRegime());
        destino.setUf(origem.getUf());
        destino.setMunicipio(origem.getMunicipio());
    }

    private String nomeMunicipio(SimplesForm form) {
        try {
            return cadastro.municipios(form.getUf()).stream()
                    .filter(municipio -> municipio.codigo().equals(form.getMunicipio()))
                    .map(MunicipioOption::nome)
                    .findFirst()
                    .orElse("");
        } catch (CalculadoraIndisponivelException ex) {
            return "";
        }
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
