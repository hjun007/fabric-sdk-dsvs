/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.whu.gmssl.sun.security.ssl;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import javax.security.auth.Subject;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.*;

/**
 * ClientHandshaker does the protocol handshaking from the point
 * of view of a client.  It is driven asychronously by handshake messages
 * as delivered by the parent Handshaker class, and also uses
 * common functionality (e.g. key generation) that is provided there.
 *
 * @author David Brownell
 */
final class ClientHandshaker extends Handshaker {

    // the server's public key from its certificate.
    private PublicKey serverKey;

    //TODO modify by ringo
    private PublicKey encServerKey = null;

    // the server's ephemeral public key from the server key exchange message
    // for ECDHE/ECDH_anon and RSA_EXPORT.
    private PublicKey ephemeralServerKey;

    // server's ephemeral public value for DHE/DH_anon key exchanges
    private BigInteger          serverDH;

    private DHCrypt             dh;

    private ECDHCrypt ecdh;

    //TODO TODO modify by ringo
    private SM2Crypt sm2;

    // TODO SM2KeyExchange params 暂时没用上，测试用，本来应该从证书里获取
    private byte[] encIdLocal = "1234567812345678".getBytes();
    private byte[] encIdRemote = "1234567812345678".getBytes();

    private HandshakeMessage.CertificateRequest certRequest;

    private boolean serverKeyExchangeReceived;

    /*
     * The RSA PreMasterSecret needs to know the version of
     * ClientHello that was used on this handshake.  This represents
     * the "max version" this client is supporting.  In the
     * case of an initial handshake, it's the max version enabled,
     * but in the case of a resumption attempt, it's the version
     * of the session we're trying to resume.
     */
    private ProtocolVersion maxProtocolVersion;

    // To switch off the SNI extension.
    private final static boolean enableSNIExtension =
            Debug.getBooleanProperty("jsse.enableSNIExtension", true);

    private List<SNIServerName> requestedServerNames =
            Collections.<SNIServerName>emptyList();

    private boolean serverNamesAccepted = false;

    /*
     * Constructors
     */
    ClientHandshaker(SSLSocketImpl socket, SSLContextImpl context,
            ProtocolList enabledProtocols,
            ProtocolVersion activeProtocolVersion,
            boolean isInitialHandshake, boolean secureRenegotiation,
            byte[] clientVerifyData, byte[] serverVerifyData) {

        super(socket, context, enabledProtocols, true, true,
            activeProtocolVersion, isInitialHandshake, secureRenegotiation,
            clientVerifyData, serverVerifyData);
    }

    ClientHandshaker(SSLEngineImpl engine, SSLContextImpl context,
            ProtocolList enabledProtocols,
            ProtocolVersion activeProtocolVersion,
            boolean isInitialHandshake, boolean secureRenegotiation,
            byte[] clientVerifyData, byte[] serverVerifyData) {

        super(engine, context, enabledProtocols, true, true,
            activeProtocolVersion, isInitialHandshake, secureRenegotiation,
            clientVerifyData, serverVerifyData);
    }

