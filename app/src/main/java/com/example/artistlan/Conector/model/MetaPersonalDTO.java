package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class MetaPersonalDTO {

    @SerializedName("idMeta")
    private Integer idMeta;

    @SerializedName("tipoMeta")
    private String tipoMeta;

    @SerializedName("objetivo")
    private BigDecimal objetivo;

    @SerializedName("estado")
    private String estado;

    @SerializedName("fechaInicio")
    private String fechaInicio;

    @SerializedName("fechaFin")
    private String fechaFin;

    @SerializedName("fechaCreacion")
    private String fechaCreacion;

    @SerializedName("fechaActualizacion")
    private String fechaActualizacion;

    @SerializedName("fechaCancelacion")
    private String fechaCancelacion;

    @SerializedName("motivoCancelacion")
    private String motivoCancelacion;

    @SerializedName("progresoActual")
    private BigDecimal progresoActual;

    @SerializedName("porcentaje")
    private BigDecimal porcentaje;

    @SerializedName("porcentajeVisual")
    private BigDecimal porcentajeVisual;

    @SerializedName("progresoTexto")
    private String progresoTexto;

    @SerializedName("mensajeEstado")
    private String mensajeEstado;

    @SerializedName("editable")
    private Boolean editable;

    @SerializedName("cancelable")
    private Boolean cancelable;

    public Integer getIdMeta() {
        return idMeta;
    }

    public void setIdMeta(Integer idMeta) {
        this.idMeta = idMeta;
    }

    public String getTipoMeta() {
        return tipoMeta;
    }

    public void setTipoMeta(String tipoMeta) {
        this.tipoMeta = tipoMeta;
    }

    public BigDecimal getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(BigDecimal objetivo) {
        this.objetivo = objetivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(String fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public BigDecimal getProgresoActual() {
        return progresoActual;
    }

    public void setProgresoActual(BigDecimal progresoActual) {
        this.progresoActual = progresoActual;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getPorcentajeVisual() {
        return porcentajeVisual;
    }

    public void setPorcentajeVisual(BigDecimal porcentajeVisual) {
        this.porcentajeVisual = porcentajeVisual;
    }

    public String getProgresoTexto() {
        return progresoTexto;
    }

    public void setProgresoTexto(String progresoTexto) {
        this.progresoTexto = progresoTexto;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    public void setMensajeEstado(String mensajeEstado) {
        this.mensajeEstado = mensajeEstado;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getCancelable() {
        return cancelable;
    }

    public void setCancelable(Boolean cancelable) {
        this.cancelable = cancelable;
    }
}
