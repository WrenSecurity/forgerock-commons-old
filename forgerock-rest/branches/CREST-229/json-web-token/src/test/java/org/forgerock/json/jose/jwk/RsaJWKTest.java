/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 ForgeRock AS All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions copyright [year] [name of copyright owner]"
 */
package org.forgerock.json.jose.jwk;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.util.encode.Base64url;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@SuppressWarnings("javadoc")
public class RsaJWKTest {

    //RSA parameter values
    private final String N = "MTQ0MDg1NDkwODc1OTU1MjYzNTg1MDcyOTU0MjQ2Mjg0NDExMzI3NjQxNzA2MjQ5OTQwMDU1MTMwMjgwNDA5N" +
            "zc2NzMzODIyMDU0ODIwNzUxMzc3OTIzNDE4NDQ5MjcxOTEyNTQxNjE1ODYxOTg0MTIwMzM4Mjg5NzMzMjk1MjQ5NTYwODY4MDYyMzY" +
            "4OTM0NDczOTg0NDc0MjA4NzI1OTMzODU1ODE5MzMwMDIxNzM1Nzg5MTk5ODY2MjE4NDc4NzcwOTgzOTU1NzgxNTE0NDc5MjkwNTc3M" +
            "jAzMDA0NjU1NjAyNTgxODcxOTcwNTU4NjU3NTg3MTgzOTM0ODM3NDk4NzE3OTc1NTg5NzQ1NzkzNDg1MzY0NzUwMzAxMTE4MTMzNjA" +
            "2NTY1Mjc1MDgzMjI2NDQx";
    private final String E = "NjU1Mzc";
    private final String D = "NDEwMDA1MDg0MDUxMDk5MzM0ODE4MTk4MTk3MDA5MTYzOTgxNjk3MjM4MjI4NzY0Njg3NDQxOTIyMDU5MTk3M" +
            "zg4Njk3ODQyMDU4NzM4MTQ2NzYzOTQ5MTkzNjg2MDgxNDM0NDU1MTMxMzMxOTQxMDE5NjA1NTMwNDk5NzUxNzU5OTMxMTI3MjI1MDk" +
            "3MDA3NjQ1MzUwOTQwNTQ4MDA4OTg3NzMyNzQ0MjI2NzM5MTA0NTIzNTc1MjExMDE2NzYxNTA5MjU3NTI2MDE1NjM0MTc1NzcyNTg2N" +
            "jA1OTk4MjUwNDYzMDE0NzUyNzY5MzYzNzI5ODExOTI4MzUzMzI3MTczMjg1NjAyNjQxMzQ5NTE4ODM4NDc2NzI3ODI0NzM1NTM0OTk" +
            "1MTM2ODY2OTkzNzUxNDE";
    private final String P = "MTI5NjY4NjkwMzMxODI0NTQ0NDU2MjE2NTc4NzQyNTA2Mzk3NDQ2NDMxMzQxMzcxNDgyMjAyNTY4OTk1MDA0M" +
            "zY3MjcyNTI2NDcyODA3NDcyNjM3NTc0NzU4MTUxMDg3NTU5MDU4NjkwODIwNTE3MTYzMjIwODA3OTk0OTc5Njc0OTYzNzg1MzIzMTY" +
            "4Nzc1MjAwMzU0MDY4NTk";
    private final String Q = "MTExMTE4MTgxNjU3NjA1OTA1ODYwMzA1MTczOTY5OTQxODEzMTA2Nzg2OTg2MDE1MDM3NjExODA1MjQ5ODIwM" +
            "jQ0ODI2MjkyOTEzMjY2NjAzODAzMTgzMDk5NzkwMzM0Njg5NjMyNTA4NzIzNTE4ODQwMDgwNjg4NjUxOTQwOTYyNzIyMTkzOTc3NTY" +
            "xMTMzODE4Nzk0NDg2OTk";
    private final String DP = "MTIyODE0OTY4ODcyMDQ3MDEwODE4Nzg1MjkyMTU5OTAzNTU5OTUzODAyMTY3NTIyOTU2NzIzMDczNDU4MTUx" +
            "OTc2NTg4OTEyMTUyNjI0OTAyNzExOTM2NDMyMjg1NzY5MjMwNDExMDY3MjY0MzIzMzg3ODk2OTkyNzYyNDQ1MjM0OTY2OTIwMTU3NT" +
            "Q1NDM4MTk1Mzk0NjMzNTU";
    private final String DQ = "NjEzNjAyNTQ0MjQwNDY4Mzk2OTQ4OTY2ODgwNjg5MTA2MDM0MTk5NzA3MTkxOTUwMjI4Mjk0MTI4ODExMzU3" +
            "NjU0NDk0OTQ3OTczMDcxNDU0NTQyODA3MzI3Mzc0MTg3NDY5MzM4MDA2MTE5MzEzNzk1NjQ1MjI2NzEzNzI1NTk2MzY3ODgzNzk4MT" +
            "UwMjcxMjgyMjAyNzk5";
    private final String QI = "NjI2ODI2NzE4MTA4NDYzNjAxMjU5MTc2ODcwMDQ5OTYwNDI3MTAyMjA4MzU3MTE5MTE1NTU4MjA1ODE2Njkz" +
            "OTA2NzUwMTA2NjM4MjE2MDc2NjgyNTYxODQyNjY3OTcyMDQ3MzEwMzgxNDc0ODg2MjA2NDE2NjQ2MDU1MzA4Nzg2NTQ2MTAwMjA5OD" +
            "ExNzQ4OTIyMDM5MzkxMw";

