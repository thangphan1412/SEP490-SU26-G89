import { useEffect, useState } from "react";
import electronicSignatureService
    from "../../services/signatureService/electronicSignatureService.js";
import "../../assets/styles/css/signature/SignatureKeyFields.css";

const decodeBase64 = (value) =>
    Uint8Array.from(
        atob(value),
        (character) => character.charCodeAt(0)
    );

const encodeBase64 = (value) =>
    btoa(
        String.fromCharCode(
            ...new Uint8Array(value)
        )
    );

async function unlockPrivateKey(backup, pin) {
    if (!/^\d{6}$/.test(pin)) {
        throw new Error(
            "Enter the 6-digit PIN used to protect your private key."
        );
    }

    if (
        backup.version !== 1 ||
        backup.encryption !== "AES-GCM" ||
        backup.kdf !== "PBKDF2" ||
        backup.iterations !== 600000
    ) {
        throw new Error(
            "This private key backup format is not supported."
        );
    }

    const material = await crypto.subtle.importKey(
        "raw",
        new TextEncoder().encode(pin),
        "PBKDF2",
        false,
        ["deriveKey"]
    );

    const key = await crypto.subtle.deriveKey(
        {
            name: "PBKDF2",
            salt: decodeBase64(backup.salt),
            iterations: backup.iterations,
            hash: "SHA-256"
        },
        material,
        {
            name: "AES-GCM",
            length: 256
        },
        false,
        ["decrypt"]
    );

    try {
        const decrypted = await crypto.subtle.decrypt(
            {
                name: "AES-GCM",
                iv: decodeBase64(backup.iv)
            },
            key,
            decodeBase64(backup.encryptedPrivateKey)
        );

        return new TextDecoder().decode(decrypted);

    } catch (cause) {

        throw new Error(
            "Incorrect PIN or damaged private key backup.",
            { cause }
        );
    }
}

