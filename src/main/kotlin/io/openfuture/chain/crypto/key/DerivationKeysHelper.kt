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
        private const val CORRECT_PATH_PATTERN = "^m([/]+[0-9]+[h]?){1,3}"

        private const val ACCOUNT_NUMBER_POSITION = 1
        private const val WALLET_NUMBER_POSITION = 2
        private const val ADDRESS_NUMBER_POSITION = 3
    }

    fun derive(rootKey: ExtendedKey, derivationPath: String): ExtendedKey {
        if (!CORRECT_PATH_PATTERN.toRegex().matches(derivationPath)) {
            throw Exception("Invalid derivation path")
        }

        val separatedPath = derivationPath.split(PATH_SEPARATOR)
        val accountNumber = translatePathNumber(separatedPath[ACCOUNT_NUMBER_POSITION])

        if (ACCOUNT_NUMBER_POSITION == separatedPath.size - 1) {
            return rootKey.derive(accountNumber)
        }

        val walletNumber = translatePathNumber(separatedPath[WALLET_NUMBER_POSITION])

        if (WALLET_NUMBER_POSITION == separatedPath.size - 1) {
            return deriveWalletChainKeys(rootKey, accountNumber, walletNumber)
        }

        val addressNumber = translatePathNumber(separatedPath[ADDRESS_NUMBER_POSITION])
        val base = deriveWalletChainKeys(rootKey, accountNumber, walletNumber)

        return base.derive(addressNumber)
    }

    fun deriveDefaultAddress(rootKey: ExtendedKey): ExtendedKey {
        val base = deriveWalletChainKeys(rootKey, 0, 0)

        return base.derive(0)
    }

    private fun deriveWalletChainKeys(rootKey: ExtendedKey, accountNumber: Int, walletNumber: Int): ExtendedKey {
        val base = rootKey.derive(accountNumber)
        return base.derive(walletNumber)
    }

    /**
     * Non hardened indexes in [0, 2^31)
     * Hardened indexes in [2^31 2^32)
     */
    private fun translatePathNumber(value: String): Int {
        return if (value.toLowerCase().contains(HARDENED_DERIVATION_FLAG)) {
            Integer.parseInt(value.dropLast(1)) or -0x80000000
        } else {
            Integer.parseInt(value)
        }
    }

}