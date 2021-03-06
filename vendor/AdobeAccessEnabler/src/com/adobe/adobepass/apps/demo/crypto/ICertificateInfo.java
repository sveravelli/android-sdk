/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.crypto;

import java.security.cert.Certificate;

public interface ICertificateInfo {
    public static final String KEYSTORE_PKCS12 = "PKCS12";

    public Certificate getCertificate();

    public Certificate[] getCertificateChain();

    public boolean isValid();
}
