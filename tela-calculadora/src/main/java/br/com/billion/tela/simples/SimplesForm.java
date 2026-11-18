package br.com.billion.tela.simples;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.billion.tela.cliente.Cliente;

public class SimplesForm {

    private String clienteId;
    private String razaoSocial;
    private String cnpj;
    private String regime = "SIMPLES NACIONAL";
    private String uf = "PR";
    private Long municipio;
    private LocalDate data = LocalDate.of(2027, 1, 15);
    private String tpDoc = "55";
    private String crt = "1";
    private String atividade;
    private String cClassTrib = "000001";
    private BigDecimal baseCalculo;
    private BigDecimal valorImpostoSeletivo = BigDecimal.ZERO;
    private BigDecimal aliquotaIbs;
    private BigDecimal aliquotaCbs;

    public static SimplesForm deCliente(Cliente cliente) {
        SimplesForm form = new SimplesForm();
        form.clienteId = cliente.getId();
        form.razaoSocial = cliente.getRazaoSocial();
        form.cnpj = cliente.getCnpj();
        form.regime = cliente.getRegime();
        form.uf = cliente.getUf();
        form.municipio = cliente.getMunicipio();
        return form;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Long getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Long municipio) {
        this.municipio = municipio;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getTpDoc() {
        return tpDoc;
    }

    public void setTpDoc(String tpDoc) {
        this.tpDoc = tpDoc;
    }

    public String getCrt() {
        return crt;
    }

    public void setCrt(String crt) {
        this.crt = crt;
    }

    public String getAtividade() {
        return atividade;
    }

    public void setAtividade(String atividade) {
        this.atividade = atividade;
    }

    public String getCClassTrib() {
        return cClassTrib;
    }

    public void setCClassTrib(String cClassTrib) {
        this.cClassTrib = cClassTrib;
    }

    public BigDecimal getBaseCalculo() {
        return baseCalculo;
    }

    public void setBaseCalculo(BigDecimal baseCalculo) {
        this.baseCalculo = baseCalculo;
    }

    public BigDecimal getValorImpostoSeletivo() {
        return valorImpostoSeletivo;
    }

    public void setValorImpostoSeletivo(BigDecimal valorImpostoSeletivo) {
        this.valorImpostoSeletivo = valorImpostoSeletivo;
    }

    public BigDecimal getAliquotaIbs() {
        return aliquotaIbs;
    }

    public void setAliquotaIbs(BigDecimal aliquotaIbs) {
        this.aliquotaIbs = aliquotaIbs;
    }

    public BigDecimal getAliquotaCbs() {
        return aliquotaCbs;
    }

    public void setAliquotaCbs(BigDecimal aliquotaCbs) {
        this.aliquotaCbs = aliquotaCbs;
    }
}
