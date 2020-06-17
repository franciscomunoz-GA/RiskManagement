package com.systramer.risk;

public class Cita {
    public int Id;
    public int Imagen;
    public String Titulo;
    public String Descripcion;
    public String Fecha;
    public String Hora;

    public Cita(int id, int imagen, String titulo, String descripcion, String fecha, String hora) {
        Id = id;
        Imagen = imagen;
        Titulo = titulo;
        Descripcion = descripcion;
        Fecha = fecha;
        Hora = hora;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo(String titulo) {
        Titulo = titulo;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    public int getImagen() {
        return Imagen;
    }

    public void setImagen(int imagen) {
        Imagen = imagen;
    }

    public String getHora() {
        return Hora;
    }

    public void setHora(String hora) {
        Hora = hora;
    }
}
