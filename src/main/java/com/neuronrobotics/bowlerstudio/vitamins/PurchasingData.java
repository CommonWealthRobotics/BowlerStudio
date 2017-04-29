package com.neuronrobotics.bowlerstudio.vitamins;

import java.util.HashMap;

public class PurchasingData {
    private HashMap<String, Double> variantParameters;
    private HashMap<Integer, Double> pricsUSD;
    private String urlAPI;
    private String db;
    private String serverType;
    private String cartURL;

    public PurchasingData() {
        variantParameters = new HashMap<>();
        variantParameters.put("Bolt Length", 10.0);
        pricsUSD = new HashMap<>();
        pricsUSD.put(1, 0.02);
        urlAPI = "http://localhost:8069/";
        db = "testdatabse";
        serverType = "odoo";
        cartURL = "http://localhost:8069/shop/product/m3-socket-cap-screw-73";
    }

    public HashMap<String, Double> getVariantParameters() {
        return variantParameters;
    }

    public void setVariantParameters(HashMap<String, Double> variantParameters) {
        this.variantParameters = variantParameters;
    }

    public double getPricsUSD(int qty) {
        return pricsUSD.get(qty);
    }

    public void setPricsUSD(int qty, double pricsUSD) {
        this.pricsUSD.put(qty, pricsUSD);
    }

    public String getAPIUrl() {
        return urlAPI;
    }

    public void setAPIUrl(String url) {
        this.urlAPI = url;
    }

    public String getDatabase() {
        return db;
    }

    public void setDatabase(String db) {
        this.db = db;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getCartUrl() {
        return cartURL;
    }

    public void setCartUrl(String cartURL) {
        this.cartURL = cartURL;
    }

    @Override
    public String toString() {
        String s = "\n";
        s += "urlAPI " + urlAPI + "\n";
        s += "db " + db + "\n";
        s += "serverType " + serverType + "\n";
        s += "cartURL " + cartURL + "\n";
        for (String key : variantParameters.keySet()) {
            s += "variable " + key + " to " + variantParameters.get(key) + "\n";
        }
        for (Integer key : pricsUSD.keySet()) {
            s += "Price at " + key + " = " + pricsUSD.get(key) + "\n";
        }

        return s;
    }
}
