const RSA_IMPORT_ALGORITHM = Object.freeze({
    name: "RSASSA-PKCS1-v1_5",
    hash: "SHA-256",
});

function requireWebCrypto() {
    if (!window.crypto?.subtle) {
        throw new Error(
            "This browser does not support secure RSA contract signing."
        );
    }
    return window.crypto.subtle;
}

function decodePem(pem, label) {
    const value = String(pem || "").trim();
    const header = `-----BEGIN ${label}-----`;
    const footer = `-----END ${label}-----`;
    if (!value.includes(header) || !value.includes(footer)) {
        throw new Error(`${label} must use PEM format.`);
    }

    const encoded = value
        .replace(header, "")
        .replace(footer, "")
        .replaceAll(/\s/g, "");
    try {
        const binary = window.atob(encoded);
        return Uint8Array.from(binary, (character) => character.charCodeAt(0));
    } catch {
        throw new Error(`${label} contains invalid Base64 data.`);
    }
}

function encodeBase64(content) {
    const bytes = content instanceof Uint8Array
        ? content
        : new Uint8Array(content);
    let binary = "";
    const chunkSize = 0x8000;
    for (let offset = 0; offset < bytes.length; offset += chunkSize) {
        binary += String.fromCharCode(
            ...bytes.subarray(offset, offset + chunkSize)
        );
    }
    return window.btoa(binary);
}

function encodePem(content, label) {
    const encoded = encodeBase64(content);
    const lines = encoded.match(/.{1,64}/g) || [];
    return [
        `-----BEGIN ${label}-----`,
        ...lines,
        `-----END ${label}-----`,
    ].join("\n");
}

export async function generateRsaSigningKeys() {
    const subtle = requireWebCrypto();
    const keyPair = await subtle.generateKey(
        {
            name: RSA_IMPORT_ALGORITHM.name,
            modulusLength: 2048,
            publicExponent: new Uint8Array([1, 0, 1]),
            hash: RSA_IMPORT_ALGORITHM.hash,
        },
        true,
        ["sign", "verify"]
    );
    const [privateKey, publicKey] = await Promise.all([
        subtle.exportKey("pkcs8", keyPair.privateKey),
        subtle.exportKey("spki", keyPair.publicKey),
    ]);

    return {
        privateKey: encodePem(privateKey, "PRIVATE KEY"),
        publicKey: encodePem(publicKey, "PUBLIC KEY"),
    };
}

export function downloadPrivateKeyPem(privateKeyPem, signatureName) {
    const value = String(privateKeyPem || "").trim();
    if (!value.includes("-----BEGIN PRIVATE KEY-----")) {
        throw new Error("A PKCS#8 private key is required for download.");
    }
    const safeName = String(signatureName || "electronic-signature")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-zA-Z0-9_-]+/g, "-")
        .replace(/^-+|-+$/g, "")
        .toLowerCase() || "electronic-signature";
    const blob = new Blob([`${value}\n`], {
        type: "application/x-pem-file",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${safeName}-rsa-private-key.pem`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1_000);
}

export async function signApprovedContractPdf(
    pdfContent,
    privateKeyPem,
    publicKeyPem
) {
    const subtle = requireWebCrypto();
    const privateKeyBytes = decodePem(privateKeyPem, "PRIVATE KEY");
    const publicKeyBytes = decodePem(publicKeyPem, "PUBLIC KEY");

    let privateKey;
    let publicKey;
    try {
        [privateKey, publicKey] = await Promise.all([
            subtle.importKey(
                "pkcs8",
                privateKeyBytes,
                RSA_IMPORT_ALGORITHM,
                false,
                ["sign"]
            ),
            subtle.importKey(
                "spki",
                publicKeyBytes,
                RSA_IMPORT_ALGORITHM,
                false,
                ["verify"]
            ),
        ]);
    } catch {
        throw new Error(
            "Use an RSA PKCS#8 private key and an X.509/SPKI public key."
        );
    }

    const bytes = pdfContent instanceof ArrayBuffer
        ? pdfContent
        : await pdfContent.arrayBuffer();
    const digitalSignature = await subtle.sign(
        RSA_IMPORT_ALGORITHM,
        privateKey,
        bytes
    );
    const verified = await subtle.verify(
        RSA_IMPORT_ALGORITHM,
        publicKey,
        digitalSignature,
        bytes
    );
    if (!verified) {
        throw new Error(
            "The private key and public key do not belong to the same RSA key pair."
        );
    }

    return {
        digitalSignature: encodeBase64(digitalSignature),
    };
}
