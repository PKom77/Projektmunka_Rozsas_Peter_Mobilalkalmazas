package com.example.myapplication;

public class MegvaltottJegyek {
    private String ido;
    private String indulo;
    private String erkezo;
    private String ar;
    private String ferohely;
    private String email;

    public MegvaltottJegyek(String ido, String indulo, String erkezo, String ar, String ferohely, String email) {
        this.ido = ido;
        this.indulo = indulo;
        this.erkezo = erkezo;
        this.ar = ar;
        this.ferohely = ferohely;
        this.email = email;
    }

    public String getIdo() {
        return ido;
    }

    public void setIdo(String ido) {
        this.ido = ido;
    }

    public String getIndulo() {
        return indulo;
    }

    public void setIndulo(String indulo) {
        this.indulo = indulo;
    }

    public String getErkezo() {
        return erkezo;
    }

    public void setErkezo(String erkezo) {
        this.erkezo = erkezo;
    }

    public String getAr() {
        return ar;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public String getFerohely() {
        return ferohely;
    }

    public void setFerohely(String ferohely) {
        this.ferohely = ferohely;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