    private final String N_SP = "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4" +
            "cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMst" +
            "n64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2Q" +
            "vzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbIS" +
            "D08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw" +
            "0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw";
    private final String E_SP = "AQAB";
    private final String D_SP = "X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9" +
            "M7dx5oo7GURknchnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqij" +
            "wp3RTzlBaCxWp4doFk5N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d" +
            "_cPVY3i07a3t8MN6TNwm0dSawm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBz" +
            "nbJSzFHK66jT8bgkuqsk0GjskDJk19Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFz" +
            "me1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q";
    private final String P_SP = "83i-7IvMGXoMXCskv73TKr8637FiO7Z27zv8oj6pbWUQyLPQBQxtPV" +
            "nwD20R-60eTDmD2ujnMt5PoqMrm8RfmNhVWDtjjMmCMjOpSXicFHj7XOuVIYQyqV" +
            "WlWEh6dN36GVZYk93N8Bc9vY41xy8B9RzzOGVQzXvNEvn7O0nVbfs";
    private final String Q_SP = "3dfOR9cuYq-0S-mkFLzgItgMEfFzB2q3hWehMuG0oCuqnb3vobLyum" +
            "qjVZQO1dIrdwgTnCdpYzBcOfW5r370AFXjiWft_NGEiovonizhKpo9VVS78TzFgx" +
            "kIdrecRezsZ-1kYd_s1qDbxtkDEgfAITAG9LUnADun4vIcb6yelxk";
    private final String DQ_SP = "s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUU" +
            "vMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9" +
            "GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk";
    private final String DP_SP = "s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUU" +
            "vMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9" +
            "GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk";
    private final String QI_SP = "GyM_p6JrXySiz1toFgKbWV-JdI3jQ4ypu9rbMWx3rQJBfmt0FoYzg" +
            "UIZEVFEcOqwemRN81zoDAaa-Bk0KWNGDjJHZDdDmFhW3AN7lI-puxk_mHZGJ11rx" +
            "yR8O55XLSe3SPmRfKwZI6yU24ZxvQKFYItdldUKGzO6Ia6zTKhAVRU";

    private final String ALG = "RS256";
    private final String KID = "2011-04-29";
    private final String KTY = "RSA";

    //json objects
    private String json = null;
    private JsonValue jsonValue = null;

