package de.vectordata.jvsl.crypt;

import android.util.Base64;

/**
 * Created by Twometer on 07.03.2018.
 * (c) 2018 Twometer
 */

public class RsaParams {

    public byte[] modulus;
    public byte[] exponent;
    public byte[] d;

    public static RsaParams fromXml(String xml, boolean isPrivate) {
        xml = xml.replace(" ", "").replace("\r", "").replace("\n", "");
        RsaParams params = new RsaParams();
        params.modulus = Base64.decode(xml.split("<Modulus>")[1].split("</Modulus>")[0], Base64.DEFAULT);
        params.exponent = Base64.decode(xml.split("<Exponent>")[1].split("</Exponent>")[0], Base64.DEFAULT);

        if(isPrivate)
            params.d = Base64.decode(xml.split("<D>")[1].split("</D>")[0], Base64.DEFAULT);
        return params;
    }

}
