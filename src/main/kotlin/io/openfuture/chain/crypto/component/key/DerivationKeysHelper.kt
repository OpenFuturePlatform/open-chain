package io.openfuture.chain.crypto.component.key

import io.openfuture.chain.crypto.model.dto.ExtendedKey
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

        const val DEFAULT_DERIVATION_KEY = "m/0/0/0"
    }


    fun derive(rootKey: ExtendedKey, derivationPath: String): ExtendedKey {
        if (!CORRECT_PATH_PATTERN.toRegex().matches(derivationPath)) {
            throw IllegalArgumentException("Invalid derivation path")
        }

        val separatedPath = derivationPath.split(PATH_SEPARATOR)
        val accountNumber = translatePathIndex(separatedPath[ACCOUNT_NUMBER_POSITION])

        if (ACCOUNT_NUMBER_POSITION == separatedPath.size - 1) {
            return rootKey.derive(accountNumber)
        }

        val walletNumber = translatePathIndex(separatedPath[WALLET_NUMBER_POSITION])

        if (WALLET_NUMBER_POSITION == separatedPath.size - 1) {
            return deriveWalletChainKeys(rootKey, accountNumber, walletNumber)
        }

        val addressNumber = translatePathIndex(separatedPath[ADDRESS_NUMBER_POSITION])
        val base = deriveWalletChainKeys(rootKey, accountNumber, walletNumber)

        return base.derive(addressNumber)
    }

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