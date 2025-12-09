/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.aphrodite.encryption

import com.arpanrec.aphrodite.exceptions.GPGException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bouncycastle.bcpg.ArmoredOutputStream
import org.bouncycastle.bcpg.CompressionAlgorithmTags
import org.bouncycastle.bcpg.HashAlgorithmTags
import org.bouncycastle.bcpg.PublicKeyPacket
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags
import org.bouncycastle.bcpg.sig.Features
import org.bouncycastle.bcpg.sig.KeyFlags
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPCompressedData
import org.bouncycastle.openpgp.PGPCompressedDataGenerator
import org.bouncycastle.openpgp.PGPEncryptedData
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator
import org.bouncycastle.openpgp.PGPEncryptedDataList
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPKeyRingGenerator
import org.bouncycastle.openpgp.PGPLiteralData
import org.bouncycastle.openpgp.PGPLiteralDataGenerator
import org.bouncycastle.openpgp.PGPObjectFactory
import org.bouncycastle.openpgp.PGPOnePassSignatureList
import org.bouncycastle.openpgp.PGPPrivateKey
import org.bouncycastle.openpgp.PGPPublicKey
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData
import org.bouncycastle.openpgp.PGPSecretKey
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.util.Date

class GnuPG(
    gpgContent: ByteArray,
    privateKeyPassphrase: String? = null,
) {
    private val log: Logger = LogManager.getLogger(this::class.java)

    private val pgpPrivateKeys: HashMap<Long, PGPPrivateKey> = HashMap()
    private val pgpPublicKeys: HashMap<Long, PGPPublicKey> = HashMap()
    private val pgpSigningKeys: HashMap<Long, PGPPrivateKey> = HashMap()

    init {
        log.info("Loading GPG armored private key.")
        this.loadGpgPrivateKeys(gpgContent, privateKeyPassphrase)
        log.info("Loading GPG armored public key.")
        this.loadGpgPublicKeyPrivateKeys()
    }

    private fun loadGpgPublicKeyPrivateKeys() {
        pgpPrivateKeys.forEach { (keyId, pgpPrivateKey) ->
            val publicKeyPacket: PublicKeyPacket? = pgpPrivateKey.publicKeyPacket
            try {
                val gpgPublicKey = PGPPublicKey(publicKeyPacket, BcKeyFingerprintCalculator())
                this.pgpPublicKeys[keyId] = gpgPublicKey
            } catch (e: NoSuchProviderException) {
                throw RuntimeException("Error converting PublicKeyPacket to PGPPublicKey", e)
            }
        }
    }

    fun encrypt(
        plainBytes: ByteArray,
        receiverKeyId: Long? = null,
    ): ByteArray {
        val clearTextDataByteOutputStream = ByteArrayOutputStream()
        val gpgCompressedDataGenerator = PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP)
        val gpgLiteralDataGenerator = PGPLiteralDataGenerator()
        val pOut: OutputStream =
            gpgLiteralDataGenerator.open(
                gpgCompressedDataGenerator.open(clearTextDataByteOutputStream),
                PGPLiteralData.TEXT,
                GnuPG::class.java.canonicalName,
                plainBytes.size.toLong(),
                Date(),
            )
        pOut.write(plainBytes)
        pOut.close()
        gpgCompressedDataGenerator.close()

        val encryptionKey =
            if (receiverKeyId != null) {
                if (!pgpPublicKeys.containsKey(receiverKeyId)) {
                    throw GPGException("No public key found for receiverKeyId: $receiverKeyId")
                }
                pgpPublicKeys[receiverKeyId]!!
            } else {
                if (pgpPublicKeys.isEmpty()) {
                    throw GPGException("No public key found.")
                }
                log.warn(
                    "No receiverKeyId provided. Using first available public key." +
                        "This can lead to unexpected results if multiple keys are loaded.",
                )
                pgpPublicKeys.values.first()
            }

        val encryptedDataGenerator =
            PGPEncryptedDataGenerator(
                JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                    .setWithIntegrityPacket(true)
                    .setSecureRandom(SecureRandom())
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME),
            )
        encryptedDataGenerator.addMethod(BcPublicKeyKeyEncryptionMethodGenerator(encryptionKey))

        val encryptedOut = ByteArrayOutputStream()
        val cipherOut: OutputStream =
            encryptedDataGenerator.open(
                encryptedOut,
                clearTextDataByteOutputStream.size().toLong(),
            )

        cipherOut.write(clearTextDataByteOutputStream.toByteArray())
        cipherOut.close()

        return encryptedOut.toByteArray()
    }

    private fun loadGpgPrivateKeys(
        privateKeyData: ByteArray,
        privateKeyPassphrase: String?,
    ) {
        val passphraseChars = privateKeyPassphrase?.toCharArray()

        val decoderStream: InputStream = PGPUtil.getDecoderStream(ByteArrayInputStream(privateKeyData))

        val secretKeyRings =
            PGPSecretKeyRingCollection(
                decoderStream,
                JcaKeyFingerprintCalculator(),
            )

        val keyRingIter = secretKeyRings.keyRings
        while (keyRingIter.hasNext()) {
            val keyRing = keyRingIter.next()
            val keyIter = keyRing.secretKeys
            while (keyIter.hasNext()) {
                val key = keyIter.next()
                val isEnc = isEncryptionKey(key)
                val isSign = isSigningKey(key)
                if (!isEnc && !isSign) continue

                val privateKey =
                    try {
                        key.extractPrivateKey(
                            JcePBESecretKeyDecryptorBuilder()
                                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                                .build(passphraseChars),
                        )
                    } catch (e: Exception) {
                        log.warn("Failed to decrypt private key.", e)
                        null
                    }

                if (privateKey != null) {
                    if (isEnc) {
                        pgpPrivateKeys[key.keyID] = privateKey
                        log.info("Private encryption key loaded with keyId: {}", key.keyID)
                    }
                    if (isSign) {
                        pgpSigningKeys[key.keyID] = privateKey
                        log.info("Private signing key loaded with keyId: {}", key.keyID)
                    }
                }
            }
        }

        log.info("Private key loaded.")
    }

    private fun isEncryptionKey(key: PGPSecretKey): Boolean {
        val publicKey = key.publicKey
        val signatures = publicKey.signatures
        while (signatures.hasNext()) {
            val signature = signatures.next() as PGPSignature
            if (signature.hashedSubPackets != null) {
                val keyFlags = signature.hashedSubPackets.keyFlags
                if (keyFlags != 0) {
                    if ((keyFlags and (KeyFlags.ENCRYPT_COMMS or KeyFlags.ENCRYPT_STORAGE)) != 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isSigningKey(key: PGPSecretKey): Boolean {
        val publicKey = key.publicKey
        val signatures = publicKey.signatures
        while (signatures.hasNext()) {
            val signature = signatures.next() as PGPSignature
            if (signature.hashedSubPackets != null) {
                val keyFlags = signature.hashedSubPackets.keyFlags
                if (keyFlags != 0) {
                    if ((keyFlags and KeyFlags.SIGN_DATA) != 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        val encryptedDataStream: InputStream = PGPUtil.getDecoderStream(ByteArrayInputStream(encryptedData))

        var publicKeyEncryptedData: PGPPublicKeyEncryptedData? = null

        val pgpObjectFactory =
            PGPObjectFactory(
                PGPUtil.getDecoderStream(encryptedDataStream),
                JcaKeyFingerprintCalculator(),
            )
        val o = pgpObjectFactory.nextObject()

        val encryptedDataList = o as? PGPEncryptedDataList ?: pgpObjectFactory.nextObject() as PGPEncryptedDataList

        val it = encryptedDataList.encryptedDataObjects
        while (it.hasNext()) {
            val data = it.next()

            if (data is PGPPublicKeyEncryptedData) {
                publicKeyEncryptedData = data
                break
            }
        }

        requireNotNull(publicKeyEncryptedData) { "No encrypted data found." }
        val keyId = publicKeyEncryptedData.keyIdentifier.keyId
        if (!pgpPrivateKeys.keys.contains(keyId)) {
            throw GPGException("No private key found for keyId: $keyId")
        }
        val pgpPrivateKey = pgpPrivateKeys[keyId]

        val clear =
            publicKeyEncryptedData.getDataStream(
                JcePublicKeyDataDecryptorFactoryBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(pgpPrivateKey),
            )
        val plainFact = PGPObjectFactory(clear, JcaKeyFingerprintCalculator())
        var message: Any = plainFact.nextObject()
        var pgpFact: PGPObjectFactory? = null

        if (message is PGPCompressedData) {
            pgpFact = PGPObjectFactory(message.dataStream, JcaKeyFingerprintCalculator())
            message = pgpFact.nextObject()
        }

        if (message is PGPOnePassSignatureList) {
            if (pgpFact != null) {
                message = pgpFact.nextObject()
            } else {
                throw GPGException("OnePassSignatureList found but no factory available")
            }
        }

        when (message) {
            is PGPLiteralData -> {
                val unc: InputStream = message.inputStream
                try {
                    ByteArrayOutputStream().use { out ->
                        val buffer = ByteArray(8 * 1024)
                        var read: Int
                        while (unc.read(buffer).also { read = it } >= 0) {
                            out.write(buffer, 0, read)
                        }
                        return out.toByteArray()
                    }
                } catch (e: Exception) {
                    throw PGPException("Failed to decrypt message", e)
                }
            }

            is PGPOnePassSignatureList -> {
                throw GPGException(
                    "Encrypted message contains a signed message - not literal data.",
                )
            }

            else -> {
                throw GPGException("Message is not a simple encrypted file - type unknown.")
            }
        }
    }

    companion object {
        fun createGpgPrivateKey(
            identity: String,
            email: String,
            password: String? = null,
            validityInDays: Int,
        ): ByteArray {
            val privateKeyPassphraseString: CharArray? = password?.toCharArray()

            val rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC")
            rsaKeyPairGenerator.initialize(1024, SecureRandom())
            val creationDate = Date()

            val masterKeyPair = rsaKeyPairGenerator.generateKeyPair()
            val masterPgpKeyPair = JcaPGPKeyPair(4, PGPPublicKey.RSA_GENERAL, masterKeyPair, creationDate)

            val masterSubpacketGenerator = PGPSignatureSubpacketGenerator()
            masterSubpacketGenerator.setKeyFlags(false, KeyFlags.CERTIFY_OTHER)
            masterSubpacketGenerator.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION)
            masterSubpacketGenerator.setPreferredSymmetricAlgorithms(
                false,
                intArrayOf(
                    SymmetricKeyAlgorithmTags.AES_256,
                    SymmetricKeyAlgorithmTags.AES_192,
                    SymmetricKeyAlgorithmTags.AES_128,
                ),
            )
            masterSubpacketGenerator.setPreferredHashAlgorithms(
                false,
                intArrayOf(
                    HashAlgorithmTags.SHA512,
                    HashAlgorithmTags.SHA384,
                    HashAlgorithmTags.SHA256,
                    HashAlgorithmTags.SHA1,
                ),
            )
            masterSubpacketGenerator.setPreferredCompressionAlgorithms(
                false,
                intArrayOf(
                    CompressionAlgorithmTags.ZLIB,
                    CompressionAlgorithmTags.BZIP2,
                    CompressionAlgorithmTags.ZIP,
                    CompressionAlgorithmTags.UNCOMPRESSED,
                ),
            )

            val sha1Calculator = JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1)
            val userId = "$identity <$email>"

            val encryptor =
                if (privateKeyPassphraseString != null) {
                    JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256)
                        .setProvider(BouncyCastleProvider())
                        .build(privateKeyPassphraseString)
                } else {
                    JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.NULL).setProvider(BouncyCastleProvider()).build(null)
                }

            val keyRingGenerator =
                PGPKeyRingGenerator(
                    PGPSignature.POSITIVE_CERTIFICATION,
                    masterPgpKeyPair,
                    userId,
                    sha1Calculator,
                    masterSubpacketGenerator.generate(),
                    null,
                    JcaPGPContentSignerBuilder(
                        masterPgpKeyPair.publicKey.algorithm,
                        HashAlgorithmTags.SHA256,
                    ),
                    encryptor,
                )

            fun addSubKey(keyFlags: Int) {
                val subKeyPair = rsaKeyPairGenerator.generateKeyPair()
                val subPgpKeyPair = JcaPGPKeyPair(4, PGPPublicKey.RSA_GENERAL, subKeyPair, creationDate)

                val subSubpacketGenerator = PGPSignatureSubpacketGenerator()
                subSubpacketGenerator.setKeyFlags(false, keyFlags)
                if (validityInDays > 0) {
                    subSubpacketGenerator.setKeyExpirationTime(true, validityInDays * 24L * 60 * 60)
                }

                keyRingGenerator.addSubKey(
                    subPgpKeyPair,
                    subSubpacketGenerator.generate(),
                    null,
                    JcaPGPContentSignerBuilder(
                        masterPgpKeyPair.publicKey.algorithm,
                        HashAlgorithmTags.SHA256,
                    ),
                )
            }

            addSubKey(KeyFlags.SIGN_DATA)

            addSubKey(KeyFlags.ENCRYPT_COMMS or KeyFlags.ENCRYPT_STORAGE)

            addSubKey(KeyFlags.AUTHENTICATION)

            val secretKeyRing: PGPSecretKeyRing = keyRingGenerator.generateSecretKeyRing()

            val out = ByteArrayOutputStream()
            secretKeyRing.encode(out)
            return out.toByteArray()
        }

        fun toArmor(binary: ByteArray): String {
            val out = ByteArrayOutputStream()
            ArmoredOutputStream(out).use { it.write(binary) }
            return out.toString(StandardCharsets.US_ASCII)
        }

        fun fromArmor(armored: String): ByteArray = PGPUtil.getDecoderStream(armored.byteInputStream()).readAllBytes()
    }
}
