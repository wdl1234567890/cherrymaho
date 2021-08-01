package com.fl.mfs;

import java.io.Serializable;
import java.util.Objects;

public class Info implements Serializable {
    public String topic;
    public String title;
    public String url;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Info info = (Info) o;
        return Objects.equals(topic, info.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }
}
