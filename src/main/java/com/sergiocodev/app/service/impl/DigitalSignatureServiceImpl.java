package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.config.SunatConfig;
import com.sergiocodev.app.service.interfaces.DigitalSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalSignatureServiceImpl implements DigitalSignatureService {

    private final SunatConfig sunatConfig;

    @Override
    public String signXml(String xmlContent) {
        try {
            if (sunatConfig.getCertificatePath() == null || sunatConfig.getCertificatePath().isEmpty()) {
                log.warn("Ruta del certificado digital no configurada. Saltando firma digital real.");
                return xmlContent;
            }

            // Cargar el KeyStore (certificado .p12 o .pfx)
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(sunatConfig.getCertificatePath())) {
                ks.load(fis, sunatConfig.getCertificatePassword().toCharArray());
            } catch (Exception e) {
                log.error("No se pudo cargar el certificado digital en la ruta: {}", sunatConfig.getCertificatePath());
                return xmlContent;
            }

            String alias = ks.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, sunatConfig.getCertificatePassword().toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            // Parsear el XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));

            // Preparar el motor de firma XML
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),
                    Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null, null);
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

            // Agregar KeyInfo con el certificado
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            List<Object> x509Content = new ArrayList<>();
            x509Content.add(cert);
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            // Buscar el nodo ExtensionContent
            NodeList extensionContents = doc.getElementsByTagName("ext:ExtensionContent");
            if (extensionContents.getLength() == 0) {
                log.warn("El nodo ext:ExtensionContent no se encontro en el XML. La firma podria no ser valida para SUNAT.");
                return xmlContent;
            }
            Element extensionContent = (Element) extensionContents.item(0);

            // Firmar el documento incrustando la firma en ExtensionContent
            DOMSignContext dsc = new DOMSignContext(privateKey, extensionContent);
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            // Convertir el documento firmado a String
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();

        } catch (Exception e) {
            log.error("Error firmando el XML: ", e);
            // Retorna el original si falla para no romper todo el flujo en pruebas
            return xmlContent;
        }
    }
}
