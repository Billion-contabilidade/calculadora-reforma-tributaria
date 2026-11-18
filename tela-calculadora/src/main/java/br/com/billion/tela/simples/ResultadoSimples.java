package br.com.billion.tela.simples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ResultadoSimples {

    private String cliente;
    private String documento;
    private boolean calculado;
    private String estado;
    private String tipoReceita;
    private BigDecimal receitaBruta = BigDecimal.ZERO;
    private BigDecimal base = BigDecimal.ZERO;
    private BigDecimal aliquotaIbs = BigDecimal.ZERO;
    private BigDecimal valorIbs = BigDecimal.ZERO;
    private BigDecimal aliquotaCbs = BigDecimal.ZERO;
    private BigDecimal valorCbs = BigDecimal.ZERO;
    private BigDecimal valorIs = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private final List<String> mensagens = new ArrayList<>();
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

    public boolean isCalculado() {
        return calculado;
    }

    public void setCalculado(boolean calculado) {
        this.calculado = calculado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoReceita() {
        return tipoReceita;
    }

    public void setTipoReceita(String tipoReceita) {
        this.tipoReceita = tipoReceita;
    }

    public BigDecimal getReceitaBruta() {
        return receitaBruta;
    }

    public void setReceitaBruta(BigDecimal receitaBruta) {
        this.receitaBruta = receitaBruta;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getAliquotaIbs() {
        return aliquotaIbs;
    }

    public void setAliquotaIbs(BigDecimal aliquotaIbs) {
        this.aliquotaIbs = aliquotaIbs;
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

    public BigDecimal getValorIs() {
        return valorIs;
    }

    public void setValorIs(BigDecimal valorIs) {
        this.valorIs = valorIs;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<String> getMensagens() {
        return mensagens;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
