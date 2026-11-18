package br.com.billion.tela.calculo;

import java.math.BigDecimal;

public class ResultadoConta {

    private String cliente;
    private String documento;
    private String municipio;
    private String uf;
    private BigDecimal base;
    private BigDecimal aliquotaIbsUf;
    private BigDecimal valorIbsUf;
    private String memoriaIbsUf;
    private BigDecimal aliquotaIbsMun;
    private BigDecimal valorIbsMun;
    private String memoriaIbsMun;
    private BigDecimal valorIbs;
    private BigDecimal aliquotaCbs;
    private BigDecimal valorCbs;
    private String memoriaCbs;
    private BigDecimal total;
    private boolean simulado;
    private String json;

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getAliquotaIbsUf() {
        return aliquotaIbsUf;
    }

    public void setAliquotaIbsUf(BigDecimal aliquotaIbsUf) {
        this.aliquotaIbsUf = aliquotaIbsUf;
    }

    public BigDecimal getValorIbsUf() {
        return valorIbsUf;
    }

    public void setValorIbsUf(BigDecimal valorIbsUf) {
        this.valorIbsUf = valorIbsUf;
    }

    public String getMemoriaIbsUf() {
        return memoriaIbsUf;
    }

    public void setMemoriaIbsUf(String memoriaIbsUf) {
        this.memoriaIbsUf = memoriaIbsUf;
    }

    public BigDecimal getAliquotaIbsMun() {
        return aliquotaIbsMun;
    }

    public void setAliquotaIbsMun(BigDecimal aliquotaIbsMun) {
        this.aliquotaIbsMun = aliquotaIbsMun;
    }

    public BigDecimal getValorIbsMun() {
        return valorIbsMun;
    }

    public void setValorIbsMun(BigDecimal valorIbsMun) {
        this.valorIbsMun = valorIbsMun;
    }

    public String getMemoriaIbsMun() {
        return memoriaIbsMun;
    }

    public void setMemoriaIbsMun(String memoriaIbsMun) {
        this.memoriaIbsMun = memoriaIbsMun;
    }

    public BigDecimal getValorIbs() {
        return valorIbs;
    }

    public void setValorIbs(BigDecimal valorIbs) {
        this.valorIbs = valorIbs;
    }

    public BigDecimal getAliquotaCbs() {
        return aliquotaCbs;
    }

    public void setAliquotaCbs(BigDecimal aliquotaCbs) {
        this.aliquotaCbs = aliquotaCbs;
    }

    public BigDecimal getValorCbs() {
        return valorCbs;
    }

    public void setValorCbs(BigDecimal valorCbs) {
        this.valorCbs = valorCbs;
    }

    public String getMemoriaCbs() {
        return memoriaCbs;
    }

    public void setMemoriaCbs(String memoriaCbs) {
        this.memoriaCbs = memoriaCbs;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public boolean isSimulado() {
        return simulado;
    }

    public void setSimulado(boolean simulado) {
        this.simulado = simulado;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