    /*
     * This routine handles all the client side handshake messages, one at
     * a time.  Given the message type (and in some cases the pending cipher
     * spec) it parses the type-specific message.  Then it calls a function
     * that handles that specific message.
     *
     * It updates the state machine (need to verify it) as each message
     * is processed, and writes responses as needed using the connection
     * in the constructor.
     */
    @Override
    void processMessage(byte type, int messageLen) throws IOException {
        if (state >= type
                && (type != HandshakeMessage.ht_hello_request)) {
            throw new SSLProtocolException(
                    "Handshake message sequence violation, " + type);
        }

        switch (type) {
        case HandshakeMessage.ht_hello_request:
            this.serverHelloRequest(new HandshakeMessage.HelloRequest(input));
            break;

        case HandshakeMessage.ht_server_hello:
            this.serverHello(new HandshakeMessage.ServerHello(input, messageLen));
            break;

        case HandshakeMessage.ht_certificate:
            if (keyExchange == CipherSuite.KeyExchange.K_DH_ANON || keyExchange == CipherSuite.KeyExchange.K_ECDH_ANON
                    || keyExchange == CipherSuite.KeyExchange.K_KRB5 || keyExchange == CipherSuite.KeyExchange.K_KRB5_EXPORT) {
                fatalSE(Alerts.alert_unexpected_message,
                    "unexpected server cert chain");
                // NOTREACHED
            }
            this.serverCertificate(new HandshakeMessage.CertificateMsg(input));

            //TODO modify by ringo 国密的加密证书和签名证书
            Certificate[] certificates = this.session.getPeerCertificates();
            serverKey = certificates[0].getPublicKey();

            if (this.protocolVersion.isIsgm() && this.protocolVersion.minor == 2) {
                if (certificates.length > 1){
                    this.encServerKey = certificates[1].getPublicKey();
                }else {
                    //TODO 只提供了一个证书做加密签名使用
                    this.encServerKey = certificates[0].getPublicKey();
                }
            }

            break;

        case HandshakeMessage.ht_server_key_exchange:
            serverKeyExchangeReceived = true;
            switch (keyExchange) {
            case K_RSA_EXPORT:
                /**
                 * The server key exchange message is sent by the server only
                 * when the server certificate message does not contain the
                 * proper amount of data to allow the client to exchange a
                 * premaster secret, such as when RSA_EXPORT is used and the
                 * public key in the server certificate is longer than 512 bits.
                 */
                if (serverKey == null) {
                    throw new SSLProtocolException
                        ("Server did not send certificate message");
                }

                if (!(serverKey instanceof RSAPublicKey)) {
                    throw new SSLProtocolException("Protocol violation:" +
                        " the certificate type must be appropriate for the" +
                        " selected cipher suite's key exchange algorithm");
                }

                if (JsseJce.getRSAKeyLength(serverKey) <= 512) {
                    throw new SSLProtocolException("Protocol violation:" +
                        " server sent a server key exchange message for" +
                        " key exchange " + keyExchange +
                        " when the public key in the server certificate" +
                        " is less than or equal to 512 bits in length");
                }

                try {
                    this.serverKeyExchange(new HandshakeMessage.RSA_ServerKeyExchange(input));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_DH_ANON:
                try {
                    this.serverKeyExchange(new HandshakeMessage.DH_ServerKeyExchange(
                                                input, protocolVersion));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_DHE_DSS:
            case K_DHE_RSA:
                try {
                    this.serverKeyExchange(new HandshakeMessage.DH_ServerKeyExchange(
                        input, serverKey,
                        clnt_random.random_bytes, svr_random.random_bytes,
                        messageLen,
                        localSupportedSignAlgs, protocolVersion));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_ECDHE_ECDSA:
            case K_ECDHE_RSA:
            case K_ECDH_ANON:
                try {
                    this.serverKeyExchange(new HandshakeMessage.ECDH_ServerKeyExchange
                        (input, serverKey, clnt_random.random_bytes,
                        svr_random.random_bytes,
                        localSupportedSignAlgs, protocolVersion));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_RSA:
            case K_DH_RSA:
            case K_DH_DSS:
            case K_ECDH_ECDSA:
            case K_ECDH_RSA:
                throw new SSLProtocolException(
                    "Protocol violation: server sent a server key exchange"
                    + "message for key exchange " + keyExchange);
            //TODO modify by ringo
            case K_SM2_SM2:
                try {
                    this.sm2ServerKeyExchange(new HandshakeMessage.SM2_ServerKeyExchange(this.input, this.serverKey, this.clnt_random.random_bytes, this.svr_random.random_bytes, this.localSupportedSignAlgs, this.protocolVersion));
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_ECC:
                try {
                    ECCServerKeyExchange eccServerKeyExchange = new ECCServerKeyExchange(this.input);
                    boolean certVerified = false;

                    Certificate[] certificates1 = this.session.getPeerCertificates();
                    // TODO 如果只有单证书，则使用它
                    X509Certificate encCertificate = certificates1.length > 1 ?
                            (X509Certificate)certificates1[1]: (X509Certificate)certificates1[0];
                    certVerified = eccServerKeyExchange.verify(this.serverKey,
                            this.clnt_random, this.svr_random, encCertificate);
                    if (!certVerified) {
                        this.fatalSE((byte)40, "server key exchange invalid");
                    }
                } catch (GeneralSecurityException e) {
                    throwSSLException("Server key", e);
                }
                break;
            case K_KRB5:
            case K_KRB5_EXPORT:
                throw new SSLProtocolException(
                    "unexpected receipt of server key exchange algorithm");
            default:
                throw new SSLProtocolException(
                    "unsupported key exchange algorithm = "
                    + keyExchange);
            }
            break;

        case HandshakeMessage.ht_certificate_request:
            // save for later, it's handled by serverHelloDone
            if ((keyExchange == CipherSuite.KeyExchange.K_DH_ANON) || (keyExchange == CipherSuite.KeyExchange.K_ECDH_ANON)) {
                throw new SSLHandshakeException(
                    "Client authentication requested for "+
                    "anonymous cipher suite.");
            } else if (keyExchange == CipherSuite.KeyExchange.K_KRB5 || keyExchange == CipherSuite.KeyExchange.K_KRB5_EXPORT) {
                throw new SSLHandshakeException(
                    "Client certificate requested for "+
                    "kerberos cipher suite.");
            }
            certRequest = new HandshakeMessage.CertificateRequest(input, protocolVersion);
            if (debug != null && Debug.isOn("handshake")) {
                certRequest.print(System.out);
            }

            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                Collection<SignatureAndHashAlgorithm> peerSignAlgs =
                                        certRequest.getSignAlgorithms();
                if (peerSignAlgs == null || peerSignAlgs.isEmpty()) {
                    throw new SSLHandshakeException(
                        "No peer supported signature algorithms");
                }

                Collection<SignatureAndHashAlgorithm> supportedPeerSignAlgs =
                    SignatureAndHashAlgorithm.getSupportedAlgorithms(
                                                            peerSignAlgs);
                if (supportedPeerSignAlgs.isEmpty()) {
                    throw new SSLHandshakeException(
                        "No supported signature and hash algorithm in common");
                }

                setPeerSupportedSignAlgs(supportedPeerSignAlgs);
                session.setPeerSupportedSignatureAlgorithms(
                                                supportedPeerSignAlgs);
            }

            break;

        case HandshakeMessage.ht_server_hello_done:
            this.serverHelloDone(new HandshakeMessage.ServerHelloDone(input));
            break;

        case HandshakeMessage.ht_finished:
            this.serverFinished(
                new HandshakeMessage.Finished(protocolVersion, input, cipherSuite));
            break;

        default:
            throw new SSLProtocolException(
                "Illegal client handshake msg, " + type);
        }

        //
        // Move state machine forward if the message handling
        // code didn't already do so
        //
        if (state < type) {
            state = type;
        }
    }

    /*
     * Used by the server to kickstart negotiations -- this requests a
     * "client hello" to renegotiate current cipher specs (e.g. maybe lots
     * of data has been encrypted with the same keys, or the server needs
     * the client to present a certificate).
     */
    private void serverHelloRequest(HandshakeMessage.HelloRequest mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }

        //
        // Could be (e.g. at connection setup) that we already
        // sent the "client hello" but the server's not seen it.
        //
        if (state < HandshakeMessage.ht_client_hello) {
            if (!secureRenegotiation && !allowUnsafeRenegotiation) {
                // renegotiation is not allowed.
                if (activeProtocolVersion.v >= ProtocolVersion.TLS10.v) {
                    // response with a no_renegotiation warning,
                    warningSE(Alerts.alert_no_renegotiation);

                    // invalidate the handshake so that the caller can
                    // dispose this object.
                    invalidated = true;

                    // If there is still unread block in the handshake
                    // input stream, it would be truncated with the disposal
                    // and the next handshake message will become incomplete.
                    //
                    // However, according to SSL/TLS specifications, no more
                    // handshake message should immediately follow ClientHello
                    // or HelloRequest. So just let it be.
                } else {
                    // For SSLv3, send the handshake_failure fatal error.
                    // Note that SSLv3 does not define a no_renegotiation
                    // alert like TLSv1. However we cannot ignore the message
                    // simply, otherwise the other side was waiting for a
                    // response that would never come.
                    fatalSE(Alerts.alert_handshake_failure,
                        "Renegotiation is not allowed");
                }
            } else {
                if (!secureRenegotiation) {
                    if (debug != null && Debug.isOn("handshake")) {
                        System.out.println(
                            "Warning: continue with insecure renegotiation");
                    }
                }
                kickstart();
            }
        }
    }


    /*
     * Server chooses session parameters given options created by the
     * client -- basically, cipher options, session id, and someday a
     * set of compression options.
     *
     * There are two branches of the state machine, decided by the
     * details of this message.  One is the "fast" handshake, where we
     * can resume the pre-existing session we asked resume.  The other
     * is a more expensive "full" handshake, with key exchange and
     * probably authentication getting done.
     */
    private void serverHello(HandshakeMessage.ServerHello mesg) throws IOException {
        serverKeyExchangeReceived = false;
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }

        // check if the server selected protocol version is OK for us
        ProtocolVersion mesgVersion = mesg.protocolVersion;

        if (!this.protocolVersion.isIsgm()){
            if (!isNegotiable(mesgVersion)) {
                throw new SSLHandshakeException(
                        "Server chose " + mesgVersion +
                                ", but that protocol version is not enabled or not supported " +
                                "by the client.");
            }

            handshakeHash.protocolDetermined(mesgVersion);

            // Set protocolVersion and propagate to SSLSocket and the
            // Handshake streams
            setVersion(mesgVersion);
        }else {
            handshakeHash.protocolDetermined(this.protocolVersion);

            // Set protocolVersion and propagate to SSLSocket and the
            // Handshake streams
            setVersion(this.protocolVersion);
        }


        // check the "renegotiation_info" extension
        RenegotiationInfoExtension serverHelloRI = (RenegotiationInfoExtension)
                    mesg.extensions.get(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (serverHelloRI != null) {
            if (isInitialHandshake) {
                // verify the length of the "renegotiated_connection" field
                if (!serverHelloRI.isEmpty()) {
                    // abort the handshake with a fatal handshake_failure alert
                    fatalSE(Alerts.alert_handshake_failure,
                        "The renegotiation_info field is not empty");
                }

                secureRenegotiation = true;
            } else {
                // For a legacy renegotiation, the client MUST verify that
                // it does not contain the "renegotiation_info" extension.
                if (!secureRenegotiation) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "Unexpected renegotiation indication extension");
                }

                // verify the client_verify_data and server_verify_data values
                byte[] verifyData =
                    new byte[clientVerifyData.length + serverVerifyData.length];
                System.arraycopy(clientVerifyData, 0, verifyData,
                        0, clientVerifyData.length);
                System.arraycopy(serverVerifyData, 0, verifyData,
                        clientVerifyData.length, serverVerifyData.length);
                if (!Arrays.equals(verifyData,
                                serverHelloRI.getRenegotiatedConnection())) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "Incorrect verify data in ServerHello " +
                        "renegotiation_info message");
                }
            }
        } else {
            // no renegotiation indication extension
            if (isInitialHandshake) {
                if (!allowLegacyHelloMessages) {
                    // abort the handshake with a fatal handshake_failure alert
                    fatalSE(Alerts.alert_handshake_failure,
                        "Failed to negotiate the use of secure renegotiation");
                }

                secureRenegotiation = false;
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: No renegotiation " +
                                    "indication extension in ServerHello");
                }
            } else {
                // For a secure renegotiation, the client must abort the
                // handshake if no "renegotiation_info" extension is present.
                if (secureRenegotiation) {
                    fatalSE(Alerts.alert_handshake_failure,
                        "No renegotiation indication extension");
                }

                // we have already allowed unsafe renegotation before request
                // the renegotiation.
            }
        }

        //
        // Save server nonce, we always use it to compute connection
        // keys and it's also used to create the master secret if we're
        // creating a new session (i.e. in the full handshake).
        //
        svr_random = mesg.svr_random;

        if (isNegotiable(mesg.cipherSuite) == false) {
            fatalSE(Alerts.alert_illegal_parameter,
                "Server selected improper ciphersuite " + mesg.cipherSuite);
        }

        setCipherSuite(mesg.cipherSuite);
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            handshakeHash.setFinishedAlg(cipherSuite.prfAlg.getPRFHashAlg());
        }

        if (mesg.compression_method != 0) {
            fatalSE(Alerts.alert_illegal_parameter,
                "compression type not supported, "
                + mesg.compression_method);
            // NOTREACHED
        }

        // so far so good, let's look at the session
        if (session != null) {
            // we tried to resume, let's see what the server decided
            if (session.getSessionId().equals(mesg.sessionId)) {
                // server resumed the session, let's make sure everything
                // checks out

                // Verify that the session ciphers are unchanged.
                CipherSuite sessionSuite = session.getSuite();
                if (cipherSuite != sessionSuite) {
                    throw new SSLProtocolException
                        ("Server returned wrong cipher suite for session");
                }

                // verify protocol version match
                ProtocolVersion sessionVersion = session.getProtocolVersion();
                if (protocolVersion != sessionVersion) {
                    throw new SSLProtocolException
                        ("Server resumed session with wrong protocol version");
                }

                // validate subject identity
                if (sessionSuite.keyExchange == CipherSuite.KeyExchange.K_KRB5 ||
                    sessionSuite.keyExchange == CipherSuite.KeyExchange.K_KRB5_EXPORT) {
                    Principal localPrincipal = session.getLocalPrincipal();

                    Subject subject = null;
                    try {
                        subject = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Subject>() {
                            @Override
                            public Subject run() throws Exception {
                                return Krb5Helper.getClientSubject(getAccSE());
                            }});
                    } catch (PrivilegedActionException e) {
                        subject = null;
                        if (debug != null && Debug.isOn("session")) {
                            System.out.println("Attempt to obtain" +
                                        " subject failed!");
                        }
                    }

                    if (subject != null) {
                        // Eliminate dependency on KerberosPrincipal
                        Set<Principal> principals =
                            subject.getPrincipals(Principal.class);
                        if (!principals.contains(localPrincipal)) {
                            throw new SSLProtocolException("Server resumed" +
                                " session with wrong subject identity");
                        } else {
                            if (debug != null && Debug.isOn("session"))
                                System.out.println("Subject identity is same");
                        }
                    } else {
                        if (debug != null && Debug.isOn("session"))
                            System.out.println("Kerberos credentials are not" +
                                " present in the current Subject; check if " +
                                " javax.security.auth.useSubjectAsCreds" +
                                " system property has been set to false");
                        throw new SSLProtocolException
                            ("Server resumed session with no subject");
                    }
                }

                // looks fine; resume it, and update the state machine.
                resumingSession = true;
                state = HandshakeMessage.ht_finished - 1;
                calculateConnectionKeys(session.getMasterSecret());
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% Server resumed " + session);
                }
            } else {
                // we wanted to resume, but the server refused
                session = null;
                if (!enableNewSession) {
                    throw new SSLException
                        ("New session creation is disabled");
                }
            }
        }

        if (resumingSession && session != null) {
            setHandshakeSessionSE(session);
            return;
        }

        // check extensions
        for (HelloExtension ext : mesg.extensions.list()) {
            ExtensionType type = ext.type;
            if (type == ExtensionType.EXT_SERVER_NAME) {
                serverNamesAccepted = true;
            } else if ((type != ExtensionType.EXT_ELLIPTIC_CURVES)
                    && (type != ExtensionType.EXT_EC_POINT_FORMATS)
                    && (type != ExtensionType.EXT_SERVER_NAME)
                    && (type != ExtensionType.EXT_RENEGOTIATION_INFO)) {
                fatalSE(Alerts.alert_unsupported_extension,
                    "Server sent an unsupported extension: " + type);
            }
        }

        // Create a new session, we need to do the full handshake
        session = new SSLSessionImpl(protocolVersion, cipherSuite,
                            getLocalSupportedSignAlgs(),
                            mesg.sessionId, getHostSE(), getPortSE());
        session.setRequestedServerNames(requestedServerNames);
        setHandshakeSessionSE(session);
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("** " + cipherSuite);
        }
    }

    /*
     * Server's own key was either a signing-only key, or was too
     * large for export rules ... this message holds an ephemeral
     * RSA key to use for key exchange.
     */
    private void serverKeyExchange(HandshakeMessage.RSA_ServerKeyExchange mesg)
            throws IOException, GeneralSecurityException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (!mesg.verify(serverKey, clnt_random, svr_random)) {
            fatalSE(Alerts.alert_handshake_failure,
                "server key exchange invalid");
            // NOTREACHED
        }
        ephemeralServerKey = mesg.getPublicKey();
    }


    /*
     * Diffie-Hellman key exchange.  We save the server public key and
     * our own D-H algorithm object so we can defer key calculations
     * until after we've sent the client key exchange message (which
     * gives client and server some useful parallelism).
     */
    private void serverKeyExchange(HandshakeMessage.DH_ServerKeyExchange mesg)
            throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        dh = new DHCrypt(mesg.getModulus(), mesg.getBase(),
                                            sslContext.getSecureRandom());
        serverDH = mesg.getServerPublicKey();
    }

    private void serverKeyExchange(HandshakeMessage.ECDH_ServerKeyExchange mesg)
            throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        ECPublicKey key = mesg.getPublicKey();
        ecdh = new ECDHCrypt(key.getParams(), sslContext.getSecureRandom());
        ephemeralServerKey = key;
    }

    //TODO TODO sm2密钥协商，暂时不支持
    private void sm2ServerKeyExchange(HandshakeMessage.SM2_ServerKeyExchange var1) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            var1.print(System.out);
        }

