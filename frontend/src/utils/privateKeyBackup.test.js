import test from "node:test";
import assert from "node:assert/strict";
import { unlockBackup } from "./privateKeyBackup.js";

const algorithm = { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048,
    publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" };
const pair = await crypto.subtle.generateKey(algorithm, true, ["sign", "verify"]);
const base64 = (data) => Buffer.from(data).toString("base64");
const privateText = base64(await crypto.subtle.exportKey("pkcs8", pair.privateKey));
const registered = { available: true, keyCode: "012345",
    publicKey: base64(await crypto.subtle.exportKey("spki", pair.publicKey)) };
const salt = crypto.getRandomValues(new Uint8Array(16));
const iv = crypto.getRandomValues(new Uint8Array(12));
const material = await crypto.subtle.importKey("raw", new TextEncoder().encode("123456"),
    "PBKDF2", false, ["deriveKey"]);
const aes = await crypto.subtle.deriveKey({ name: "PBKDF2", salt,
    iterations: 600000, hash: "SHA-256" }, material,
    { name: "AES-GCM", length: 256 }, false, ["encrypt"]);
const backup = { version: 1, keyAlgorithm: "RSA", keySize: 2048,
    encryption: "AES-GCM", kdf: "PBKDF2", iterations: 600000,
    salt: base64(salt), iv: base64(iv),
    encryptedPrivateKey: base64(await crypto.subtle.encrypt({ name: "AES-GCM", iv },
        aes, new TextEncoder().encode(privateText))) };
const file = (data = backup, name = "private-key-012345.enc") =>
    new File([typeof data === "string" ? data : JSON.stringify(data)], name);

test("restores an existing-format backup and preserves encrypted storage data", async () => {
    const result = await unlockBackup(file(), "123456", "012345", registered);
    assert.equal(result.privateKeyText, privateText);
    assert.deepEqual(result.backup, backup);
    assert.equal(JSON.stringify(result.backup).includes(privateText), false);
});
test("rejects incorrect PIN", async () => {
    await assert.rejects(unlockBackup(file(), "654321", "012345", registered), /Incorrect PIN/);
});
test("rejects a different private key even with the correct PIN", async () => {
    const other = await crypto.subtle.generateKey(algorithm, true, ["sign", "verify"]);
    await assert.rejects(unlockBackup(file(), "123456", "012345", {
        ...registered, publicKey: base64(await crypto.subtle.exportKey("spki", other.publicKey)),
    }), /does not match/);
});
test("rejects damaged encrypted data", async () => {
    const bytes = Buffer.from(backup.encryptedPrivateKey, "base64");
    bytes[0] ^= 1;
    await assert.rejects(unlockBackup(file({ ...backup, encryptedPrivateKey: base64(bytes) }),
        "123456", "012345", registered), /damaged/);
});
test("rejects malformed JSON and unsupported iteration counts", async () => {
    await assert.rejects(unlockBackup(file("{"), "123456", "012345", registered), /invalid/);
    await assert.rejects(unlockBackup(file({ ...backup, iterations: 999999999 }),
        "123456", "012345", registered), /unsupported/);
});
test("rejects incorrect code, extension and oversized file", async () => {
    await assert.rejects(unlockBackup(file(), "123456", "999999", registered), /key code/);
    await assert.rejects(unlockBackup(file(backup, "backup.txt"), "123456", "012345", registered), /\.enc/);
    await assert.rejects(unlockBackup(file("x".repeat(65537)), "123456", "012345", registered), /64 KB/);
});