export default function SignatureKeyVerification({
                                                     signatureId,
                                                     onVerified,
                                                     saving
                                                 }) {

    const [keyCode, setKeyCode] = useState("");
    const [pin, setPin] = useState("");

    const [publicKey, setPublicKey] = useState("");

    const [verifying, setVerifying] = useState(false);
    const [verified, setVerified] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {

        if (!verified) {
            return;
        }

        const timeout = setTimeout(() => {

            setVerified(null);
            setPublicKey("");

            onVerified(null);

            setError(
                "Key verification expired. Please verify again before saving."
            );

        }, Math.max(
            0,
            Date.parse(verified.expiresAt) - Date.now()
        ));

        return () => clearTimeout(timeout);

    }, [verified, onVerified]);


    const invalidate = () => {

        setVerified(null);
        setPublicKey("");
        setError("");

        onVerified(null);
    };


    const verify = async () => {

        if (verifying || saving) {
            return;
        }

        invalidate();
        setVerifying(true);

        try {

            /*
             * 1. Validate Public Key Code
             */

            if (!/^\d{6}$/.test(keyCode)) {

                throw new Error(
                    "Public key code must contain exactly 6 digits."
                );
            }


            /*
             * 2. Validate PIN
             */

            if (!/^\d{6}$/.test(pin)) {

                throw new Error(
                    "PIN must contain exactly 6 digits."
                );
            }


            /*
             * 3. Get encrypted private key
             *    from browser localStorage
             */

            const storageKey =
                `encryptedPrivateKey_${keyCode}`;

            const stored =
                localStorage.getItem(storageKey);

            if (!stored) {

                throw new Error(
                    "Encrypted private key was not found for this public key code."
                );
            }


            /*
             * 4. Parse encrypted backup
             */

            let backup;

            try {

                backup = JSON.parse(stored);

            } catch (cause) {

                throw new Error(
                    "The encrypted private key backup is invalid.",
                    { cause }
                );
            }


            /*
             * 5. Decrypt private key using PIN
             */

            let rawKey =
                await unlockPrivateKey(
                    backup,
                    pin
                );


            /*
             * 6. Remove PEM headers / whitespace
             */

            rawKey = rawKey
                .replace(
                    /-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\s/g,
                    ""
                );


            /*
             * 7. Import RSA private key
             */

            let signingKey;

            try {

                signingKey =
                    await crypto.subtle.importKey(
                        "pkcs8",
                        decodeBase64(rawKey),
                        {
                            name: "RSASSA-PKCS1-v1_5",
                            hash: "SHA-256"
                        },
                        false,
                        ["sign"]
                    );

            } catch (cause) {

                throw new Error(
                    "Unable to import the decrypted RSA private key.",
                    { cause }
                );
            }


            /*
             * 8. Ask backend for verification challenge
             */

            const response =
                await electronicSignatureService
                    .createUpdateChallenge(
                        signatureId,
                        keyCode
                    );


            const challenge =
                response.data.data;


            /*
             * 9. Import registered public key
             *    returned by backend
             */

            const verifyingKey =
                await crypto.subtle.importKey(
                    "spki",
                    decodeBase64(
                        challenge.publicKey
                    ),
                    {
                        name: "RSASSA-PKCS1-v1_5",
                        hash: "SHA-256"
                    },
                    false,
                    ["verify"]
                );


            /*
             * 10. Convert challenge to bytes
             */

            const bytes =
                new TextEncoder().encode(
                    challenge.challenge
                );


            /*
             * 11. Sign challenge using private key
             */

            const signed =
                await crypto.subtle.sign(
                    "RSASSA-PKCS1-v1_5",
                    signingKey,
                    bytes
                );


            /*
             * 12. Local verification
             *
             * Make sure the private key and
             * registered public key belong
             * to the same key pair.
             */

            const localValid =
                await crypto.subtle.verify(
                    "RSASSA-PKCS1-v1_5",
                    verifyingKey,
                    signed,
                    bytes
                );


            if (!localValid) {

                throw new Error(
                    "The private key does not match your registered public key."
                );
            }


            /*
             * 13. Create verification proof
             */

            const proof = {

                signatureId,

                challengeId:
                challenge.challengeId,

                signature:
                    encodeBase64(signed),

                expiresAt:
                challenge.expiresAt
            };


            /*
             * 14. Verification success
             */

            setVerified(proof);

            setPublicKey(
                challenge.publicKey
            );

            setPin("");

            onVerified(proof);

        } catch (exception) {

            console.error(
                "KEY VERIFICATION ERROR:",
                exception
            );

            setError(
                exception?.response?.data?.message ||
                exception?.message ||
                "Unable to verify your signing keys."
            );

        } finally {

            setVerifying(false);
        }
    };


    return (
        <section
            className="signature-key-fields"
            aria-label="Verify signing keys"
        >

            <h2>
                Verify your signing keys
            </h2>

            <p>
                Enter your public key code and PIN
                to verify ownership of your signing key.
                Your PIN and private key stay in this browser.
            </p>


            <fieldset
                disabled={
                    saving ||
                    verifying
                }
            >

                <div className="signature-key-grid">


                    {/* Public Key Code */}

                    <label className="signature-key-field">

                        Public key code

                        <input
                            className="form-control"
                            type="text"
                            value={keyCode}
                            inputMode="numeric"
                            maxLength={6}
                            onChange={(event) => {

                                invalidate();

                                setKeyCode(
                                    event.target.value
                                        .replace(/\D/g, "")
                                );

                            }}
                            placeholder="Enter 6-digit key code"
                        />

                    </label>


                    {/* PIN */}

                    <label className="signature-key-field">

                        Private key PIN

                        <input
                            className="form-control"
                            type="password"
                            autoComplete="off"
                            value={pin}
                            inputMode="numeric"
                            maxLength={6}
                            onChange={(event) => {

                                invalidate();

                                setPin(
                                    event.target.value
                                        .replace(/\D/g, "")
                                );

                            }}
                            placeholder="Enter 6-digit PIN"
                        />

                    </label>

                </div>


                <button
                    className="btn btn-outline-primary mt-3"
                    type="button"
                    onClick={verify}
                    disabled={
                        verifying ||
                        saving
                    }
                >

                    {verifying
                        ? "Verifying..."
                        : "Verify key pair"
                    }

                </button>

            </fieldset>


            {/* Error */}

            {error && (

                <div
                    className="alert alert-danger mt-3"
                    role="alert"
                >
                    {error}
                </div>

            )}


            {/* Success */}

            {verified && (

                <div
                    className="alert alert-success mt-3"
                    role="status"
                >
                    Key pair verified.
                    You can save changes within 5 minutes.
                </div>

            )}


            {/* Public key returned by backend */}

            {publicKey && (

                <label className="signature-key-field mt-3">

                    Registered public key

                    <textarea
                        className="form-control font-monospace"
                        rows={3}
                        value={publicKey}
                        readOnly
                    />

                </label>

            )}

        </section>
    );
}