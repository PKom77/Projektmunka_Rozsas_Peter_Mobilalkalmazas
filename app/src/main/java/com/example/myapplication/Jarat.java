package com.example.myapplication;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Jarat {

    private String ido;
    private String indulo;
    private String erkezo;
    private String ar;
    private String ferohely;

    public Jarat(){

    }

    public Jarat(String ido, String indulo, String erkezo, String ar) {
        this.ido = ido;
        this.indulo = indulo;
        this.erkezo = erkezo;
        this.ar = ar;
        this.ferohely = "50";
    }

    public Jarat(String ido, String indulo, String erkezo, String ar, String ferohely) {
        this.ido = ido;
        this.indulo = indulo;
        this.erkezo = erkezo;
        this.ar = ar;
        this.ferohely = ferohely;
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

    public ArrayList<Jarat> general(){
        String[] hungarian_cities = {"Budapest", "Békéscsaba", "Cegléd", "Dunaújváros", "Eger", "Győr", "Kaposvár", "Kecskemét", "Komárom", "Miskolc", "Nagykanizsa", "Nyíregyháza", "Pécs", "Siófok", "Sopron", "Szeged", "Székesfehérvár", "Szolnok", "Szombathely", "Tatabánya", "Zalaegerszeg"};

        List<String> idotomb = new ArrayList<>();
        List<String> indulotomb = new ArrayList<>();
        List<String> erkezotomb = new ArrayList<>();
        List<String> artomb = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < hungarian_cities.length; i++) {
            for (int j = 0; j < hungarian_cities.length; j++) {
                for (int h = 0; h < 4; h++) {
                    if (i != j) {
                        int hour = 6 + random.nextInt(5) * h;
                        String hourString = String.valueOf(hour) + ":00";
                        String induloCity = hungarian_cities[i];
                        String erkezoCity = hungarian_cities[j];
                        String arString = String.valueOf(random.nextInt(4000 - 400) + 400);

                        String[] sor = {hourString, induloCity, erkezoCity, arString};

                        idotomb.add(hourString);
                        indulotomb.add(induloCity);
                        erkezotomb.add(erkezoCity);
                        artomb.add(arString);
                    }
                }
            }
        }

        ArrayList<Jarat> jaratok = new ArrayList<>();
        for (int i = 0; i < idotomb.size(); i++) {
            Jarat j = new Jarat(idotomb.get(i),indulotomb.get(i),erkezotomb.get(i),artomb.get(i));
            jaratok.add(j);
        }
        return  jaratok;
    }
}
