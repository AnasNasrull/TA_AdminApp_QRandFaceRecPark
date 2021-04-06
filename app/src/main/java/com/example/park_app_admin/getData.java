package com.example.park_app_admin;

public class getData {
    String plat;
    String tanggal;
    String waktu_keluar;
    String waktu_masuk;
    String key;
    String NIM;

    public getData() {

    }

    public String getNIM() {
        return NIM;
    }

    public void setNIM(String NIM) {
        this.NIM = NIM;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public getData(String key, String plat, String tanggal, String waktu_keluar, String waktu_masuk) {
        this.key = key;
        this.plat = plat;
        this.tanggal = tanggal;
        this.waktu_keluar = waktu_keluar;
        this.waktu_masuk = waktu_masuk;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getWaktu_keluar() {
        return waktu_keluar;
    }

    public void setWaktu_keluar(String waktu_keluar) {
        this.waktu_keluar = waktu_keluar;
    }

    public String getWaktu_masuk() {
        return waktu_masuk;
    }

    public void setWaktu_masuk(String waktu_masuk) {
        this.waktu_masuk = waktu_masuk;
    }
}
