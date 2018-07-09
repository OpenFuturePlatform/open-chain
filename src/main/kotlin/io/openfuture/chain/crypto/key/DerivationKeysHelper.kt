package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.domain.ExtendedKey
import org.springframework.stereotype.Component

/**
 * Component for hierarchical generating keys depending on the derivation path.
 * When derivation path is m/i then will be derived account keys.
 * When derivation path is m/i/k then will be derived external or internal account keys.
 * When derivation path is m/i/k/n then will be derived wallet keys.
 */
@Component
class DerivationKeysHelper {

    companion object {
        private const val PATH_SEPARATOR = '/'
        private const val HARDENED_DERIVATION_FLAG = 'h'
        private const val CORRECT_PATH_PATTERN = "^m([/]+[0-9]+[h]?){1,4}"

        private const val PURPOSE_NUMBER_POSITION = 1
        private const val ACCOUNT_NUMBER_POSITION = 2
        private const val WALLET_NUMBER_POSITION = 3
        private const val ADDRESS_NUMBER_POSITION = 4

        private const val DEFAULT_ACCOUNT_INDEX = 0
        private const val DEFAULT_MULTISIG_ACCOUNT_INDEX = 45
    }


    fun derive(rootKey: ExtendedKey, derivationPath: String): ExtendedKey {
        if (!CORRECT_PATH_PATTERN.toRegex().matches(derivationPath)) {
            throw Exception("Invalid derivation path")
        }

        val separatedPath = derivationPath.split(PATH_SEPARATOR)
        val purposeNumber = translatePathIndex(separatedPath[PURPOSE_NUMBER_POSITION])
        val purposeRoot = rootKey.derive(purposeNumber)

        if (PURPOSE_NUMBER_POSITION == separatedPath.size - 1) {
            return purposeRoot
        }

        val accountNumber = translatePathIndex(separatedPath[ACCOUNT_NUMBER_POSITION])

        if (ACCOUNT_NUMBER_POSITION == separatedPath.size - 1) {
            return purposeRoot.derive(accountNumber)
        }

        val walletNumber = translatePathIndex(separatedPath[WALLET_NUMBER_POSITION])

        if (WALLET_NUMBER_POSITION == separatedPath.size - 1) {
            return deriveWalletChainKeys(purposeRoot, accountNumber, walletNumber)
        }

        val addressNumber = translatePathIndex(separatedPath[ADDRESS_NUMBER_POSITION])
        val base = deriveWalletChainKeys(purposeRoot, accountNumber, walletNumber)

        return base.derive(addressNumber)
    }

    fun deriveDefaultAddress(rootKey: ExtendedKey): ExtendedKey {
        val base = deriveWalletChainKeys(rootKey, 0, 0)

        return base.derive(hardenedIndex(DEFAULT_ACCOUNT_INDEX))
    }

    fun deriveDefaultMultisigAddress(rootKey: ExtendedKey): ExtendedKey =
        rootKey.derive(hardenedIndex(DEFAULT_MULTISIG_ACCOUNT_INDEX))

    private fun deriveWalletChainKeys(rootKey: ExtendedKey, accountNumber: Int, walletNumber: Int): ExtendedKey {
        val base = rootKey.derive(accountNumber)
        return base.derive(walletNumber)
    }

    private fun hardenedIndex(index: Int): Int = index or -0x80000000

    /**
     * Non hardened indexes in [0, 2^31)
     * Hardened indexes in [2^31, 2^32)
     */
    private fun translatePathIndex(value: String): Int {
        return if (value.toLowerCase().contains(HARDENED_DERIVATION_FLAG)) {
            hardenedIndex(Integer.parseInt(value.dropLast(1)))
        } else {
            Integer.parseInt(value)
        }
    }

}