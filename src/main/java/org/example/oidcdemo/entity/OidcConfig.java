package org.example.oidcdemo.entity;

public class OidcConfig {
    private String type;
    private String id;
    private String password;
    private String issuer;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        return "OidcConfig{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", issuer='" + issuer + '\'' +
                '}';
    }
}
