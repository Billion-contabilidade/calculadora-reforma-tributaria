package br.com.billion.tela.web;

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

import br.com.billion.tela.calculo.CalculoForm;
import br.com.billion.tela.calculo.CalculadoraClient;
import br.com.billion.tela.calculo.CalculadoraClient.MunicipioOption;
import br.com.billion.tela.calculo.CalculadoraClient.UfOption;
import br.com.billion.tela.calculo.CalculadoraIndisponivelException;
import br.com.billion.tela.calculo.ResultadoConta;
import br.com.billion.tela.cliente.Cliente;
import br.com.billion.tela.cliente.ClienteStore;

@Controller
public class TelaController {

    private final ClienteStore clientes;
    private final CalculadoraClient calculadora;

    public TelaController(ClienteStore clientes, CalculadoraClient calculadora) {
        this.clientes = clientes;
        this.calculadora = calculadora;
    }

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String cliente, Model model) {
        CalculoForm form = new CalculoForm();
        if (cliente != null) {
            clientes.buscar(cliente).ifPresent(salvo -> copiarCliente(CalculoForm.deCliente(salvo), form));
        }
        preparar(model, form, null, null);
        return "index";
    }

    @GetMapping("/municipios")
    @ResponseBody
    public List<MunicipioOption> municipios(@RequestParam String uf) {
        try {
            return calculadora.municipios(uf).stream()
                    .sorted(Comparator.comparing(MunicipioOption::nome, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (CalculadoraIndisponivelException ex) {
            return List.of();
        }
    }

    @PostMapping("/clientes")
    public String guardar(CalculoForm form, Model model) {
        List<String> erros = validarCliente(form);
        if (!erros.isEmpty()) {
            preparar(model, form, erros, null);
            return "index";
        }
        try {
            Cliente salvo = clientes.salvar(paraCliente(form, nomeMunicipio(form)));
            form.setClienteId(salvo.getId());
            preparar(model, form, null, null);
            model.addAttribute("aviso", "Cliente guardado.");
            return "index";
        } catch (Exception ex) {
            preparar(model, form, List.of("Não foi possível gravar o cliente neste computador."), null);
            return "index";
        }
    }

    @PostMapping("/clientes/{id}/excluir")
    public String excluir(@PathVariable String id) {
        try {
            clientes.excluir(id);
        } catch (Exception ignored) {
            // a lista volta a aparecer no próximo carregamento
        }
        return "redirect:/";
    }

    @PostMapping("/calcular")
    public String calcular(CalculoForm form, Model model) {
        List<String> erros = validarCalculo(form);
        if (!erros.isEmpty()) {
            preparar(model, form, erros, null);
            return "index";
        }
        try {
            String municipio = nomeMunicipio(form);
            ResultadoConta resultado = calculadora.calcular(form, municipio);
            preparar(model, form, null, resultado);
            return "index";
        } catch (CalculadoraIndisponivelException ex) {
            preparar(model, form, List.of(ex.getMessage()), null);
            return "index";
        }
    }

    private void preparar(Model model, CalculoForm form, List<String> erros, ResultadoConta resultado) {
        List<UfOption> ufs = List.of();
        List<MunicipioOption> municipios = List.of();
        String falhaApi = null;
        try {
            ufs = calculadora.ufs().stream()
                    .sorted(Comparator.comparing(UfOption::sigla))
                    .toList();
            municipios = calculadora.municipios(form.getUf()).stream()
                    .sorted(Comparator.comparing(MunicipioOption::nome, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (CalculadoraIndisponivelException ex) {
            falhaApi = ex.getMessage();
        }
        model.addAttribute("form", form);
        model.addAttribute("clientes", clientes.listar());
        model.addAttribute("ufs", ufs);
        model.addAttribute("municipios", municipios);
        model.addAttribute("erros", erros == null ? List.of() : erros);
        model.addAttribute("falhaApi", falhaApi);
        model.addAttribute("resultado", resultado);
    }

    private List<String> validarCliente(CalculoForm form) {
        List<String> erros = new ArrayList<>();
        if (!temTexto(form.getRazaoSocial())) {
            erros.add("Informe a razão social.");
        }
        if (!temTexto(form.getCnpj())) {
            erros.add("Informe o CNPJ.");
        }
        if (!temTexto(form.getRegime())) {
            erros.add("Informe o regime.");
        }
        if (!temTexto(form.getUf()) || form.getMunicipio() == null) {
            erros.add("Informe a UF e o município do cliente.");
        }
        return erros;
    }

    private List<String> validarCalculo(CalculoForm form) {
        List<String> erros = validarCliente(form);
        if ("SIMPLES NACIONAL".equals(form.getRegime())) {
            erros.add("Este cliente está no Simples Nacional. Esta tela calcula o regime regular, Lucro Presumido e Lucro Real.");
        }
        if (form.getData() == null) {
            erros.add("Informe a data do fato gerador.");
        }
        if (form.getBaseCalculo() == null || form.getBaseCalculo().signum() <= 0) {
            erros.add("Informe a base de cálculo maior que zero.");
        }
        boolean nbs = temTexto(form.getNbs());
        boolean ncm = temTexto(form.getNcm());
        if (nbs == ncm) {
            erros.add("Informe o NBS do serviço ou o NCM da mercadoria, um dos dois.");
        }
        if (!temTexto(form.getCst()) || !temTexto(form.getCClassTrib())) {
            erros.add("Informe o CST e a classificação tributária.");
        }
        if (form.exigeAliquotasNominais()
                && (form.getAliquotaCbs() == null || form.getAliquotaIbsUf() == null || form.getAliquotaIbsMun() == null)) {
            erros.add("A partir de 2027 informe as alíquotas de CBS, IBS estadual e IBS municipal.");
        }
        if (form.getQuantidade() == null || form.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            erros.add("Informe a quantidade maior que zero.");
        }
        if (!temTexto(form.getUnidade())) {
            erros.add("Informe a unidade.");
        }
        return erros;
    }

    private Cliente paraCliente(CalculoForm form, String municipioNome) {
        Cliente cliente = new Cliente();
        cliente.setId(form.getClienteId());
        cliente.setRazaoSocial(form.getRazaoSocial().trim());
        cliente.setCnpj(form.getCnpj().trim());
        cliente.setRegime(form.getRegime());
        cliente.setUf(form.getUf());
        cliente.setMunicipio(form.getMunicipio());
        cliente.setMunicipioNome(municipioNome);
        return cliente;
    }

    private void copiarCliente(CalculoForm origem, CalculoForm destino) {
        destino.setClienteId(origem.getClienteId());
        destino.setRazaoSocial(origem.getRazaoSocial());
        destino.setCnpj(origem.getCnpj());
        destino.setRegime(origem.getRegime());
        destino.setUf(origem.getUf());
        destino.setMunicipio(origem.getMunicipio());
    }

    private String nomeMunicipio(CalculoForm form) {
        try {
            return calculadora.municipios(form.getUf()).stream()
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