//        ECPublicKey var2 = var1.getPublicKey();
//        this.ephemeralServerKey = var2;
    }

    /*
     * The server's "Hello Done" message is the client's sign that
     * it's time to do all the hard work.
     */
    private void serverHelloDone(HandshakeMessage.ServerHelloDone mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        /*
         * Always make sure the input has been digested before we
         * start emitting data, to ensure the hashes are correctly
         * computed for the Finished and CertificateVerify messages
         * which we send (here).
         */
        input.digestNow();

        /*
         * FIRST ... if requested, send an appropriate Certificate chain
         * to authenticate the client, and remember the associated private
         * key to sign the CertificateVerify message.
         */
        PrivateKey signingKey = null;

        //TODO TODO 国密双向认证还未实现 modify by ringo
        if (certRequest != null) {
            X509ExtendedKeyManager km = sslContext.getX509KeyManager();

            ArrayList<String> keytypesTmp = new ArrayList<>(4);

            for (int i = 0; i < certRequest.types.length; i++) {
                String typeName;

                switch (certRequest.types[i]) {
                case HandshakeMessage.CertificateRequest.cct_rsa_sign:
                    typeName = "RSA";
                    break;

                case HandshakeMessage.CertificateRequest.cct_dss_sign:
                    typeName = "DSA";
                    break;

                case HandshakeMessage.CertificateRequest.cct_ecdsa_sign:
                    // ignore if we do not have EC crypto available
                    typeName = JsseJce.isEcAvailable() ? "EC" : null;
                    break;

                // Fixed DH/ECDH client authentication not supported
                case HandshakeMessage.CertificateRequest.cct_rsa_fixed_dh:
                case HandshakeMessage.CertificateRequest.cct_dss_fixed_dh:
                case HandshakeMessage.CertificateRequest.cct_rsa_fixed_ecdh:
                case HandshakeMessage.CertificateRequest.cct_ecdsa_fixed_ecdh:
                // Any other values (currently not used in TLS)
                case HandshakeMessage.CertificateRequest.cct_rsa_ephemeral_dh:
                case HandshakeMessage.CertificateRequest.cct_dss_ephemeral_dh:
                default:
                    typeName = null;
                    break;
                }

                if ((typeName != null) && (!keytypesTmp.contains(typeName))) {
                    keytypesTmp.add(typeName);
                }
            }

            String alias = null;
            int keytypesTmpSize = keytypesTmp.size();
            if (keytypesTmpSize != 0) {
                String keytypes[] =
                        keytypesTmp.toArray(new String[keytypesTmpSize]);

                if (conn != null) {
                    alias = km.chooseClientAlias(keytypes,
                        certRequest.getAuthorities(), conn);
                } else {
                    alias = km.chooseEngineClientAlias(keytypes,
                        certRequest.getAuthorities(), engine);
                }
            }

            HandshakeMessage.CertificateMsg m1 = null;
            if (alias != null) {
                X509Certificate[] certs = km.getCertificateChain(alias);
                if ((certs != null) && (certs.length != 0)) {
                    PublicKey publicKey = certs[0].getPublicKey();
                    // for EC, make sure we use a supported named curve
                    if (publicKey instanceof ECPublicKey) {
                        ECParameterSpec params =
                            ((ECPublicKey)publicKey).getParams();
                        int index =
                            SupportedEllipticCurvesExtension.getCurveIndex(
                                params);
                        if (!SupportedEllipticCurvesExtension.isSupported(
                                index)) {
                            publicKey = null;
                        }
                    }
                    if (publicKey != null) {
                        m1 = new HandshakeMessage.CertificateMsg(certs);
                        signingKey = km.getPrivateKey(alias);
                        session.setLocalPrivateKey(signingKey);
                        session.setLocalCertificates(certs);
                    }
                }
            }
            if (m1 == null) {
                //
                // No appropriate cert was found ... report this to the
                // server.  For SSLv3, send the no_certificate alert;
                // TLS uses an empty cert chain instead.
                //
                if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
                    m1 = new HandshakeMessage.CertificateMsg(new X509Certificate [0]);
                } else {
                    warningSE(Alerts.alert_no_certificate);
                }
            }

            //
            // At last ... send any client certificate chain.
            //
            if (m1 != null) {
                if (debug != null && Debug.isOn("handshake")) {
                    m1.print(System.out);
                }
                m1.write(output);
            }
        }

        /*
         * SECOND ... send the client key exchange message.  The
         * procedure used is a function of the cipher suite selected;
         * one is always needed.
         */
        HandshakeMessage m2;

        switch (keyExchange) {

        case K_RSA:
        case K_RSA_EXPORT:
            if (serverKey == null) {
                throw new SSLProtocolException
                        ("Server did not send certificate message");
            }

            if (!(serverKey instanceof RSAPublicKey)) {
                throw new SSLProtocolException
                        ("Server certificate does not include an RSA key");
            }

            /*
             * For RSA key exchange, we randomly generate a new
             * pre-master secret and encrypt it with the server's
             * public key.  Then we save that pre-master secret
             * so that we can calculate the keying data later;
             * it's a performance speedup not to do that until
             * the client's waiting for the server response, but
             * more of a speedup for the D-H case.
             *
             * If the RSA_EXPORT scheme is active, when the public
             * key in the server certificate is less than or equal
             * to 512 bits in length, use the cert's public key,
             * otherwise, the ephemeral one.
             */
            PublicKey key;
            if (keyExchange == CipherSuite.KeyExchange.K_RSA) {
                key = serverKey;
            } else {    // K_RSA_EXPORT
                if (JsseJce.getRSAKeyLength(serverKey) <= 512) {
                    // extraneous ephemeralServerKey check done
                    // above in processMessage()
                    key = serverKey;
                } else {
                    if (ephemeralServerKey == null) {
                        throw new SSLProtocolException("Server did not send" +
                            " a RSA_EXPORT Server Key Exchange message");
                    }
                    key = ephemeralServerKey;
                }
            }

            m2 = new RSAClientKeyExchange(protocolVersion, maxProtocolVersion,
                                sslContext.getSecureRandom(), key);
            break;
        case K_DH_RSA:
        case K_DH_DSS:
            /*
             * For DH Key exchange, we only need to make sure the server
             * knows our public key, so we calculate the same pre-master
             * secret.
             *
             * For certs that had DH keys in them, we send an empty
             * handshake message (no key) ... we flag this case by
             * passing a null "dhPublic" value.
             *
             * Otherwise we send ephemeral DH keys, unsigned.
             */
            // if (useDH_RSA || useDH_DSS)
            m2 = new DHClientKeyExchange();
            break;
        case K_DHE_RSA:
        case K_DHE_DSS:
        case K_DH_ANON:
            if (dh == null) {
                throw new SSLProtocolException
                    ("Server did not send a DH Server Key Exchange message");
            }
            m2 = new DHClientKeyExchange(dh.getPublicKey());
            break;
        case K_ECDHE_RSA:
        case K_ECDHE_ECDSA:
        case K_ECDH_ANON:
            if (ecdh == null) {
                throw new SSLProtocolException
                    ("Server did not send a ECDH Server Key Exchange message");
            }
            m2 = new ECDHClientKeyExchange(ecdh.getPublicKey());
            break;
        case K_ECDH_RSA:
        case K_ECDH_ECDSA:
            if (serverKey == null) {
                throw new SSLProtocolException
                        ("Server did not send certificate message");
            }
            if (serverKey instanceof ECPublicKey == false) {
                throw new SSLProtocolException
                        ("Server certificate does not include an EC key");
            }
            ECParameterSpec params = ((ECPublicKey)serverKey).getParams();
            ecdh = new ECDHCrypt(params, sslContext.getSecureRandom());
            m2 = new ECDHClientKeyExchange(ecdh.getPublicKey());
            break;
        case K_SM2_SM2:
            //TODO TODO K_SM2_SM2暂不支持
//            if (this.protocolVersion.major != 1) {
//                throw new RuntimeException("gb tls protocol version major must be 1");
//            }
//
//            if (this.protocolVersion.minor == 0) {
//                this.ecdh = new ECDHCrypt(((ECPublicKey)this.ephemeralServerKey).getParams(), this.sslContext.getSecureRandom());
//                m2 = new ECDHClientKeyExchange(this.ecdh.getPublicKey());
//            } else {
//                if (this.protocolVersion.minor != 1) {
//                    throw new RuntimeException("unsupported protocol version");
//                }
//
//                this.sm2 = new SM2Crypt(this.encClientPublicKey, this.encClientPrivateKey, this.sslContext.getSecureRandom(), false);
//                m2 = new SM2ClientKeyExchange(this.sm2.getRPointEncoded());
//            }
            throw new IllegalStateException("K_SM2_SM2暂不支持");
        case K_ECC:
            if (this.encServerKey == null) {
                throw new SSLProtocolException("Server did not send certificate message");
            }

            if (!(this.encServerKey instanceof ECPublicKey)) {
                throw new SSLProtocolException("Server certificate does not include an RSA key");
            }

            PublicKey encServerKey = this.encServerKey;
            m2 = new ECCClientKeyExchange(this.protocolVersion, this.maxProtocolVersion, this.sslContext.getSecureRandom(), encServerKey);
            break;
        case K_KRB5:
        case K_KRB5_EXPORT:
            String sniHostname = null;
            for (SNIServerName serverName : requestedServerNames) {
                if (serverName instanceof SNIHostName) {
                    sniHostname = ((SNIHostName) serverName).getAsciiName();
                    break;
                }
            }

            KerberosClientKeyExchange kerberosMsg = null;
            if (sniHostname != null) {
                // use first requested SNI hostname
                try {
                    kerberosMsg = new KerberosClientKeyExchange(
                        sniHostname, getAccSE(), protocolVersion,
                        sslContext.getSecureRandom());
                } catch(IOException e) {
                    if (serverNamesAccepted) {
                        // server accepted requested SNI hostname,
                        // so it must be used
                        throw e;
                    }
                    // fallback to using hostname
                    if (debug != null && Debug.isOn("handshake")) {
                        System.out.println(
                            "Warning, cannot use Server Name Indication: "
                                + e.getMessage());
                    }
                }
            }

            if (kerberosMsg == null) {
                String hostname = getHostSE();
                if (hostname == null) {
                    throw new IOException("Hostname is required" +
                        " to use Kerberos cipher suites");
                }
                kerberosMsg = new KerberosClientKeyExchange(
                     hostname, getAccSE(), protocolVersion,
                     sslContext.getSecureRandom());
            }

            // Record the principals involved in exchange
            session.setPeerPrincipal(kerberosMsg.getPeerPrincipal());
            session.setLocalPrincipal(kerberosMsg.getLocalPrincipal());
            m2 = kerberosMsg;
            break;
        default:
            // somethings very wrong
            throw new RuntimeException
                                ("Unsupported key exchange: " + keyExchange);
        }
        if (debug != null && Debug.isOn("handshake")) {
            m2.print(System.out);
        }
        m2.write(output);


        /*
         * THIRD, send a "change_cipher_spec" record followed by the
         * "Finished" message.  We flush the messages we've queued up, to
         * get concurrency between client and server.  The concurrency is
         * useful as we calculate the master secret, which is needed both
         * to compute the "Finished" message, and to compute the keys used
         * to protect all records following the change_cipher_spec.
         */

        output.doHashes();
        output.flush();

        /*
         * We deferred calculating the master secret and this connection's
         * keying data; we do it now.  Deferring this calculation is good
         * from a performance point of view, since it lets us do it during
         * some time that network delays and the server's own calculations
         * would otherwise cause to be "dead" in the critical path.
         */
        SecretKey preMasterSecret;
        switch (keyExchange) {
        case K_RSA:
        case K_RSA_EXPORT:
            preMasterSecret = ((RSAClientKeyExchange)m2).preMaster;
            break;
        case K_KRB5:
        case K_KRB5_EXPORT:
            byte[] secretBytes =
                ((KerberosClientKeyExchange)m2).getUnencryptedPreMasterSecret();
            preMasterSecret = new SecretKeySpec(secretBytes,
                "TlsPremasterSecret");
            break;
        case K_DHE_RSA:
        case K_DHE_DSS:
        case K_DH_ANON:
            preMasterSecret = dh.getAgreedSecret(serverDH, true);
            break;
        case K_ECDHE_RSA:
        case K_ECDHE_ECDSA:
        case K_ECDH_ANON:
            preMasterSecret = ecdh.getAgreedSecret(ephemeralServerKey);
            break;
        case K_ECDH_RSA:
        case K_ECDH_ECDSA:
            preMasterSecret = ecdh.getAgreedSecret(serverKey);
            break;
        case K_SM2_SM2:
            //TODO TODO K_SM2_SM2暂不支持
//            if (this.protocolVersion.minor == 0) {
//                var20 = this.ecdh.getAgreedSecret(this.ephemeralServerKey);
//            } else {
//                if (this.protocolVersion.minor != 1) {
//                    throw new RuntimeException("unsupported protocol version");
//                }
//
//                this.sm2.setPeerPublicKey(this.encServerKey);
//                var20 = this.sm2.getAgreedSecret(this.ephemeralServerKey, this.encIdLocal, this.encIdRemote);
//            }

            throw new IllegalStateException("K_SM2_SM2暂不支持");
        case K_ECC:
            preMasterSecret = ((ECCClientKeyExchange)m2).preMaster;
            break;
        default:
            throw new IOException("Internal error: unknown key exchange "
                + keyExchange);
        }

        calculateKeys(preMasterSecret, null);

        /*
         * FOURTH, if we sent a Certificate, we need to send a signed
         * CertificateVerify (unless the key in the client's certificate
         * was a Diffie-Hellman key).).
         *
         * This uses a hash of the previous handshake messages ... either
         * a nonfinal one (if the particular implementation supports it)
         * or else using the third element in the arrays of hashes being
         * computed.
         */
        if (signingKey != null) {
            HandshakeMessage.CertificateVerify m3;
            try {
                SignatureAndHashAlgorithm preferableSignatureAlgorithm = null;
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    preferableSignatureAlgorithm =
                        SignatureAndHashAlgorithm.getPreferableAlgorithm(
                            peerSupportedSignAlgs, signingKey.getAlgorithm(),
                            signingKey);

                    if (preferableSignatureAlgorithm == null) {
                        throw new SSLHandshakeException(
                            "No supported signature algorithm");
                    }

                    String hashAlg =
                        SignatureAndHashAlgorithm.getHashAlgorithmName(
                                preferableSignatureAlgorithm);
                    if (hashAlg == null || hashAlg.length() == 0) {
                        throw new SSLHandshakeException(
                                "No supported hash algorithm");
                    }
                }

                m3 = new HandshakeMessage.CertificateVerify(protocolVersion, handshakeHash,
                    signingKey, session.getMasterSecret(),
                    sslContext.getSecureRandom(),
                    preferableSignatureAlgorithm);
            } catch (GeneralSecurityException e) {
                fatalSE(Alerts.alert_handshake_failure,
                    "Error signing certificate verify", e);
                // NOTREACHED, make compiler happy
                m3 = null;
            }
            if (debug != null && Debug.isOn("handshake")) {
                m3.print(System.out);
            }
            m3.write(output);
            output.doHashes();
        }

        /*
         * OK, that's that!
         */
        sendChangeCipherAndFinish(false);
    }


    /*
     * "Finished" is the last handshake message sent.  If we got this
     * far, the MAC has been validated post-decryption.  We validate
     * the two hashes here as an additional sanity check, protecting
     * the handshake against various active attacks.
     */
    private void serverFinished(HandshakeMessage.Finished mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }

        boolean verified = mesg.verify(handshakeHash, HandshakeMessage.Finished.SERVER,
            session.getMasterSecret());

        if (!verified) {
            fatalSE(Alerts.alert_illegal_parameter,
                       "server 'finished' message doesn't verify");
            // NOTREACHED
        }

        /*
         * save server verify data for secure renegotiation
         */
        if (secureRenegotiation) {
            serverVerifyData = mesg.getVerifyData();
        }

        /*
         * OK, it verified.  If we're doing the fast handshake, add that
         * "Finished" message to the hash of handshake messages, then send
         * our own change_cipher_spec and Finished message for the server
         * to verify in turn.  These are the last handshake messages.
         *
         * In any case, update the session cache.  We're done handshaking,
         * so there are no threats any more associated with partially
         * completed handshakes.
         */
        if (resumingSession) {
            input.digestNow();
            sendChangeCipherAndFinish(true);
        }
        session.setLastAccessedTime(System.currentTimeMillis());

        if (!resumingSession) {
            if (session.isRejoinable()) {
                ((SSLSessionContextImpl) sslContext
                        .engineGetClientSessionContext())
                        .put(session);
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% Cached client session: " + session);
                }
            } else if (debug != null && Debug.isOn("session")) {
                System.out.println(
                    "%% Didn't cache non-resumable client session: "
                    + session);
            }
        }
    }


    /*
     * Send my change-cipher-spec and Finished message ... done as the
     * last handshake act in either the short or long sequences.  In
     * the short one, we've already seen the server's Finished; in the
     * long one, we wait for it now.
     */
    private void sendChangeCipherAndFinish(boolean finishedTag)
            throws IOException {
        HandshakeMessage.Finished mesg = new HandshakeMessage.Finished(protocolVersion, handshakeHash,
            HandshakeMessage.Finished.CLIENT, session.getMasterSecret(), cipherSuite);

        /*
         * Send the change_cipher_spec message, then the Finished message
         * which we just calculated (and protected using the keys we just
         * calculated).  Server responds with its Finished message, except
         * in the "fast handshake" (resume session) case.
         */
        sendChangeCipherSpec(mesg, finishedTag);

        /*
         * save client verify data for secure renegotiation
         */
        if (secureRenegotiation) {
            clientVerifyData = mesg.getVerifyData();
        }

        /*
         * Update state machine so server MUST send 'finished' next.
         * (In "long" handshake case; in short case, we're responding
         * to its message.)
         */
        state = HandshakeMessage.ht_finished - 1;
    }


    /*
     * Returns a ClientHello message to kickstart renegotiations
     */
    @Override
    HandshakeMessage getKickstartMessage() throws SSLException {
        // session ID of the ClientHello message
        SessionId sessionId = SSLSessionImpl.nullSession.getSessionId();

        // a list of cipher suites sent by the client
        CipherSuiteList cipherSuites = getActiveCipherSuites();

        // set the max protocol version this client is supporting.
        maxProtocolVersion = protocolVersion;

        //
        // Try to resume an existing session.  This might be mandatory,
        // given certain API options.
        //
        session = ((SSLSessionContextImpl)sslContext
                        .engineGetClientSessionContext())
                        .get(getHostSE(), getPortSE());
        if (debug != null && Debug.isOn("session")) {
            if (session != null) {
                System.out.println("%% Client cached "
                    + session
                    + (session.isRejoinable() ? "" : " (not rejoinable)"));
            } else {
                System.out.println("%% No cached client session");
            }
        }
        if ((session != null) && (session.isRejoinable() == false)) {
            session = null;
        }

        if (session != null) {
            CipherSuite sessionSuite = session.getSuite();
            ProtocolVersion sessionVersion = session.getProtocolVersion();
            if (isNegotiable(sessionSuite) == false) {
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% can't resume, unavailable cipher");
                }
                session = null;
            }

            if ((session != null) && !isNegotiable(sessionVersion)) {
                if (debug != null && Debug.isOn("session")) {
                    System.out.println("%% can't resume, protocol disabled");
                }
                session = null;
            }

            if (session != null) {
                if (debug != null) {
                    if (Debug.isOn("handshake") || Debug.isOn("session")) {
                        System.out.println("%% Try resuming " + session
                            + " from port " + getLocalPortSE());
                    }
                }

                sessionId = session.getSessionId();
                maxProtocolVersion = sessionVersion;

                // Update SSL version number in underlying SSL socket and
                // handshake output stream, so that the output records (at the
                // record layer) have the correct version
                setVersion(sessionVersion);
            }

            /*
             * Force use of the previous session ciphersuite, and
             * add the SCSV if enabled.
             */
            if (!enableNewSession) {
                if (session == null) {
                    throw new SSLHandshakeException(
                        "Can't reuse existing SSL client session");
                }

                Collection<CipherSuite> cipherList = new ArrayList<>(2);
                cipherList.add(sessionSuite);
                if (!secureRenegotiation &&
                        cipherSuites.contains(CipherSuite.C_SCSV)) {
                    cipherList.add(CipherSuite.C_SCSV);
                }   // otherwise, renegotiation_info extension will be used

                cipherSuites = new CipherSuiteList(cipherList);
            }
        }

        if (session == null && !enableNewSession) {
            throw new SSLHandshakeException("No existing session to resume");
        }

        // exclude SCSV for secure renegotiation
        if (secureRenegotiation && cipherSuites.contains(CipherSuite.C_SCSV)) {
            Collection<CipherSuite> cipherList =
                        new ArrayList<>(cipherSuites.size() - 1);
            for (CipherSuite suite : cipherSuites.collection()) {
                if (suite != CipherSuite.C_SCSV) {
                    cipherList.add(suite);
                }
            }

            cipherSuites = new CipherSuiteList(cipherList);
        }

        // make sure there is a negotiable cipher suite.
        boolean negotiable = false;
        for (CipherSuite suite : cipherSuites.collection()) {
            if (isNegotiable(suite)) {
                negotiable = true;
                break;
            }
        }

        if (!negotiable) {
            throw new SSLHandshakeException("No negotiable cipher suite");
        }

        // Not a TLS1.2+ handshake
        // For SSLv2Hello, HandshakeHash.reset() will be called, so we
        // cannot call HandshakeHash.protocolDetermined() here. As it does
        // not follow the spec that HandshakeHash.reset() can be only be
        // called before protocolDetermined.
        // if (maxProtocolVersion.v < ProtocolVersion.TLS12.v) {
        //     handshakeHash.protocolDetermined(maxProtocolVersion);
        // }

        // create the ClientHello message
        HandshakeMessage.ClientHello clientHelloMessage = new HandshakeMessage.ClientHello(
                sslContext.getSecureRandom(), maxProtocolVersion,
                sessionId, cipherSuites);

        // add signature_algorithm extension
        if (maxProtocolVersion.v >= ProtocolVersion.TLS12.v) {
            // we will always send the signature_algorithm extension
            Collection<SignatureAndHashAlgorithm> localSignAlgs =
                                                getLocalSupportedSignAlgs();
            if (localSignAlgs.isEmpty()) {
                throw new SSLHandshakeException(
                            "No supported signature algorithm");
            }

            clientHelloMessage.addSignatureAlgorithmsExtension(localSignAlgs);
        }

        // add server_name extension
        if (enableSNIExtension) {
            if (session != null) {
                requestedServerNames = session.getRequestedServerNames();
            } else {
                requestedServerNames = serverNames;
            }

            if (!requestedServerNames.isEmpty()) {
                clientHelloMessage.addSNIExtension(requestedServerNames);
            }
        }

        // reset the client random cookie
        clnt_random = clientHelloMessage.clnt_random;

        /*
         * need to set the renegotiation_info extension for:
         * 1: secure renegotiation
         * 2: initial handshake and no SCSV in the ClientHello
         * 3: insecure renegotiation and no SCSV in the ClientHello
         */
        if (secureRenegotiation ||
                !cipherSuites.contains(CipherSuite.C_SCSV)) {
            clientHelloMessage.addRenegotiationInfoExtension(clientVerifyData);
        }

        return clientHelloMessage;
    }

    /*
     * Fault detected during handshake.
     */
    @Override
    void handshakeAlert(byte description) throws SSLProtocolException {
        String message = Alerts.alertDescription(description);

        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("SSL - handshake alert: " + message);
        }
        throw new SSLProtocolException("handshake alert:  " + message);
    }

    /*
     * Unless we are using an anonymous ciphersuite, the server always
     * sends a certificate message (for the CipherSuites we currently
     * support). The trust manager verifies the chain for us.
     */
    private void serverCertificate(HandshakeMessage.CertificateMsg mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        X509Certificate[] peerCerts = mesg.getCertificateChain();
        if (peerCerts.length == 0) {
            fatalSE(Alerts.alert_bad_certificate,
                "empty certificate chain");
        }
        // ask the trust manager to verify the chain
        X509TrustManager tm = sslContext.getX509TrustManager();
        try {
            // find out the key exchange algorithm used
            // use "RSA" for non-ephemeral "RSA_EXPORT"
            String keyExchangeString;
            if (keyExchange == CipherSuite.KeyExchange.K_RSA_EXPORT && !serverKeyExchangeReceived) {
                keyExchangeString = CipherSuite.KeyExchange.K_RSA.name;
            } else {
                keyExchangeString = keyExchange.name;
            }

            if (tm instanceof X509ExtendedTrustManager) {
                //TODO 证书链验证，没测试过可能出问题
                if (protocolVersion.isIsgm()){
                    X509Certificate[] peerCertsTest;
                    switch (peerCerts.length){
                        case 0:
                            // TODO 没有证书，不归我们处理 modify by ringo
                        case 1:
                            // TODO 一个证书，单证书，CA证书在trust manager里 modify by ringo
                            peerCertsTest = peerCerts;
                            break;
                        case 2:
                            //TODO 两个证书，第一个为签名证书，第二个为加密证书 modify by ringo
                            peerCertsTest = new X509Certificate[]{peerCerts[0]};
                            break;
                        default:
                            //TODO 多个证书，第一个为签名证书，第二个为加密证书,之后为签名证书的证书链 modify by ringo
                            // 证书链跳过第二个证书
                            X509Certificate[] peerCertsTestTemp = new X509Certificate[peerCerts.length - 1];
                            peerCertsTest = peerCerts.clone();
                            peerCertsTestTemp[0] = peerCertsTest[0];
                            System.arraycopy(peerCertsTest, 2,
                                    peerCertsTestTemp, 1, peerCerts.length - 2);
                            peerCertsTest = peerCertsTestTemp;
                            break;
                    }
                    if (conn != null) {
                        ((X509ExtendedTrustManager)tm).checkServerTrusted(
                                peerCertsTest.clone(),
                                keyExchangeString,
                                conn);
                    } else {
                        ((X509ExtendedTrustManager)tm).checkServerTrusted(
                                peerCertsTest.clone(),
                                keyExchangeString,
                                engine);
                    }
                }else {
                    if (conn != null) {
                        ((X509ExtendedTrustManager)tm).checkServerTrusted(
                                peerCerts.clone(),
                                keyExchangeString,
                                conn);
                    } else {
                        ((X509ExtendedTrustManager)tm).checkServerTrusted(
                                peerCerts.clone(),
                                keyExchangeString,
                                engine);
                    }
                }
            } else {
                // Unlikely to happen, because we have wrapped the old
                // X509TrustManager with the new X509ExtendedTrustManager.
                throw new CertificateException(
                    "Improper X509TrustManager implementation");
            }
        } catch (CertificateException e) {
            // This will throw an exception, so include the original error.
            fatalSE(Alerts.alert_certificate_unknown, e);
        }
        session.setPeerCertificates(peerCerts);
    }
}
