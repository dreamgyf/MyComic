package com.dreamgyf.mycomic.utils;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BeanUtils {

    public static String encodeBean(Object bean) throws Exception {
        if(!(bean instanceof Serializable))
            throw new Exception("只有序列化的bean才能加密");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(bean);
        String base64 = new String(Base64.encode(baos.toByteArray(), 0));
        oos.close();
        return base64;
    }

    public static <T> T decodeBean(String base64,Class<T> clz) throws Exception {
        byte[] base64Bytes = Base64.decode(base64.getBytes(), 1);
        ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        T bean = (T) ois.readObject();
        ois.close();
        return bean;
    }
}