    @BeforeClass
    public void setup(){
        /*{
         "kty":"RSA",
          "n":"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4
     cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMst
     n64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2Q
     vzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbIS
     D08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw
     0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
          "e":"AQAB",
          "d":"X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9
     M7dx5oo7GURknchnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqij
     wp3RTzlBaCxWp4doFk5N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d
     _cPVY3i07a3t8MN6TNwm0dSawm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBz
     nbJSzFHK66jT8bgkuqsk0GjskDJk19Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFz
     me1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q",
          "p":"83i-7IvMGXoMXCskv73TKr8637FiO7Z27zv8oj6pbWUQyLPQBQxtPV
     nwD20R-60eTDmD2ujnMt5PoqMrm8RfmNhVWDtjjMmCMjOpSXicFHj7XOuVIYQyqV
     WlWEh6dN36GVZYk93N8Bc9vY41xy8B9RzzOGVQzXvNEvn7O0nVbfs",
          "q":"3dfOR9cuYq-0S-mkFLzgItgMEfFzB2q3hWehMuG0oCuqnb3vobLyum
     qjVZQO1dIrdwgTnCdpYzBcOfW5r370AFXjiWft_NGEiovonizhKpo9VVS78TzFgx
     kIdrecRezsZ-1kYd_s1qDbxtkDEgfAITAG9LUnADun4vIcb6yelxk",
          "dp":"G4sPXkc6Ya9y8oJW9_ILj4xuppu0lzi_H7VTkS8xj5SdX3coE0oim
     YwxIi2emTAue0UOa5dpgFGyBJ4c8tQ2VF402XRugKDTP8akYhFo5tAA77Qe_Nmtu
     YZc3C3m3I24G2GvR5sSDxUyAN2zq8Lfn9EUms6rY3Ob8YeiKkTiBj0",
          "dq":"s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUU
     vMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9
     GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk",
          "qi":"GyM_p6JrXySiz1toFgKbWV-JdI3jQ4ypu9rbMWx3rQJBfmt0FoYzg
     UIZEVFEcOqwemRN81zoDAaa-Bk0KWNGDjJHZDdDmFhW3AN7lI-puxk_mHZGJ11rx
     yR8O55XLSe3SPmRfKwZI6yU24ZxvQKFYItdldUKGzO6Ia6zTKhAVRU",
          "alg":"RS256",
          "kid":"2011-04-29"
          }
         */
        //create string rsa JWT
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"kty\"").append(":").append("\"" + KTY + "\"").append(",")
                .append("\"n\"").append(":").append("\"" + N + "\"").append(",")
                .append("\"e\"").append(":").append("\"" + E + "\"").append(",")
                .append("\"d\"").append(":").append("\"" + D + "\"").append(",")
                .append("\"p\"").append(":").append("\"" + P + "\"").append(",")
                .append("\"q\"").append(":").append("\"" + Q + "\"").append(",")
                .append("\"dp\"").append(":").append("\"" + DP + "\"").append(",")
                .append("\"dq\"").append(":").append("\"" + DQ + "\"").append(",")
                .append("\"qi\"").append(":").append("\"" + QI + "\"").append(",")
                .append("\"alg\"").append(":").append("\"" + ALG + "\"").append(",")
                .append("\"kid\"").append(":").append("\"" + KID + "\"")
                .append("}");
        json = sb.toString();

