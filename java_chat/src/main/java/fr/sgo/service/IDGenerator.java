package fr.sgo.service;

import java.util.UUID;

/**
 * Class IDGenerator
 * 
 * generates random 32 characters strings
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
public class IDGenerator
{
    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
