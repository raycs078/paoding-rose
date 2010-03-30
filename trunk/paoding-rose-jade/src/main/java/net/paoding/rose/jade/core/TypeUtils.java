package net.paoding.rose.jade.jadeinterface.impl;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;

import org.apache.commons.lang.ClassUtils;

public class TypeUtils {

    public static boolean isColumnType(Class<?> columnTypeCandidate) {
        return String.class == columnTypeCandidate // NL
                || ClassUtils.wrapperToPrimitive(columnTypeCandidate) != null // NL
                || Date.class.isAssignableFrom(columnTypeCandidate) // NL
                || columnTypeCandidate == byte[].class // NL
                || columnTypeCandidate == BigDecimal.class // NL
                || columnTypeCandidate == Blob.class // NL
                || columnTypeCandidate == Clob.class;
    }
}