        //create json value object
        jsonValue = new JsonValue(new HashMap<String, String>());
        jsonValue.put("kty", KTY);
        jsonValue.put("n", N);
        jsonValue.put("e", E);
        jsonValue.put("d", D);
        jsonValue.put("p", P);
        jsonValue.put("q", Q);
        jsonValue.put("dp", DP);
        jsonValue.put("dq", DQ);
        jsonValue.put("qi", QI);
        jsonValue.put("alg", ALG);
        jsonValue.put("kid", KID);
    }

    @Test
    public void testCreateJWKFromAString(){
        //Given

        //When
        RsaJWK jwk = RsaJWK.parse(json);

        //Then
        assertEquals(jwk.getModulus(), N);
        assertEquals(jwk.getPublicExponent(), E);
        assertEquals(jwk.getPrivateExponent(), D);
        assertEquals(jwk.getPrimeP(), P);
        assertEquals(jwk.getPrimeQ(), Q);
        assertEquals(jwk.getPrimePExponent(), DP);
        assertEquals(jwk.getPrimeQExponent(), DQ);
        assertEquals(jwk.getCRTCoefficient(), QI);
    }

    @Test
    public void testCreateJWKFromAJsonValue(){
        //Given

        //When
        RsaJWK jwk = RsaJWK.parse(jsonValue);

        //Then
        assertEquals(jwk.getModulus(), N);
        assertEquals(jwk.getPublicExponent(), E);
        assertEquals(jwk.getPrivateExponent(), D);
        assertEquals(jwk.getPrimeP(), P);
        assertEquals(jwk.getPrimeQ(), Q);
        assertEquals(jwk.getPrimePExponent(), DP);
        assertEquals(jwk.getPrimeQExponent(), DQ);
        assertEquals(jwk.getCRTCoefficient(), QI);
    }

    @Test
    public void testCreatePrivateKey(){
        //Given
        RsaJWK jwk = RsaJWK.parse(json);

        //When
        RSAPrivateKey privKey = jwk.toRSAPrivateKey();

        //Then
        testPrivateKey(privKey);
    }

    @Test
    public void testCreatePublicKey(){
        //Given
        RsaJWK jwk = RsaJWK.parse(json);

        //When
        RSAPublicKey pubKey = jwk.toRSAPublicKey();

        //Then
        testPublicKey(pubKey);
    }

    @Test
    public void testCreatePairKey(){
        //Given
        RsaJWK jwk = RsaJWK.parse(json);

        //When
        KeyPair keypair = jwk.toKeyPair();

        //Then
        testPrivateKey((RSAPrivateKey) keypair.getPrivate());
        testPublicKey((RSAPublicKey) keypair.getPublic());
    }

    @Test
    public void testCreateJWKUsingPublicKey(){
        //Given
        KeyPair keypair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            keypair = generator.genKeyPair();
        } catch (Exception e){
            assertFalse(false, e.getLocalizedMessage());
        }

        //When
        RsaJWK jwk = new RsaJWK((RSAPublicKey) keypair.getPublic(), null, ALG, KID, null, null, null);

        //Then
        BigInteger modulus = toPosBigInt(jwk.getModulus());
        BigInteger pubExponent = toPosBigInt(jwk.getPublicExponent());
        assertEquals(modulus, ((RSAPublicKey) keypair.getPublic()).getModulus());
        assertEquals(pubExponent, ((RSAPublicKey) keypair.getPublic()).getPublicExponent());

    }

    @Test
    public void testCreateJWKUsingPublicKeyandPrivateKey(){
        //Given
        KeyPair keypair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            keypair = generator.genKeyPair();
        } catch (Exception e){
            assertFalse(false, e.getLocalizedMessage());
        }

        //When
        RsaJWK jwk = new
                RsaJWK((RSAPublicKey)keypair.getPublic(),(RSAPrivateKey)keypair.getPrivate(), null, ALG, KID,
                null, null, null);

        //Then
        BigInteger modulus = toPosBigInt(jwk.getModulus());
        BigInteger pubExponent = toPosBigInt(jwk.getPublicExponent());
        BigInteger privExponent = toPosBigInt(jwk.getPrivateExponent());

        assertEquals(modulus, ((RSAPublicKey) keypair.getPublic()).getModulus());
        assertEquals(pubExponent, ((RSAPublicKey) keypair.getPublic()).getPublicExponent());
        assertEquals(privExponent ,((RSAPrivateKey) keypair.getPrivate()).getPrivateExponent());
    }

    @Test
    public void testCreateJWKUsingPublicKeyandPrivateCert(){
        //Given
        KeyPair keypair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            keypair = generator.genKeyPair();
        } catch (Exception e){
            assertFalse(false, e.getLocalizedMessage());
        }

        //When
        RsaJWK jwk = new
                RsaJWK((RSAPublicKey)keypair.getPublic(),(RSAPrivateCrtKey)keypair.getPrivate(), null, ALG, KID,
                null, null, null);

        //Then
        BigInteger modulus = toPosBigInt(jwk.getModulus());
        BigInteger pubExponent = toPosBigInt(jwk.getPublicExponent());
        BigInteger privExponent = toPosBigInt(jwk.getPrivateExponent());
        BigInteger primeP = toPosBigInt(jwk.getPrimeP());
        BigInteger primeQ = toPosBigInt(jwk.getPrimeQ());
        BigInteger primePExponent = toPosBigInt(jwk.getPrimePExponent());
        BigInteger primeQExponent = toPosBigInt(jwk.getPrimeQExponent());
        BigInteger crtCoefficient = toPosBigInt(jwk.getCRTCoefficient());

        assertEquals(modulus, ((RSAPublicKey) keypair.getPublic()).getModulus());
        assertEquals(pubExponent, ((RSAPublicKey) keypair.getPublic()).getPublicExponent());
        assertEquals(privExponent, ((RSAPrivateCrtKey) keypair.getPrivate()).getPrivateExponent());
        assertEquals(primeP, ((RSAPrivateCrtKey) keypair.getPrivate()).getPrimeP());
        assertEquals(primeQ, ((RSAPrivateCrtKey) keypair.getPrivate()).getPrimeQ());
        assertEquals(primePExponent, ((RSAPrivateCrtKey) keypair.getPrivate()).getPrimeExponentP());
        assertEquals(primeQExponent, ((RSAPrivateCrtKey) keypair.getPrivate()).getPrimeExponentQ());
        assertEquals(crtCoefficient, ((RSAPrivateCrtKey) keypair.getPrivate()).getCrtCoefficient());
    }

    private String createJsonForSecurityProviderTest() {

        //create string rsa JWT
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"kty\"").append(":").append("\"" + KTY + "\"").append(",")
                .append("\"n\"").append(":").append("\"" + N_SP + "\"").append(",")
                .append("\"e\"").append(":").append("\"" + E_SP + "\"").append(",")
                .append("\"d\"").append(":").append("\"" + D_SP + "\"").append(",")
                .append("\"p\"").append(":").append("\"" + P_SP + "\"").append(",")
                .append("\"q\"").append(":").append("\"" + Q_SP + "\"").append(",")
                .append("\"dp\"").append(":").append("\"" + DP_SP + "\"").append(",")
                .append("\"dq\"").append(":").append("\"" + DQ_SP + "\"").append(",")
                .append("\"qi\"").append(":").append("\"" + QI_SP + "\"").append(",")
                .append("\"alg\"").append(":").append("\"" + ALG + "\"").append(",")
                .append("\"kid\"").append(":").append("\"" + KID + "\"")
                .append("}");
        return sb.toString();
    }

    private void testPrivateKey(RSAPrivateKey privKey){

        BigInteger modulus = toPosBigInt(N);
        BigInteger privateExponent = toPosBigInt(D);

        assertEquals(privKey.getModulus(), modulus);
        assertEquals(privKey.getPrivateExponent(), privateExponent);

    }

    private void testPublicKey(RSAPublicKey pubKey){
        BigInteger modulus = toPosBigInt(N);
        BigInteger publicExponent = toPosBigInt(E);

        assertEquals(pubKey.getPublicExponent(), publicExponent);
        assertEquals(pubKey.getModulus(), modulus);
    }

    @Test
    public void testSecurityProviderWithCreatePrivateKey(){
        //Given
        RsaJWK jwk = RsaJWK.parse(createJsonForSecurityProviderTest());

        //When
        RSAPrivateKey privKey = jwk.toRSAPrivateKey();

        //Then
        testSecurityProviderPrivateKey(privKey);
    }

    @Test
    public void testSecurityProviderWithCreatePublicKey(){
        //Given
        RsaJWK jwk = RsaJWK.parse(createJsonForSecurityProviderTest());

        //When
        RSAPublicKey pubKey = jwk.toRSAPublicKey();

        //Then
        testSecurityProviderPublicKey(pubKey);
    }

    @Test
    public void testSecurityProviderWithCreatePairKey(){
        //Given
        RsaJWK jwk = RsaJWK.parse(createJsonForSecurityProviderTest());

        //When
        KeyPair keypair = jwk.toKeyPair();

        //Then
        testSecurityProviderPrivateKey((RSAPrivateKey) keypair.getPrivate());
        testSecurityProviderPublicKey((RSAPublicKey) keypair.getPublic());
    }

    private void testSecurityProviderPrivateKey(RSAPrivateKey privKey){

        BigInteger modulus = toPosBigInt(N_SP);
        BigInteger privateExponent = toPosBigInt(D_SP);

        assertEquals(privKey.getModulus(), modulus);
        assertEquals(privKey.getPrivateExponent(), privateExponent);

    }

    private void testSecurityProviderPublicKey(RSAPublicKey pubKey){
        BigInteger modulus = toPosBigInt(N_SP);
        BigInteger publicExponent = toPosBigInt(E_SP);

        assertEquals(pubKey.getPublicExponent(), publicExponent);
        assertEquals(pubKey.getModulus(), modulus);
    }

    private BigInteger toPosBigInt(String s) {
        return new BigInteger(1, Base64url.decode(s));
    }
}
