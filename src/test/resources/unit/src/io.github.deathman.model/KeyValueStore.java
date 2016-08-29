package io.github.deathman.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Access(AccessType.FIELD)
@Table(name = "KEY_VALUE_STORE")
public class KeyValueStore {

    @Id
    @Column(name = "STORED_KEY", length = 128)
    private String key;

    @Column(name = "STORED_VALUE", length = 32768)
    private String value;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT")
    private Date createdAt;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}