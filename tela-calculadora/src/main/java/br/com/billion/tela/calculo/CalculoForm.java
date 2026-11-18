package br.com.billion.tela.calculo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CalculoForm {

    private String clienteId;
    private String razaoSocial;
    private String cnpj;
    private String regime = "LUCRO PRESUMIDO";
    private String uf = "PR";
    private Long municipio;
    private LocalDate data = LocalDate.of(2026, 11, 18);
    private Integer tpDoc = 91;
    private String nbs;
    private String ncm;
    private String cst = "000";
    private String cClassTrib = "000001";
    private BigDecimal baseCalculo;
    private BigDecimal quantidade = BigDecimal.ONE;
    private String unidade = "UN";
    private BigDecimal aliquotaCbs;
    private BigDecimal aliquotaIbsUf;
    private BigDecimal aliquotaIbsMun;

    public static CalculoForm deCliente(br.com.billion.tela.cliente.Cliente cliente) {
        CalculoForm form = new CalculoForm();
        form.clienteId = cliente.getId();
        form.razaoSocial = cliente.getRazaoSocial();
        form.cnpj = cliente.getCnpj();
        form.regime = cliente.getRegime();
        form.uf = cliente.getUf();
        form.municipio = cliente.getMunicipio();
        return form;
    }

    public boolean exigeAliquotasNominais() {
        return data != null && data.getYear() >= 2027;
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

    public Integer getTpDoc() {
        return tpDoc;
    }

    public void setTpDoc(Integer tpDoc) {
        this.tpDoc = tpDoc;
    }

    public String getNbs() {
        return nbs;
    }

    public void setNbs(String nbs) {
        this.nbs = nbs;
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public String getCst() {
        return cst;
    }

    public void setCst(String cst) {
        this.cst = cst;
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

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getAliquotaCbs() {
        return aliquotaCbs;
    }

    public void setAliquotaCbs(BigDecimal aliquotaCbs) {
        this.aliquotaCbs = aliquotaCbs;
    }

    public BigDecimal getAliquotaIbsUf() {
        return aliquotaIbsUf;
    }

    public void setAliquotaIbsUf(BigDecimal aliquotaIbsUf) {
        this.aliquotaIbsUf = aliquotaIbsUf;
    }

    public BigDecimal getAliquotaIbsMun() {
        return aliquotaIbsMun;
    }

    public void setAliquotaIbsMun(BigDecimal aliquotaIbsMun) {
        this.aliquotaIbsMun = aliquotaIbsMun;
    }
}
