const decode = (value) => Uint8Array.from(atob(value), (char) => char.charCodeAt(0));

// Validate and prove ownership before the caller persists anything.
export async function unlockBackup(file, pin, keyCode, registeredKey) {
    if (!/^[0-9]{6}$/.test(pin) || !/^[0-9]{6}$/.test(keyCode)) {
        throw new Error("Enter your 6-digit public key code and PIN first.");
    }
    if (!file || !/\.enc$/i.test(file.name) || file.size > 65536) {
        throw new Error("Choose a private key .enc backup smaller than 64 KB.");
    }
    if (!registeredKey?.available || registeredKey.keyCode !== keyCode) {
        throw new Error("This key code does not match your registered signing key.");
    }
    let backup, salt, iv, encrypted;
    try {
        backup = JSON.parse(await file.text());
        if (backup.version !== 1 || backup.keyAlgorithm !== "RSA" ||
            backup.keySize !== 2048 || backup.encryption !== "AES-GCM" ||
            backup.kdf !== "PBKDF2" || backup.iterations !== 600000) throw new Error();
        salt = decode(backup.salt);
        iv = decode(backup.iv);
        encrypted = decode(backup.encryptedPrivateKey);
        if (salt.length !== 16 || iv.length !== 12 || encrypted.length < 17) throw new Error();
    } catch {
        throw new Error("The private key backup is invalid or unsupported.");
    }
    const material = await crypto.subtle.importKey("raw", new TextEncoder().encode(pin),
        "PBKDF2", false, ["deriveKey"]);
    const aes = await crypto.subtle.deriveKey({
        name: "PBKDF2", salt, iterations: 600000, hash: "SHA-256",
    }, material, { name: "AES-GCM", length: 256 }, false, ["decrypt"]);
    let privateKeyText;
    try {
        privateKeyText = new TextDecoder().decode(await crypto.subtle.decrypt(
            { name: "AES-GCM", iv }, aes, encrypted));
    } catch {
        throw new Error("Incorrect PIN or damaged private key backup.");
    }
    try {
        const algorithm = { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" };
        const privateKey = await crypto.subtle.importKey("pkcs8", decode(privateKeyText),
            algorithm, false, ["sign"]);
        const publicKey = await crypto.subtle.importKey("spki", decode(registeredKey.publicKey),
            algorithm, false, ["verify"]);
        const challenge = crypto.getRandomValues(new Uint8Array(32));
        const signature = await crypto.subtle.sign(algorithm, privateKey, challenge);
        if (!await crypto.subtle.verify(algorithm, publicKey, signature, challenge)) throw new Error();
    } catch {
        throw new Error("This backup does not match your registered public key.");
    }
    return { privateKeyText, backup: {
        version: 1, keyAlgorithm: "RSA", keySize: 2048, encryption: "AES-GCM",
        kdf: "PBKDF2", iterations: 600000, salt: backup.salt, iv: backup.iv,
        encryptedPrivateKey: backup.encryptedPrivateKey,
    } };
}
